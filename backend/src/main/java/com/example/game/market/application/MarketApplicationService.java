package com.example.game.market.application;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.activity.application.ActivityApplicationService;
import com.example.game.activity.domain.ActivityType;
import com.example.game.character.application.CharacterCombatGuard;
import com.example.game.character.application.CharacterIdentityService;
import com.example.game.character.application.CharacterLocationService;
import com.example.game.character.application.CharacterLocationView;
import com.example.game.character.application.CharacterVitalsService;
import com.example.game.character.application.CharacterVitalsView;
import com.example.game.inventory.application.InventoryApplicationService;
import com.example.game.inventory.application.InventoryFullException;
import com.example.game.inventory.application.MarketItemDisplay;
import com.example.game.inventory.application.OwnedItemSnapshot;
import com.example.game.item.application.ItemCatalogService;
import com.example.game.item.application.ItemDefinitionView;
import com.example.game.item.domain.ItemRarity;
import com.example.game.item.domain.ItemType;
import com.example.game.item.domain.WeaponFamily;
import com.example.game.market.domain.BuyOrderRules;
import com.example.game.market.domain.MarketBalance;
import com.example.game.market.domain.MarketBuyOrderStatus;
import com.example.game.market.domain.MarketFeeCalculator;
import com.example.game.market.domain.MarketListingSort;
import com.example.game.market.domain.MarketListingStatus;
import com.example.game.market.domain.MarketRules;
import com.example.game.market.infrastructure.MarketBuyOrderEntity;
import com.example.game.market.infrastructure.MarketBuyOrderFillEntity;
import com.example.game.market.infrastructure.MarketBuyOrderFillRepository;
import com.example.game.market.infrastructure.MarketBuyOrderRepository;
import com.example.game.market.infrastructure.MarketListingEntity;
import com.example.game.market.infrastructure.MarketListingRepository;
import com.example.game.telemetry.application.GameTelemetry;
import com.example.game.telemetry.application.GameTelemetryRecorder;
import com.example.game.telemetry.domain.GoldDestroyReason;
import com.example.game.world.application.LocationView;
import com.example.game.world.application.WorldApplicationService;
import com.example.game.world.domain.LocationAction;
import com.example.game.world.domain.LocationActions;

/**
 * Marketplace flows.
 *
 * <p>Every write here takes row locks in one fixed order: the listing or buy-order row first, then
 * character rows in id order, then item instances. Locking the order/listing before any character
 * row keeps a purchase and a concurrent cancellation from deadlocking.
 */
@Service
public class MarketApplicationService {

	private final CharacterVitalsService characterVitalsService;
	private final CharacterLocationService characterLocationService;
	private final CharacterIdentityService characterIdentityService;
	private final CharacterCombatGuard characterCombatGuard;
	private final WorldApplicationService worldApplicationService;
	private final InventoryApplicationService inventoryApplicationService;
	private final ItemCatalogService itemCatalogService;
	private final ActivityApplicationService activityApplicationService;
	private final MarketListingRepository marketListingRepository;
	private final MarketBuyOrderRepository marketBuyOrderRepository;
	private final MarketBuyOrderFillRepository marketBuyOrderFillRepository;
	private final GameTelemetryRecorder gameTelemetryRecorder;
	private final Clock clock;

