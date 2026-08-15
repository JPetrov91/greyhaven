package com.example.game.market.application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.activity.application.ActivityApplicationService;
import com.example.game.character.application.CharacterCombatGuard;
import com.example.game.character.application.CharacterVitalsService;
import com.example.game.character.application.CharacterVitalsView;
import com.example.game.inventory.application.InventoryApplicationService;
import com.example.game.inventory.application.InventoryFullException;
import com.example.game.inventory.application.OwnedItemSnapshot;
import com.example.game.item.application.ItemCatalogService;
import com.example.game.item.application.ItemDefinitionView;
import com.example.game.item.domain.ItemStatCalculator;
import com.example.game.item.domain.ItemStats;
import com.example.game.item.domain.ItemType;
import com.example.game.market.domain.MerchantPriceCalculator;
import com.example.game.market.domain.MerchantRules;
import com.example.game.market.infrastructure.MerchantDefinitionEntity;
import com.example.game.market.infrastructure.MerchantDefinitionRepository;
import com.example.game.market.infrastructure.MerchantStockEntity;
import com.example.game.market.infrastructure.MerchantStockRepository;
import com.example.game.telemetry.application.GameTelemetry;
import com.example.game.telemetry.application.GameTelemetryRecorder;
import com.example.game.telemetry.domain.GoldCreateReason;
import com.example.game.telemetry.domain.GoldDestroyReason;
import com.example.game.telemetry.domain.ItemCreateSource;
import com.example.game.world.application.LocationView;
import com.example.game.world.application.WorldApplicationService;
import com.example.game.world.domain.LocationAction;
import com.example.game.world.domain.LocationActions;
import com.example.game.world.infrastructure.NpcDefinitionEntity;
import com.example.game.world.infrastructure.NpcDefinitionRepository;

@Service
public class MerchantApplicationService {

	private final MerchantDefinitionRepository merchantDefinitionRepository;
	private final MerchantStockRepository merchantStockRepository;
	private final ItemCatalogService itemCatalogService;
	private final CharacterVitalsService characterVitalsService;
	private final CharacterCombatGuard characterCombatGuard;
	private final WorldApplicationService worldApplicationService;
	private final InventoryApplicationService inventoryApplicationService;
	private final ActivityApplicationService activityApplicationService;
	private final GameTelemetryRecorder gameTelemetryRecorder;
	private final NpcDefinitionRepository npcDefinitionRepository;

	public MerchantApplicationService(
			MerchantDefinitionRepository merchantDefinitionRepository,
			MerchantStockRepository merchantStockRepository,
			ItemCatalogService itemCatalogService,
			CharacterVitalsService characterVitalsService,
			CharacterCombatGuard characterCombatGuard,
			WorldApplicationService worldApplicationService,
			InventoryApplicationService inventoryApplicationService,
			ActivityApplicationService activityApplicationService,
			GameTelemetryRecorder gameTelemetryRecorder,
			NpcDefinitionRepository npcDefinitionRepository) {
		this.merchantDefinitionRepository = merchantDefinitionRepository;
		this.merchantStockRepository = merchantStockRepository;
		this.itemCatalogService = itemCatalogService;
		this.characterVitalsService = characterVitalsService;
		this.characterCombatGuard = characterCombatGuard;
		this.worldApplicationService = worldApplicationService;
		this.inventoryApplicationService = inventoryApplicationService;
		this.activityApplicationService = activityApplicationService;
		this.gameTelemetryRecorder = gameTelemetryRecorder;
		this.npcDefinitionRepository = npcDefinitionRepository;
	}

	@Transactional(readOnly = true)
	public List<MerchantView> listMerchants() {
		List<MerchantDefinitionEntity> merchants = merchantDefinitionRepository.findAllByOrderBySortOrderAsc();
		Set<UUID> merchantIds = merchants.stream().map(MerchantDefinitionEntity::getId).collect(Collectors.toSet());
		List<MerchantStockEntity> stockRows = merchantIds.isEmpty()
				? List.of()
				: merchantStockRepository.findByMerchantIdInOrderByMerchantIdAscSortOrderAsc(merchantIds);
		Map<UUID, List<MerchantStockEntity>> stockByMerchant = new HashMap<>();
		for (MerchantStockEntity row : stockRows) {
			stockByMerchant.computeIfAbsent(row.getMerchantId(), key -> new ArrayList<>()).add(row);
		}
		Map<UUID, ItemDefinitionView> definitions = itemCatalogService.findByIds(
				stockRows.stream().map(MerchantStockEntity::getItemDefinitionId).toList());
		Map<String, NpcDefinitionEntity> people = peopleByMerchantCode();
		List<MerchantView> views = new ArrayList<>();
		for (MerchantDefinitionEntity merchant : merchants) {
			views.add(toView(merchant, stockByMerchant.getOrDefault(merchant.getId(), List.of()), definitions, people));
		}
		return views;
	}

