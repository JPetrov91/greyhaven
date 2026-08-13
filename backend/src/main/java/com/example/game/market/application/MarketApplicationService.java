package com.example.game.market.application;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.activity.application.ActivityApplicationService;
import com.example.game.character.application.CharacterCombatGuard;
import com.example.game.character.application.CharacterIdentityService;
import com.example.game.character.application.CharacterLocationService;
import com.example.game.character.application.CharacterLocationView;
import com.example.game.character.application.CharacterVitalsService;
import com.example.game.character.application.CharacterVitalsView;
import com.example.game.inventory.application.InventoryApplicationService;
import com.example.game.inventory.application.InventoryFullException;
import com.example.game.inventory.application.OwnedItemSnapshot;
import com.example.game.item.application.ItemCatalogService;
import com.example.game.item.application.ItemDefinitionView;
import com.example.game.item.domain.ItemType;
import com.example.game.market.domain.MarketListingStatus;
import com.example.game.market.domain.MarketRules;
import com.example.game.market.infrastructure.MarketListingEntity;
import com.example.game.market.infrastructure.MarketListingRepository;
import com.example.game.world.application.LocationView;
import com.example.game.world.application.WorldApplicationService;
import com.example.game.world.domain.LocationAction;
import com.example.game.world.domain.LocationActions;

/**
 * Marketplace flows.
 *
 * <p>Every write here takes row locks in one fixed order: the listing row first, then character
 * rows in id order, then item instances. Locking the listing before any character row is what
 * keeps a purchase and a concurrent cancellation of the same listing from deadlocking, because no
 * transaction ever waits for a listing while already holding a character row.
 */
@Service
public class MarketApplicationService {

	/**
	 * The market is browsed as a page of the newest listings rather than the whole table, which
	 * would otherwise grow with every unsold offer in the world.
	 */
	static final int ACTIVE_LISTING_LIMIT = 100;

	private final CharacterVitalsService characterVitalsService;
	private final CharacterLocationService characterLocationService;
	private final CharacterIdentityService characterIdentityService;
	private final CharacterCombatGuard characterCombatGuard;
	private final WorldApplicationService worldApplicationService;
	private final InventoryApplicationService inventoryApplicationService;
	private final ItemCatalogService itemCatalogService;
	private final ActivityApplicationService activityApplicationService;
	private final MarketListingRepository marketListingRepository;
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
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public MarketListingPage listActive(UUID accountId, ItemType itemType, boolean mine) {
		CharacterLocationView locationView = characterLocationService.locationOf(accountId);

		Set<UUID> typeDefinitionIds = null;
		if (itemType != null) {
			typeDefinitionIds = itemCatalogService.idsOfType(itemType);
			if (typeDefinitionIds.isEmpty()) {
				return new MarketListingPage(List.of(), false);
			}
		}
		List<MarketListingEntity> found = findActive(locationView.characterId(), mine, typeDefinitionIds);
		boolean truncated = found.size() > ACTIVE_LISTING_LIMIT;
		if (truncated) {
			found = found.subList(0, ACTIVE_LISTING_LIMIT);
		}
		return new MarketListingPage(toViews(found, locationView.characterId()), truncated);
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

		MarketListingEntity listing = new MarketListingEntity(
				UUID.randomUUID(),
				vitals.characterId(),
				item.itemInstanceId(),
				item.itemDefinitionId(),
				quantity,
				price,
				Instant.now(clock));
		marketListingRepository.saveAndFlush(listing);
		return toView(listing, vitals.characterId(), requireDefinition(item.itemDefinitionId()));
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

		Instant now = Instant.now(clock);
		listing.markSold(buyerId, now);
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
		characterVitalsService.addGold(sellerId, listing.getPrice());

		ItemDefinitionView definition = requireDefinition(listing.getItemDefinitionId());
		activityApplicationService.recordMarketSold(sellerId, listing.getPrice());
		activityApplicationService.recordMarketBought(buyerId, definition.name());
		return toView(listing, buyerId, definition);
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
		return toView(listing, vitals.characterId(), requireDefinition(listing.getItemDefinitionId()));
	}