	public MarketApplicationService(
			CharacterVitalsService characterVitalsService,
			CharacterLocationService characterLocationService,
			CharacterIdentityService characterIdentityService,
			CharacterCombatGuard characterCombatGuard,
			WorldApplicationService worldApplicationService,
			InventoryApplicationService inventoryApplicationService,
			ItemCatalogService itemCatalogService,
			ActivityApplicationService activityApplicationService,
			MarketListingRepository marketListingRepository,
			MarketBuyOrderRepository marketBuyOrderRepository,
			MarketBuyOrderFillRepository marketBuyOrderFillRepository,
			GameTelemetryRecorder gameTelemetryRecorder,
			Clock clock) {
		this.characterVitalsService = characterVitalsService;
		this.characterLocationService = characterLocationService;
		this.characterIdentityService = characterIdentityService;
		this.characterCombatGuard = characterCombatGuard;
		this.worldApplicationService = worldApplicationService;
		this.inventoryApplicationService = inventoryApplicationService;
		this.itemCatalogService = itemCatalogService;
		this.activityApplicationService = activityApplicationService;
		this.marketListingRepository = marketListingRepository;
		this.marketBuyOrderRepository = marketBuyOrderRepository;
		this.marketBuyOrderFillRepository = marketBuyOrderFillRepository;
		this.gameTelemetryRecorder = gameTelemetryRecorder;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public MarketFeesView fees() {
		return new MarketFeesView(MarketBalance.LISTING_FEE_PERCENT, MarketBalance.SALE_FEE_PERCENT);
	}

	@Transactional(readOnly = true)
	public MarketListingPage listActive(
			UUID accountId,
			boolean mine,
			ItemType itemType,
			ItemRarity rarity,
			WeaponFamily weaponFamily,
			Integer minLevel,
			Integer maxLevel,
			Integer minPrice,
			Integer maxPrice,
			MarketListingSort sort,
			Sort.Direction direction,
			int page,
			int size) {
		CharacterLocationView locationView = characterLocationService.locationOf(accountId);
		Pageable pageable = listingPageable(page, size, sort, direction);
		UUID sellerId = mine ? locationView.characterId() : null;
		Page<MarketListingEntity> found = marketListingRepository.search(
				MarketListingStatus.ACTIVE,
				sellerId,
				itemType,
				rarity,
				weaponFamily,
				minLevel,
				maxLevel,
				minPrice,
				maxPrice,
				pageable);
		return new MarketListingPage(
				toListingViews(found.getContent(), locationView.characterId()),
				found.getNumber(),
				found.getSize(),
				found.getTotalElements(),
				found.hasNext());
	}

	@Transactional(readOnly = true)
	public MarketListingPage history(UUID accountId, int page, int size) {
		CharacterLocationView locationView = characterLocationService.locationOf(accountId);
		Pageable pageable = PageRequest.of(
				Math.max(0, page),
				clampSize(size),
				Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<MarketListingEntity> found = marketListingRepository.findHistory(
				locationView.characterId(),
				List.of(MarketListingStatus.SOLD, MarketListingStatus.CANCELLED),
				pageable);
		return new MarketListingPage(
				toListingViews(found.getContent(), locationView.characterId()),
				found.getNumber(),
				found.getSize(),
				found.getTotalElements(),
				found.hasNext());
	}

	@Transactional
	public MarketListingView create(UUID accountId, UUID itemInstanceId, int quantity, int price) {
		if (!MarketRules.isValidPrice(price)) {
			throw MarketErrors.invalidPrice();
		}
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		assertAtMarket(accountId, LocationAction.CREATE_LISTING);
		characterCombatGuard.assertNotInActiveCombat(vitals.characterId());

		OwnedItemSnapshot item = inventoryApplicationService.requireOwnedItemForTrade(
				vitals.characterId(),
				itemInstanceId);
		if (item.equipped()) {
			throw MarketErrors.cannotSellEquippedItem();
		}
		if (!MarketRules.isValidQuantity(quantity, item.unreservedQuantity())) {
			throw MarketErrors.invalidListingQuantity();
		}
		ItemDefinitionView definition = requireDefinition(item.itemDefinitionId());
		int listingFee = MarketFeeCalculator.listingFee(price);
		if (vitals.gold() < listingFee) {
			throw MarketErrors.insufficientGoldForListingFee();
		}
		if (listingFee > 0) {
			characterVitalsService.spendGold(vitals.characterId(), listingFee, GoldDestroyReason.LISTING_FEE);
		}

		MarketListingEntity listing = new MarketListingEntity(
				UUID.randomUUID(),
				vitals.characterId(),
				item.itemInstanceId(),
				item.itemDefinitionId(),
				quantity,
				price,
				listingFee,
				item.rarity(),
				definition.type(),
				definition.weaponFamily(),
				definition.requiredLevel(),
				Instant.now(clock));
		marketListingRepository.saveAndFlush(listing);
		if (listingFee > 0) {
			activityApplicationService.record(
					vitals.characterId(),
					ActivityType.MARKET_LISTING_FEE,
					"You paid a " + listingFee + " gold listing fee.");
		}
		return toListingView(listing, vitals.characterId(), definition);
	}

	@Transactional
	public MarketListingView buy(UUID accountId, UUID listingId) {
		UUID buyerId = characterLocationService.locationOf(accountId).characterId();

		MarketListingEntity listing = marketListingRepository.findWithLockById(listingId)
				.orElseThrow(MarketErrors::listingNotFound);
		if (listing.getStatus() != MarketListingStatus.ACTIVE) {
			throw MarketErrors.listingNotActive();
		}
		UUID sellerId = listing.getSellerCharacterId();
		if (MarketRules.isOwnListing(buyerId, sellerId)) {
			throw MarketErrors.cannotBuyOwnListing();
		}

		CharacterVitalsView buyer = lockBuyerAndSeller(buyerId, sellerId);
		assertAtMarket(accountId, LocationAction.BUY_ITEM);
		characterCombatGuard.assertNotInActiveCombat(buyerId);
		if (buyer.gold() < listing.getPrice()) {
			throw MarketErrors.insufficientGold();
		}

		OwnedItemSnapshot item = inventoryApplicationService.requireOwnedItemForTrade(
				sellerId,
				listing.getItemInstanceId());
		if (item.quantity() < listing.getQuantity()) {
			throw MarketErrors.listingNotActive();
		}

		int saleFee = MarketFeeCalculator.saleFee(listing.getPrice());
		int proceeds = MarketFeeCalculator.sellerProceeds(listing.getPrice());
		Instant now = Instant.now(clock);
		listing.markSold(buyerId, now, saleFee);
		marketListingRepository.saveAndFlush(listing);

		try {
			inventoryApplicationService.transferListedQuantity(
					sellerId,
					buyerId,
					listing.getItemInstanceId(),
					listing.getQuantity());
		}
		catch (InventoryFullException exception) {
			throw MarketErrors.buyerInventoryFull();
		}

		characterVitalsService.spendGold(buyerId, listing.getPrice());
		if (proceeds > 0) {
			characterVitalsService.addGold(sellerId, proceeds);
		}
		GameTelemetry.goldDestroyed(gameTelemetryRecorder, buyerId, saleFee, GoldDestroyReason.SALE_FEE);
		GameTelemetry.marketTrade(
				gameTelemetryRecorder,
				buyerId,
				sellerId,
				listing.getPrice(),
				saleFee,
				proceeds,
				"LISTING");

		ItemDefinitionView definition = requireDefinition(listing.getItemDefinitionId());
		activityApplicationService.recordMarketSold(sellerId, listing.getPrice());
		activityApplicationService.record(
				sellerId,
				ActivityType.MARKET_SALE,
				"Sale fee of " + saleFee + " gold was collected.");
		activityApplicationService.recordMarketBought(buyerId, definition.name());
		return toListingView(listing, buyerId, definition);
	}

	@Transactional
	public MarketListingView cancel(UUID accountId, UUID listingId) {
		MarketListingEntity listing = marketListingRepository.findWithLockById(listingId)
				.orElseThrow(MarketErrors::listingNotFound);

		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		assertAtMarket(accountId, LocationAction.CANCEL_LISTING);
		characterCombatGuard.assertNotInActiveCombat(vitals.characterId());
		if (!listing.getSellerCharacterId().equals(vitals.characterId())) {
			throw MarketErrors.listingNotOwned();
		}
		if (listing.getStatus() != MarketListingStatus.ACTIVE) {
			throw MarketErrors.listingNotActive();
		}

		listing.markCancelled(Instant.now(clock));
		marketListingRepository.saveAndFlush(listing);
		activityApplicationService.recordMarketCancelled(vitals.characterId());
		return toListingView(listing, vitals.characterId(), requireDefinition(listing.getItemDefinitionId()));
	}

	@Transactional(readOnly = true)
	public MarketBuyOrderPage listBuyOrders(
			UUID accountId,
			boolean mine,
			UUID itemDefinitionId,
			int page,
			int size) {
		CharacterLocationView locationView = characterLocationService.locationOf(accountId);
		Pageable pageable = PageRequest.of(
				Math.max(0, page),
				clampSize(size),
				Sort.by(Sort.Direction.DESC, "createdAt"));
		UUID buyerId = mine ? locationView.characterId() : null;
		Page<MarketBuyOrderEntity> found = marketBuyOrderRepository.search(
				MarketBuyOrderStatus.ACTIVE,
				buyerId,
				itemDefinitionId,
				pageable);
		return new MarketBuyOrderPage(
				toBuyOrderViews(found.getContent(), locationView.characterId()),
				found.getNumber(),
				found.getSize(),
				found.getTotalElements(),
				found.hasNext());
	}

	@Transactional
	public MarketBuyOrderView createBuyOrder(
			UUID accountId,
			UUID itemDefinitionId,
			int quantity,
			int maxUnitPrice) {
		if (!MarketRules.isValidPrice(maxUnitPrice) || quantity < 1) {
			throw MarketErrors.invalidPrice();
		}
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		assertAtMarket(accountId, LocationAction.CREATE_BUY_ORDER);
		characterCombatGuard.assertNotInActiveCombat(vitals.characterId());
		ItemDefinitionView definition = itemCatalogService.findByIds(List.of(itemDefinitionId)).get(itemDefinitionId);
		if (definition == null) {
			throw MarketErrors.itemDefinitionNotFound();
		}
		int escrow = BuyOrderRules.escrowGold(quantity, maxUnitPrice);
		int postingFee = MarketFeeCalculator.buyOrderPostingFee(escrow);
		if (vitals.gold() < Math.addExact(escrow, postingFee)) {
			throw MarketErrors.insufficientGold();
		}
		if (postingFee > 0) {
			characterVitalsService.spendGold(
					vitals.characterId(),
					postingFee,
					GoldDestroyReason.BUY_ORDER_POSTING_FEE);
		}
		characterVitalsService.spendGold(vitals.characterId(), escrow);
		MarketBuyOrderEntity order = new MarketBuyOrderEntity(
				UUID.randomUUID(),
				vitals.characterId(),
				itemDefinitionId,
				quantity,
				maxUnitPrice,
				escrow,
				postingFee,
				Instant.now(clock));
		marketBuyOrderRepository.saveAndFlush(order);
		if (postingFee > 0) {
			activityApplicationService.record(
					vitals.characterId(),
					ActivityType.MARKET_LISTING_FEE,
					"You paid a " + postingFee + " gold buy-order posting fee.");
		}
		activityApplicationService.record(
				vitals.characterId(),
				ActivityType.BUY_ORDER_CREATED,
				"You posted a buy order for " + definition.name() + ".");
		return toBuyOrderView(order, vitals.characterId(), definition, characterIdentityService.requireName(vitals.characterId()));
	}

	@Transactional
	public MarketBuyOrderView fulfillBuyOrder(UUID accountId, UUID orderId, UUID itemInstanceId, int quantity) {
		UUID sellerId = characterLocationService.locationOf(accountId).characterId();
		MarketBuyOrderEntity order = marketBuyOrderRepository.findWithLockById(orderId)
				.orElseThrow(MarketErrors::buyOrderNotFound);
		if (order.getStatus() != MarketBuyOrderStatus.ACTIVE || order.getRemainingQuantity() < 1) {
			throw MarketErrors.buyOrderNotActive();
		}
		UUID buyerId = order.getBuyerCharacterId();
		if (BuyOrderRules.isOwnOrder(buyerId, sellerId)) {
			throw MarketErrors.cannotFulfillOwnOrder();
		}

		lockBuyerAndSeller(buyerId, sellerId);
		assertAtMarket(accountId, LocationAction.FULFILL_BUY_ORDER);
		characterCombatGuard.assertNotInActiveCombat(sellerId);

		OwnedItemSnapshot item = inventoryApplicationService.requireOwnedItemForTrade(sellerId, itemInstanceId);
		if (item.equipped()) {
			throw MarketErrors.cannotSellEquippedItem();
		}
		if (!item.itemDefinitionId().equals(order.getItemDefinitionId())) {
			throw MarketErrors.buyOrderItemMismatch();
		}
		if (quantity < 1 || quantity > order.getRemainingQuantity() || quantity > item.unreservedQuantity()) {
			throw MarketErrors.invalidBuyOrderQuantity();
		}

		BuyOrderRules.Fill fill = BuyOrderRules.applyFill(
				order.getRemainingQuantity(),
				order.getReservedGold(),
				order.getMaxUnitPrice(),
				quantity);
		int saleFee = MarketFeeCalculator.saleFee(fill.grossGold());
		int proceeds = MarketFeeCalculator.sellerProceeds(fill.grossGold());
		Instant now = Instant.now(clock);
		order.applyFill(fill.remainingQuantity(), fill.reservedGoldAfter(), fill.completed(), now);
		marketBuyOrderRepository.saveAndFlush(order);
		marketBuyOrderFillRepository.saveAndFlush(new MarketBuyOrderFillEntity(
				UUID.randomUUID(),
				order.getId(),
				sellerId,
				itemInstanceId,
				quantity,
				fill.grossGold(),
				saleFee,
				now));

		try {
			inventoryApplicationService.transferListedQuantity(sellerId, buyerId, itemInstanceId, quantity);
		}
		catch (InventoryFullException exception) {
			throw MarketErrors.buyerInventoryFull();
		}
		if (proceeds > 0) {
			characterVitalsService.addGold(sellerId, proceeds);
		}
		GameTelemetry.goldDestroyed(gameTelemetryRecorder, buyerId, saleFee, GoldDestroyReason.SALE_FEE);
		GameTelemetry.marketTrade(
				gameTelemetryRecorder,
				buyerId,
				sellerId,
				fill.grossGold(),
				saleFee,
				proceeds,
				"BUY_ORDER");

		ItemDefinitionView definition = requireDefinition(order.getItemDefinitionId());
		activityApplicationService.record(
				sellerId,
				ActivityType.BUY_ORDER_FILLED,
				"You filled a buy order for " + definition.name() + ".");
		activityApplicationService.record(
				buyerId,
				ActivityType.BUY_ORDER_FILLED,
				"Your buy order for " + definition.name() + " was filled.");
		return toBuyOrderView(order, sellerId, definition, characterIdentityService.requireName(buyerId));
	}

	@Transactional
	public MarketBuyOrderView cancelBuyOrder(UUID accountId, UUID orderId) {
		MarketBuyOrderEntity order = marketBuyOrderRepository.findWithLockById(orderId)
				.orElseThrow(MarketErrors::buyOrderNotFound);
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		assertAtMarket(accountId, LocationAction.CREATE_BUY_ORDER);
		characterCombatGuard.assertNotInActiveCombat(vitals.characterId());
		if (!order.getBuyerCharacterId().equals(vitals.characterId())) {
			throw MarketErrors.buyOrderNotOwned();
		}
		if (order.getStatus() != MarketBuyOrderStatus.ACTIVE) {
			throw MarketErrors.buyOrderNotActive();
		}
		int refund = order.cancel(Instant.now(clock));
		marketBuyOrderRepository.saveAndFlush(order);
		if (refund > 0) {
			characterVitalsService.addGold(vitals.characterId(), refund);
		}
		ItemDefinitionView definition = requireDefinition(order.getItemDefinitionId());
		activityApplicationService.record(
				vitals.characterId(),
				ActivityType.BUY_ORDER_CANCELLED,
				"You cancelled your buy order for " + definition.name() + ".");
		return toBuyOrderView(order, vitals.characterId(), definition, characterIdentityService.requireName(vitals.characterId()));
	}

	private CharacterVitalsView lockBuyerAndSeller(UUID buyerId, UUID sellerId) {
		if (buyerId.compareTo(sellerId) < 0) {
			CharacterVitalsView buyer = characterVitalsService.lockVitalsByCharacterId(buyerId);
			characterVitalsService.lockVitalsByCharacterId(sellerId);
			return buyer;
		}
		characterVitalsService.lockVitalsByCharacterId(sellerId);
		return characterVitalsService.lockVitalsByCharacterId(buyerId);
	}

	private void assertAtMarket(UUID accountId, LocationAction action) {
		LocationView location = worldApplicationService.currentLocation(accountId);
		if (!LocationActions.forCode(location.code()).contains(action)) {
			throw MarketErrors.locationCannotUseMarket();
		}
	}

	private List<MarketListingView> toListingViews(List<MarketListingEntity> listings, UUID viewerCharacterId) {
		if (listings.isEmpty()) {
			return List.of();
		}
		Set<UUID> definitionIds = new HashSet<>();
		Set<UUID> sellerIds = new HashSet<>();
		Set<UUID> instanceIds = new HashSet<>();
		for (MarketListingEntity listing : listings) {
			definitionIds.add(listing.getItemDefinitionId());
			sellerIds.add(listing.getSellerCharacterId());
			if (listing.getItemInstanceId() != null) {
				instanceIds.add(listing.getItemInstanceId());
			}
		}
		Map<UUID, ItemDefinitionView> definitions = itemCatalogService.findByIds(definitionIds);
		Map<UUID, String> sellerNames = characterIdentityService.namesOf(sellerIds);
		Map<UUID, MarketItemDisplay> displays = inventoryApplicationService.marketDisplays(instanceIds);
		List<MarketListingView> views = new ArrayList<>();
		for (MarketListingEntity listing : listings) {
			ItemDefinitionView definition = definitions.get(listing.getItemDefinitionId());
			if (definition == null) {
				continue;
			}
			views.add(toListingView(
					listing,
					viewerCharacterId,
					definition,
					sellerNames.getOrDefault(listing.getSellerCharacterId(), "Unknown"),
					displays.get(listing.getItemInstanceId())));
		}
		return views;
	}

	private MarketListingView toListingView(
			MarketListingEntity listing,
			UUID viewerCharacterId,
			ItemDefinitionView definition) {
		MarketItemDisplay display = listing.getItemInstanceId() == null
				? null
				: inventoryApplicationService.marketDisplays(List.of(listing.getItemInstanceId()))
						.get(listing.getItemInstanceId());
		return toListingView(
				listing,
				viewerCharacterId,
				definition,
				characterIdentityService.requireName(listing.getSellerCharacterId()),
				display);
	}

	private static MarketListingView toListingView(
			MarketListingEntity listing,
			UUID viewerCharacterId,
			ItemDefinitionView definition,
			String sellerName,
			MarketItemDisplay display) {
		String displayName = display == null ? definition.name() : display.displayName();
		return new MarketListingView(
				listing.getId(),
				listing.getSellerCharacterId(),
				sellerName,
				listing.getItemInstanceId(),
				definition.id(),
				definition.code(),
				definition.name(),
				displayName,
				listing.getItemType(),
				listing.getInstanceRarity(),
				listing.getWeaponFamily(),
				listing.getRequiredLevel(),
				listing.getQuantity(),
				listing.getPrice(),
				listing.getListingFeePaid(),
				listing.getSaleFeePaid(),
				listing.getStatus(),
				listing.getCreatedAt(),
				listing.getSoldAt(),
				listing.getSellerCharacterId().equals(viewerCharacterId),
				display == null ? List.of() : display.affixes());
	}

	private List<MarketBuyOrderView> toBuyOrderViews(List<MarketBuyOrderEntity> orders, UUID viewerCharacterId) {
		if (orders.isEmpty()) {
			return List.of();
		}
		Set<UUID> definitionIds = new HashSet<>();
		Set<UUID> buyerIds = new HashSet<>();
		for (MarketBuyOrderEntity order : orders) {
			definitionIds.add(order.getItemDefinitionId());
			buyerIds.add(order.getBuyerCharacterId());
		}
		Map<UUID, ItemDefinitionView> definitions = itemCatalogService.findByIds(definitionIds);
		Map<UUID, String> names = characterIdentityService.namesOf(buyerIds);
		List<MarketBuyOrderView> views = new ArrayList<>();
		for (MarketBuyOrderEntity order : orders) {
			ItemDefinitionView definition = definitions.get(order.getItemDefinitionId());
			if (definition == null) {
				continue;
			}
			views.add(toBuyOrderView(
					order,
					viewerCharacterId,
					definition,
					names.getOrDefault(order.getBuyerCharacterId(), "Unknown")));
		}
		return views;
	}

	private static MarketBuyOrderView toBuyOrderView(
			MarketBuyOrderEntity order,
			UUID viewerCharacterId,
			ItemDefinitionView definition,
			String buyerName) {
		return new MarketBuyOrderView(
				order.getId(),
				order.getBuyerCharacterId(),
				buyerName,
				order.getItemDefinitionId(),
				definition.code(),
				definition.name(),
				definition.type(),
				order.getRemainingQuantity(),
				order.getOriginalQuantity(),
				order.getMaxUnitPrice(),
				order.getReservedGold(),
				order.getPostingFeePaid(),
				order.getStatus(),
				order.getCreatedAt(),
				order.getBuyerCharacterId().equals(viewerCharacterId));
	}

	private ItemDefinitionView requireDefinition(UUID itemDefinitionId) {
		ItemDefinitionView definition = itemCatalogService.findByIds(List.of(itemDefinitionId)).get(itemDefinitionId);
		if (definition == null) {
			throw new IllegalStateException("Missing item definition: " + itemDefinitionId);
		}
		return definition;
	}

	private static Pageable listingPageable(
			int page,
			int size,
			MarketListingSort sort,
			Sort.Direction direction) {
		String property = sort == MarketListingSort.PRICE ? "price" : "createdAt";
		Sort.Direction dir = direction == null ? Sort.Direction.DESC : direction;
		return PageRequest.of(Math.max(0, page), clampSize(size), Sort.by(dir, property));
	}

	private static int clampSize(int size) {
		if (size < 1) {
			return MarketBalance.LISTING_PAGE_SIZE;
		}
		return Math.min(size, MarketBalance.MAX_LISTING_PAGE_SIZE);
	}
}