	@Transactional(readOnly = true)
	public MerchantView getMerchant(UUID merchantId) {
		MerchantDefinitionEntity merchant = merchantDefinitionRepository.findById(merchantId)
				.orElseThrow(MarketErrors::merchantNotFound);
		List<MerchantStockEntity> stock = merchantStockRepository.findByMerchantIdOrderBySortOrderAsc(merchantId);
		Map<UUID, ItemDefinitionView> definitions = itemCatalogService.findByIds(
				stock.stream().map(MerchantStockEntity::getItemDefinitionId).toList());
		return toView(merchant, stock, definitions, peopleByMerchantCode());
	}

	@Transactional
	public MerchantPurchaseView purchase(UUID accountId, UUID merchantId, UUID itemDefinitionId, int quantity) {
		MerchantDefinitionEntity merchant = merchantDefinitionRepository.findById(merchantId)
				.orElseThrow(MarketErrors::merchantNotFound);
		MerchantStockEntity stock = merchantStockRepository
				.findByMerchantIdAndItemDefinitionId(merchantId, itemDefinitionId)
				.orElseThrow(MarketErrors::itemNotSoldByMerchant);
		ItemDefinitionView definition = requireDefinition(stock.getItemDefinitionId());
		if (!MerchantRules.isValidPurchaseQuantity(quantity, definition.type().isStackable())) {
			throw MarketErrors.invalidMerchantQuantity();
		}

		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		assertAtMarket(accountId, LocationAction.BUY_ITEM);
		characterCombatGuard.assertNotInActiveCombat(vitals.characterId());

		int unitPrice = MerchantPriceCalculator.merchantSellPrice(definition.baseValue(), definition.rarity());
		int totalPrice = Math.multiplyExact(unitPrice, quantity);
		if (vitals.gold() < totalPrice) {
			throw MarketErrors.insufficientGold();
		}

		try {
			inventoryApplicationService.grantMerchantPurchase(vitals.characterId(), definition.code(), quantity);
		}
		catch (InventoryFullException exception) {
			throw MarketErrors.merchantInventoryFull();
		}
		characterVitalsService.spendGold(vitals.characterId(), totalPrice, GoldDestroyReason.MERCHANT_BUY);
		GameTelemetry.itemCreated(
				gameTelemetryRecorder,
				vitals.characterId(),
				definition.code(),
				definition.rarity(),
				quantity,
				ItemCreateSource.MERCHANT);

		int goldRemaining = vitals.gold() - totalPrice;
		if (shouldRecordPurchase(definition.type())) {
			activityApplicationService.recordMerchantBought(vitals.characterId(), definition.name(), totalPrice);
		}
		return new MerchantPurchaseView(
				merchant.getId(),
				definition.id(),
				definition.code(),
				definition.name(),
				quantity,
				totalPrice,
				goldRemaining);
	}

	@Transactional
	public MerchantSaleView sell(UUID accountId, UUID itemInstanceId, int quantity) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		assertAtMarket(accountId, LocationAction.CREATE_LISTING);
		characterCombatGuard.assertNotInActiveCombat(vitals.characterId());

		OwnedItemSnapshot item = inventoryApplicationService.requireOwnedItemForTrade(
				vitals.characterId(),
				itemInstanceId);
		if (item.equipped()) {
			throw MarketErrors.cannotSellEquippedItem();
		}
		if (!MerchantRules.isValidQuantity(quantity, item.unreservedQuantity())) {
			if (item.unreservedQuantity() < item.quantity()) {
				throw MarketErrors.cannotSellListedItem();
			}
			throw MarketErrors.invalidMerchantQuantity();
		}

		ItemDefinitionView definition = requireDefinition(item.itemDefinitionId());
		int unitPrice = MerchantPriceCalculator.merchantBuyPrice(
				definition.baseValue(),
				item.rarity(),
				item.affixCount());
		int goldAwarded = Math.multiplyExact(unitPrice, quantity);

		inventoryApplicationService.consumeUnreservedQuantity(
				vitals.characterId(),
				itemInstanceId,
				quantity);
		characterVitalsService.addGold(vitals.characterId(), goldAwarded, GoldCreateReason.MERCHANT_SELL);