	/**
	 * Locks both sides of a trade in character-id order and returns the buyer's locked state. The
	 * order matters when two characters buy from each other at the same moment: without it each
	 * transaction could hold the row the other one is waiting for.
	 */
	private CharacterVitalsView lockBuyerAndSeller(UUID buyerId, UUID sellerId) {
		if (buyerId.compareTo(sellerId) < 0) {
			CharacterVitalsView buyer = characterVitalsService.lockVitalsByCharacterId(buyerId);
			characterVitalsService.lockVitalsByCharacterId(sellerId);
			return buyer;
		}
		characterVitalsService.lockVitalsByCharacterId(sellerId);
		return characterVitalsService.lockVitalsByCharacterId(buyerId);
	}

	private List<MarketListingEntity> findActive(
			UUID viewerCharacterId,
			boolean mine,
			Set<UUID> typeDefinitionIds) {
		Limit limit = Limit.of(ACTIVE_LISTING_LIMIT + 1);
		if (mine) {
			return typeDefinitionIds == null
					? marketListingRepository.findBySellerCharacterIdAndStatusOrderByCreatedAtDesc(
							viewerCharacterId,
							MarketListingStatus.ACTIVE,
							limit)
					: marketListingRepository
							.findBySellerCharacterIdAndStatusAndItemDefinitionIdInOrderByCreatedAtDesc(
									viewerCharacterId,
									MarketListingStatus.ACTIVE,
									typeDefinitionIds,
									limit);
		}
		return typeDefinitionIds == null
				? marketListingRepository.findByStatusOrderByCreatedAtDesc(MarketListingStatus.ACTIVE, limit)
				: marketListingRepository.findByStatusAndItemDefinitionIdInOrderByCreatedAtDesc(
						MarketListingStatus.ACTIVE,
						typeDefinitionIds,
						limit);
	}

	private void assertAtMarket(UUID accountId, LocationAction action) {
		LocationView location = worldApplicationService.currentLocation(accountId);
		if (!LocationActions.forCode(location.code()).contains(action)) {
			throw MarketErrors.locationCannotUseMarket();
		}
	}

	private List<MarketListingView> toViews(List<MarketListingEntity> listings, UUID viewerCharacterId) {
		if (listings.isEmpty()) {
			return List.of();
		}
		Set<UUID> definitionIds = new HashSet<>();
		Set<UUID> sellerIds = new HashSet<>();
		for (MarketListingEntity listing : listings) {
			definitionIds.add(listing.getItemDefinitionId());
			sellerIds.add(listing.getSellerCharacterId());
		}
		Map<UUID, ItemDefinitionView> definitions = itemCatalogService.findByIds(definitionIds);
		Map<UUID, String> sellerNames = characterIdentityService.namesOf(sellerIds);
		List<MarketListingView> views = new ArrayList<>();
		for (MarketListingEntity listing : listings) {
			ItemDefinitionView definition = definitions.get(listing.getItemDefinitionId());
			if (definition == null) {
				continue;
			}
			views.add(toView(
					listing,
					viewerCharacterId,
					definition,
					sellerNames.getOrDefault(listing.getSellerCharacterId(), "Unknown")));
		}
		return views;
	}

	private MarketListingView toView(MarketListingEntity listing, UUID viewerCharacterId, ItemDefinitionView definition) {
		return toView(
				listing,
				viewerCharacterId,
				definition,
				characterIdentityService.requireName(listing.getSellerCharacterId()));
	}

	private static MarketListingView toView(
			MarketListingEntity listing,
			UUID viewerCharacterId,
			ItemDefinitionView definition,
			String sellerName) {
		return new MarketListingView(
				listing.getId(),
				listing.getSellerCharacterId(),
				sellerName,
				listing.getItemInstanceId(),
				definition.code(),
				definition.name(),
				definition.type(),
				definition.rarity(),
				listing.getQuantity(),
				listing.getPrice(),
				listing.getStatus(),
				listing.getCreatedAt(),
				listing.getSoldAt(),
				listing.getSellerCharacterId().equals(viewerCharacterId));
	}

	private ItemDefinitionView requireDefinition(UUID itemDefinitionId) {
		ItemDefinitionView definition = itemCatalogService.findByIds(List.of(itemDefinitionId)).get(itemDefinitionId);
		if (definition == null) {
			throw new IllegalStateException("Missing item definition: " + itemDefinitionId);
		}
		return definition;
	}
}