		int goldRemaining = vitals.gold() + goldAwarded;
		if (shouldRecordSale(definition.type())) {
			activityApplicationService.recordMerchantSold(vitals.characterId(), definition.name(), goldAwarded);
		}
		return new MerchantSaleView(
				itemInstanceId,
				definition.code(),
				definition.name(),
				quantity,
				goldAwarded,
				goldRemaining);
	}

	private MerchantView toView(
			MerchantDefinitionEntity merchant,
			List<MerchantStockEntity> stock,
			Map<UUID, ItemDefinitionView> definitions,
			Map<String, NpcDefinitionEntity> people) {
		List<MerchantStockItemView> items = new ArrayList<>();
		for (MerchantStockEntity row : stock) {
			ItemDefinitionView definition = definitions.get(row.getItemDefinitionId());
			if (definition == null) {
				continue;
			}
			items.add(toStockView(row, definition));
		}
		NpcDefinitionEntity person = people.get(merchant.getCode());
		return new MerchantView(
				merchant.getId(),
				merchant.getCode(),
				person != null ? person.getName() : merchant.getName(),
				person != null ? person.getTitle() : merchant.getTitle(),
				person != null ? person.getDescription() : merchant.getDescription(),
				merchant.getMerchantType(),
				person != null ? person.getPortraitCode() : merchant.getPortraitCode(),
				items);
	}

	private Map<String, NpcDefinitionEntity> peopleByMerchantCode() {
		return npcDefinitionRepository.findAllByOrderBySortOrderAsc().stream()
				.filter(npc -> npc.getMerchantCode() != null && !npc.getMerchantCode().isBlank())
				.collect(Collectors.toMap(NpcDefinitionEntity::getMerchantCode, npc -> npc, (left, right) -> left));
	}

	private static MerchantStockItemView toStockView(MerchantStockEntity row, ItemDefinitionView definition) {
		ItemStats stats = ItemStatCalculator.calculate(
				definition.weaponDamage(),
				definition.armorValue(),
				definition.modifiers().stream()
						.map(modifier -> new ItemStatCalculator.AppliedAffix(modifier.stat(), modifier.magnitude()))
						.toList(),
				List.of());
		return new MerchantStockItemView(
				definition.id(),
				definition.code(),
				definition.name(),
				definition.description(),
				definition.type(),
				definition.rarity(),
				MerchantPriceCalculator.merchantSellPrice(definition.baseValue(), definition.rarity()),
				row.getAvailabilityType(),
				definition.requiredLevel(),
				displayWeapon(definition.weaponDamage(), stats),
				displayArmor(definition.armorValue(), stats),
				definition.healAmount(),
				definition.twoHanded(),
				definition.equipmentSlot(),
				definition.weaponFamily(),
				definition.armorCategory(),
				definition.requiredStrength(),
				definition.requiredAgility(),
				definition.requiredEndurance(),
				definition.requiredPerception(),
				stats.accuracy(),
				stats.criticalChance(),
				stats.dodge(),
				stats.strength(),
				stats.agility(),
				stats.endurance(),
				stats.perception(),
				stats.staminaCostReduction());
	}

	private static Integer displayWeapon(Integer catalogDamage, ItemStats stats) {
		if (catalogDamage == null && stats.weaponDamage() == 0) {
			return null;
		}
		return stats.weaponDamage();
	}

	private static Integer displayArmor(Integer catalogArmor, ItemStats stats) {
		if (catalogArmor == null && stats.armor() == 0) {
			return null;
		}
		return stats.armor();
	}

	private ItemDefinitionView requireDefinition(UUID itemDefinitionId) {
		ItemDefinitionView definition = itemCatalogService.findByIds(List.of(itemDefinitionId)).get(itemDefinitionId);
		if (definition == null) {
			throw new IllegalStateException("Missing item definition: " + itemDefinitionId);
		}
		return definition;
	}

	private void assertAtMarket(UUID accountId, LocationAction action) {
		LocationView location = worldApplicationService.currentLocation(accountId);
		if (!LocationActions.forCode(location.code()).contains(action)) {
			throw MarketErrors.locationCannotUseMarket();
		}
	}

	private static boolean shouldRecordPurchase(ItemType type) {
		return type == ItemType.WEAPON || type == ItemType.ARMOR || type == ItemType.ACCESSORY;
	}

	private static boolean shouldRecordSale(ItemType type) {
		return type != ItemType.CONSUMABLE;
	}
}
