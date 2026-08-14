package com.example.game.inventory.application;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.character.application.CharacterCombatGuard;
import com.example.game.character.application.CharacterVitalsService;
import com.example.game.character.application.CharacterVitalsView;
import com.example.game.character.domain.CharacterStatCalculator;
import com.example.game.character.domain.DerivedCombatStats;
import com.example.game.inventory.domain.EquipmentSlot;
import com.example.game.inventory.domain.EquipmentValidator;
import com.example.game.inventory.domain.InventoryBalance;
import com.example.game.inventory.infrastructure.EquipmentEntity;
import com.example.game.inventory.infrastructure.EquipmentRepository;
import com.example.game.item.application.AffixCatalogService;
import com.example.game.item.domain.AffixCatalog;
import com.example.game.item.domain.ArmorCategory;
import com.example.game.item.domain.GeneratedItem;
import com.example.game.item.domain.ItemBalance;
import com.example.game.item.domain.ItemCodes;
import com.example.game.item.domain.ItemDefinitionData;
import com.example.game.item.domain.ItemDisplayNames;
import com.example.game.item.domain.ItemGenerator;
import com.example.game.item.domain.ItemStatCalculator;
import com.example.game.item.domain.ItemStats;
import com.example.game.item.domain.ItemType;
import com.example.game.item.domain.RolledAffix;
import com.example.game.item.infrastructure.ItemDefinitionEntity;
import com.example.game.item.infrastructure.ItemDefinitionModifierEntity;
import com.example.game.item.infrastructure.ItemDefinitionModifierRepository;
import com.example.game.item.infrastructure.ItemDefinitionRepository;
import com.example.game.item.infrastructure.ItemInstanceAffixEntity;
import com.example.game.item.infrastructure.ItemInstanceAffixRepository;
import com.example.game.item.infrastructure.ItemInstanceEntity;
import com.example.game.item.infrastructure.ItemInstanceRepository;
import com.example.game.market.domain.MerchantPriceCalculator;
import com.example.game.shared.domain.RandomProvider;

@Service
public class InventoryApplicationService implements HealingPotionConsumption {

	private final ItemDefinitionRepository itemDefinitionRepository;
	private final ItemInstanceRepository itemInstanceRepository;
	private final EquipmentRepository equipmentRepository;
	private final CharacterVitalsService characterVitalsService;
	private final CharacterCombatGuard characterCombatGuard;
	private final ItemReservationQuery itemReservationQuery;
	private final ItemInstanceAffixRepository itemInstanceAffixRepository;
	private final ItemDefinitionModifierRepository itemDefinitionModifierRepository;
	private final AffixCatalogService affixCatalogService;
	private final RandomProvider randomProvider;
	private final Clock clock;

	public InventoryApplicationService(
			ItemDefinitionRepository itemDefinitionRepository,
			ItemInstanceRepository itemInstanceRepository,
			EquipmentRepository equipmentRepository,
			CharacterVitalsService characterVitalsService,
			CharacterCombatGuard characterCombatGuard,
			ItemReservationQuery itemReservationQuery,
			ItemInstanceAffixRepository itemInstanceAffixRepository,
			ItemDefinitionModifierRepository itemDefinitionModifierRepository,
			AffixCatalogService affixCatalogService,
			RandomProvider randomProvider,
			Clock clock) {
		this.itemDefinitionRepository = itemDefinitionRepository;
		this.itemInstanceRepository = itemInstanceRepository;
		this.equipmentRepository = equipmentRepository;
		this.characterVitalsService = characterVitalsService;
		this.characterCombatGuard = characterCombatGuard;
		this.itemReservationQuery = itemReservationQuery;
		this.itemInstanceAffixRepository = itemInstanceAffixRepository;
		this.itemDefinitionModifierRepository = itemDefinitionModifierRepository;
		this.affixCatalogService = affixCatalogService;
		this.randomProvider = randomProvider;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public InventoryView getInventory(UUID accountId) {
		CharacterVitalsView vitals = characterVitalsService.vitalsOf(accountId);
		return buildInventoryView(vitals);
	}

	@Transactional
	public InventoryView equip(UUID accountId, UUID itemInstanceId) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		characterCombatGuard.assertNotInActiveCombat(vitals.characterId());
		equipForCharacter(vitals, itemInstanceId);
		return buildInventoryView(vitals);
	}

	@Transactional
	public InventoryView unequip(UUID accountId, UUID itemInstanceId) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		characterCombatGuard.assertNotInActiveCombat(vitals.characterId());
		requireOwnedInstance(vitals.characterId(), itemInstanceId);

		EquipmentEntity equipped = equipmentRepository.findWithLockByItemInstanceId(itemInstanceId)
				.orElseThrow(InventoryErrors::itemNotEquipped);
		if (!equipped.getCharacterId().equals(vitals.characterId())) {
			throw InventoryErrors.itemNotOwned();
		}

		equipmentRepository.delete(equipped);
		equipmentRepository.flush();
		return buildInventoryView(vitals);
	}

	@Transactional
	public InventoryView use(UUID accountId, UUID itemInstanceId) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		characterCombatGuard.assertNotInActiveCombat(vitals.characterId());
		ItemInstanceEntity instance = requireOwnedInstance(vitals.characterId(), itemInstanceId);
		ItemDefinitionEntity definition = requireDefinition(instance.getItemDefinitionId());

		if (!isUsable(definition) || equipmentRepository.existsByItemInstanceId(instance.getId())) {
			throw InventoryErrors.itemNotUsable();
		}
		if (unreservedQuantity(instance) < 1) {
			throw InventoryErrors.itemListed();
		}

		instance.decreaseQuantity(1);
		if (instance.getQuantity() == 0) {
			itemInstanceRepository.delete(instance);
		}
		else {
			itemInstanceRepository.saveAndFlush(instance);
		}

		characterVitalsService.heal(accountId, definition.getHealAmount());
		CharacterVitalsView updatedVitals = characterVitalsService.vitalsOf(accountId);
		return buildInventoryView(updatedVitals);
	}

	/**
	 * Whether the character has at least one healing potion that can be consumed in combat.
	 */
	@Transactional(readOnly = true)
	public boolean hasHealingPotion(UUID characterId) {
		ItemDefinitionEntity potion = itemDefinitionRepository.findByCode(ItemCodes.HEALING_POTION)
				.orElse(null);
		if (potion == null || potion.getHealAmount() == null) {
			return false;
		}
		return itemInstanceRepository
				.findByOwnerCharacterIdAndItemDefinitionId(characterId, potion.getId())
				.filter(instance -> unreservedQuantity(instance) > 0)
				.isPresent();
	}

	@Transactional(readOnly = true)
	public HealingPotionStock healingPotionStock(UUID characterId) {
		ItemDefinitionEntity potion = itemDefinitionRepository.findByCode(ItemCodes.HEALING_POTION)
				.orElse(null);
		if (potion == null || potion.getHealAmount() == null) {
			return new HealingPotionStock(0, 0);
		}
		int quantity = itemInstanceRepository
				.findByOwnerCharacterIdAndItemDefinitionId(characterId, potion.getId())
				.map(this::unreservedQuantity)
				.orElse(0);
		return new HealingPotionStock(quantity, potion.getHealAmount());
	}

	@Transactional(readOnly = true)
	public List<PublicEquippedItemView> publicEquippedItems(UUID characterId) {
		List<ItemInstanceEntity> instances = itemInstanceRepository
				.findByOwnerCharacterIdOrderByCreatedAtAscIdAsc(characterId);
		Map<UUID, ItemDefinitionEntity> definitions = loadDefinitions(instances);
		Map<UUID, List<ItemInstanceAffixEntity>> affixesByInstance = loadAffixes(instances);
		AffixCatalog catalog = affixCatalogService.load();
		Map<UUID, ItemInstanceEntity> byId = new HashMap<>();
		for (ItemInstanceEntity instance : instances) {
			byId.put(instance.getId(), instance);
		}
		List<PublicEquippedItemView> equipped = new ArrayList<>();
		for (Map.Entry<EquipmentSlot, UUID> entry : equippedItemIds(characterId).entrySet()) {
			ItemInstanceEntity instance = byId.get(entry.getValue());
			if (instance == null) {
				continue;
			}
			ItemDefinitionEntity definition = definitions.get(instance.getItemDefinitionId());
			if (definition == null) {
				continue;
			}
			ItemStats stats = statsOf(
					instance,
					definition,
					loadCatalogModifiers(Set.of(definition.getId())),
					affixesByInstance,
					catalog);
			List<RolledAffix> rolled = rolledAffixes(affixesByInstance.getOrDefault(instance.getId(), List.of()));
			List<ItemAffixView> affixViews = rolled.stream()
					.map(affix -> {
						var def = catalog.require(affix.affixCode());
						return new ItemAffixView(
								def.code(),
								def.kind(),
								def.displayName(),
								def.stat(),
								affix.magnitude());
					})
					.toList();
			equipped.add(new PublicEquippedItemView(
					entry.getKey(),
					definition.getCode(),
					ItemDisplayNames.compose(definition.getName(), rolled, catalog),
					instance.getRarity(),
					stats.weaponDamage() == 0 ? null : stats.weaponDamage(),
					stats.armor() == 0 ? null : stats.armor(),
					affixViews));
		}
		return equipped;
	}

	/**
	 * Consumes up to {@code maxCharges} healing potions. Returns the number actually taken
	 * and the potion heal amount (0 heal if the definition is missing).
	 */
	@Override
	@Transactional
	public HealingPotionStock consumeUpTo(UUID characterId, int maxCharges) {
		characterVitalsService.lockVitalsByCharacterId(characterId);
		ItemDefinitionEntity potion = itemDefinitionRepository.findByCode(ItemCodes.HEALING_POTION)
				.orElse(null);
		if (potion == null || potion.getHealAmount() == null) {
			return new HealingPotionStock(0, 0);
		}
		int healAmount = potion.getHealAmount();
		if (maxCharges < 1) {
			return new HealingPotionStock(0, healAmount);
		}
		ItemInstanceEntity instance = itemInstanceRepository
				.findWithLockByOwnerCharacterIdAndItemDefinitionId(characterId, potion.getId())
				.orElse(null);
		if (instance == null) {
			return new HealingPotionStock(0, healAmount);
		}
		int consumed = Math.min(maxCharges, unreservedQuantity(instance));
		if (consumed < 1) {
			return new HealingPotionStock(0, healAmount);
		}
		instance.decreaseQuantity(consumed);
		if (instance.getQuantity() == 0) {
			itemInstanceRepository.delete(instance);
		}
		else {
			itemInstanceRepository.saveAndFlush(instance);
		}
		return new HealingPotionStock(consumed, healAmount);
	}

	/**
	 * Consumes one healing potion for combat. Returns heal amount. Does not apply heal itself.
	 */
	@Transactional
	public int consumeOneHealingPotion(UUID characterId) {
		characterVitalsService.lockVitalsByCharacterId(characterId);
		ItemDefinitionEntity potion = itemDefinitionRepository.findByCode(ItemCodes.HEALING_POTION)
				.orElseThrow(() -> InventoryErrors.itemDefinitionMissing(ItemCodes.HEALING_POTION));
		if (potion.getHealAmount() == null) {
			throw InventoryErrors.itemNotUsable();
		}
		ItemInstanceEntity instance = itemInstanceRepository
				.findWithLockByOwnerCharacterIdAndItemDefinitionId(characterId, potion.getId())
				.orElseThrow(InventoryErrors::itemNotFound);
		if (unreservedQuantity(instance) < 1) {
			throw InventoryErrors.itemListed();
		}
		instance.decreaseQuantity(1);
		if (instance.getQuantity() == 0) {
			itemInstanceRepository.delete(instance);
		}
		else {
			itemInstanceRepository.saveAndFlush(instance);
		}
		return potion.getHealAmount();
	}

	/**
	 * Grants items to a character, merging stackable definitions and enforcing capacity.
	 * Used by starter loadout and other server-side grants.
	 */
	@Transactional
	public void grantItems(UUID characterId, String itemCode, int quantity) {
		if (quantity < 1) {
			throw new IllegalArgumentException("quantity must be positive");
		}
		// Serialize capacity and stack merges against the character row.
		characterVitalsService.lockVitalsByCharacterId(characterId);

		ItemDefinitionEntity definition = itemDefinitionRepository.findByCode(itemCode)
				.orElseThrow(() -> InventoryErrors.itemDefinitionMissing(itemCode));
		boolean stackable = definition.getType().isStackable();

		if (stackable) {
			ItemInstanceEntity existing = itemInstanceRepository
					.findWithLockByOwnerCharacterIdAndItemDefinitionId(characterId, definition.getId())
					.orElse(null);
			if (existing != null) {
				existing.increaseQuantity(quantity);
				itemInstanceRepository.saveAndFlush(existing);
				return;
			}
			ensureCapacity(characterId, 1);
			itemInstanceRepository.saveAndFlush(copyDefinitionInstance(
					characterId,
					definition,
					quantity,
					true,
					Instant.now(clock)));
			return;
		}

		ensureCapacity(characterId, quantity);
		Instant now = Instant.now(clock);
		AffixCatalog catalog = affixCatalogService.load();
		for (int i = 0; i < quantity; i++) {
			persistOwnedInstance(characterId, definition, rollItem(definition, catalog), false, now);
		}
	}

	/**
	 * Rolls a new instance snapshot without persisting it. Combat and expedition plans store this
	 * so a later grant retry cannot reroll rarity or affixes.
	 */
	@Transactional(readOnly = true)
	public GeneratedItem rollItem(String itemCode) {
		ItemDefinitionEntity definition = itemDefinitionRepository.findByCode(itemCode)
				.orElseThrow(() -> InventoryErrors.itemDefinitionMissing(itemCode));
		return rollItem(definition, affixCatalogService.load());
	}

	/**
	 * Grants a previously rolled snapshot. Unique items must be quantity 1.
	 */
	@Transactional
	public void grantRolled(UUID characterId, String itemCode, int quantity, GeneratedItem generated) {
		if (quantity < 1) {
			throw new IllegalArgumentException("quantity must be positive");
		}
		if (generated == null) {
			throw new IllegalArgumentException("generated item is required");
		}
		characterVitalsService.lockVitalsByCharacterId(characterId);
		ItemDefinitionEntity definition = itemDefinitionRepository.findByCode(itemCode)
				.orElseThrow(() -> InventoryErrors.itemDefinitionMissing(itemCode));
		if (definition.getType().isStackable()) {
			grantItems(characterId, itemCode, quantity);
			return;
		}
		if (quantity != 1) {
			throw new IllegalArgumentException("non-stackable rolled grants must have quantity 1");
		}
		ensureCapacity(characterId, 1);
		persistOwnedInstance(characterId, definition, generated, false, Instant.now(clock));
	}

	/**
	 * Grants an equippable item using catalog base stats and no affixes. Used for the starter
	 * loadout so new characters stay deterministic.
	 */
	@Transactional
	public void grantCatalogExact(UUID characterId, String itemCode, int quantity) {
		if (quantity < 1) {
			throw new IllegalArgumentException("quantity must be positive");
		}
		characterVitalsService.lockVitalsByCharacterId(characterId);
		ItemDefinitionEntity definition = itemDefinitionRepository.findByCode(itemCode)
				.orElseThrow(() -> InventoryErrors.itemDefinitionMissing(itemCode));
		GeneratedItem generated = new GeneratedItem(
				definition.getRarity(),
				definition.getWeaponDamage(),
				definition.getArmorValue(),
				List.of());
		ensureCapacity(characterId, quantity);
		Instant now = Instant.now(clock);
		for (int i = 0; i < quantity; i++) {
			persistOwnedInstance(characterId, definition, generated, false, now);
		}
	}

	/**
	 * Grants a merchant purchase using catalog-exact equipment (no random affixes) or stack merge
	 * for consumables and materials.
	 */
	@Transactional(propagation = Propagation.MANDATORY)
	public void grantMerchantPurchase(UUID characterId, String itemCode, int quantity) {
		ItemDefinitionEntity definition = itemDefinitionRepository.findByCode(itemCode)
				.orElseThrow(() -> InventoryErrors.itemDefinitionMissing(itemCode));
		if (definition.getType().isStackable()) {
			grantItems(characterId, itemCode, quantity);
			return;
		}
		grantCatalogExact(characterId, itemCode, quantity);
	}

	/**
	 * Destroys unreserved quantity as an NPC item sink. Listed quantity must already be excluded by
	 * the caller.
	 */
	@Transactional(propagation = Propagation.MANDATORY)
	public void consumeUnreservedQuantity(UUID characterId, UUID itemInstanceId, int quantity) {
		if (quantity < 1) {
			throw new IllegalArgumentException("quantity must be positive");
		}
		ItemInstanceEntity instance = requireOwnedInstance(characterId, itemInstanceId);
		if (equipmentRepository.existsByItemInstanceId(instance.getId())) {
			throw InventoryErrors.itemNotOwned();
		}
		int available = unreservedQuantity(instance);
		if (available < quantity) {
			throw InventoryErrors.itemListed();
		}
		instance.decreaseQuantity(quantity);
		if (instance.getQuantity() == 0) {
			itemInstanceAffixRepository.deleteAll(
					itemInstanceAffixRepository.findByItemInstanceIdIn(List.of(instance.getId())));
			itemInstanceRepository.delete(instance);
		}
		else {
			itemInstanceRepository.saveAndFlush(instance);
		}
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public OwnedItemSnapshot requireOwnedItemForTrade(UUID characterId, UUID itemInstanceId) {
		characterVitalsService.lockVitalsByCharacterId(characterId);
		ItemInstanceEntity instance = requireOwnedInstance(characterId, itemInstanceId);
		int affixCount = itemInstanceAffixRepository.findByItemInstanceIdIn(List.of(instance.getId())).size();
		return new OwnedItemSnapshot(
				instance.getId(),
				instance.getItemDefinitionId(),
				instance.getQuantity(),
				unreservedQuantity(instance),
				equipmentRepository.existsByItemInstanceId(instance.getId()),
				instance.getRarity(),
				affixCount);
	}

	/**
	 * Moves listed quantity from seller to buyer, merging stackable stacks so uniqueness holds.
	 */
	@Transactional(propagation = Propagation.MANDATORY)
	public void transferListedQuantity(
			UUID fromCharacterId,
			UUID toCharacterId,
			UUID itemInstanceId,
			int quantity) {
		if (quantity < 1) {
			throw new IllegalArgumentException("quantity must be positive");
		}
		ItemInstanceEntity instance = requireOwnedInstance(fromCharacterId, itemInstanceId);
		if (equipmentRepository.existsByItemInstanceId(instance.getId())) {
			throw InventoryErrors.itemListed();
		}
		if (instance.getQuantity() < quantity) {
			throw InventoryErrors.itemNotFound();
		}

		if (instance.isStackable()) {
			ItemInstanceEntity buyerStack = itemInstanceRepository
					.findWithLockByOwnerCharacterIdAndItemDefinitionId(toCharacterId, instance.getItemDefinitionId())
					.orElse(null);
			if (buyerStack != null) {
				buyerStack.increaseQuantity(quantity);
				itemInstanceRepository.saveAndFlush(buyerStack);
				instance.decreaseQuantity(quantity);
				if (instance.getQuantity() == 0) {
					itemInstanceRepository.delete(instance);
					itemInstanceRepository.flush();
				}
				else {
					itemInstanceRepository.saveAndFlush(instance);
				}
				return;
			}
			if (instance.getQuantity() == quantity) {
				ensureCapacity(toCharacterId, 1);
				instance.transferTo(toCharacterId);
				itemInstanceRepository.saveAndFlush(instance);
				return;
			}
			ensureCapacity(toCharacterId, 1);
			instance.decreaseQuantity(quantity);
			itemInstanceRepository.saveAndFlush(instance);
			itemInstanceRepository.saveAndFlush(new ItemInstanceEntity(
					UUID.randomUUID(),
					instance.getItemDefinitionId(),
					toCharacterId,
					quantity,
					true,
					instance.getRarity(),
					instance.getRolledWeaponDamage(),
					instance.getRolledArmorValue(),
					instance.isLegacy(),
					Instant.now(clock)));
			return;
		}

		if (quantity != 1) {
			throw new IllegalArgumentException("non-stackable transfer quantity must be 1");
		}
		ensureCapacity(toCharacterId, 1);
		instance.transferTo(toCharacterId);
		itemInstanceRepository.saveAndFlush(instance);
	}

	/**
	 * Equips an owned item using the same rules as the player-facing equip path (type + level).
	 */
	@Transactional
	public void equipOwnedItem(UUID characterId, UUID itemInstanceId) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsByCharacterId(characterId);
		equipForCharacter(vitals, itemInstanceId);
	}

	private void equipForCharacter(CharacterVitalsView vitals, UUID itemInstanceId) {
		ItemInstanceEntity instance = requireOwnedInstance(vitals.characterId(), itemInstanceId);
		if (reservedQuantity(instance.getId()) > 0) {
			throw InventoryErrors.itemListed();
		}
		ItemDefinitionEntity definition = requireDefinition(instance.getItemDefinitionId());

		ItemDefinitionData data = definition.toData();
		boolean listed = reservedQuantity(instance.getId()) > 0;
		EquipmentValidator.CharacterRequirements requirements = requirementsOf(vitals);
		boolean mainHandTwoHanded = isMainHandTwoHanded(vitals.characterId());
		EquipmentValidator.Failure failure = EquipmentValidator.validate(
				data,
				listed,
				requirements,
				mainHandTwoHanded);
		if (failure == EquipmentValidator.Failure.LISTED) {
			throw InventoryErrors.itemListed();
		}
		if (failure == EquipmentValidator.Failure.REQUIREMENTS_NOT_MET) {
			throw InventoryErrors.equipRequirementsNotMet();
		}
		if (failure == EquipmentValidator.Failure.TWO_HANDED_BLOCKS_OFF_HAND) {
			throw InventoryErrors.twoHandedBlocksOffHand();
		}
		if (failure != null) {
			throw InventoryErrors.itemNotEquippable();
		}

		EquipmentSlot slot = data.equipmentSlot();
		if (definition.isTwoHanded()) {
			equipmentRepository.findWithLockByCharacterIdAndSlot(vitals.characterId(), EquipmentSlot.OFF_HAND)
					.ifPresent(offHand -> {
						equipmentRepository.delete(offHand);
						equipmentRepository.flush();
					});
		}
		equipmentRepository.findWithLockByCharacterIdAndSlot(vitals.characterId(), slot)
				.ifPresentOrElse(
						existing -> existing.equip(instance.getId()),
						() -> equipmentRepository.saveAndFlush(new EquipmentEntity(
								UUID.randomUUID(),
								vitals.characterId(),
								slot,
								instance.getId())));
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void unequipInvalidEquipment(UUID characterId, EquipmentValidator.CharacterRequirements requirements) {
		characterVitalsService.lockVitalsByCharacterId(characterId);
		boolean removed;
		do {
			removed = false;
			boolean mainHandTwoHanded = isMainHandTwoHanded(characterId);
			List<EquipmentEntity> equipped = equipmentRepository.findWithLockByCharacterId(characterId);
			for (EquipmentEntity row : equipped) {
				ItemInstanceEntity instance = requireOwnedInstance(characterId, row.getItemInstanceId());
				ItemDefinitionEntity definition = requireDefinition(instance.getItemDefinitionId());
				EquipmentValidator.Failure failure = EquipmentValidator.validate(
						definition.toData(),
						false,
						requirements,
						mainHandTwoHanded);
				if (failure != null) {
					equipmentRepository.delete(row);
					removed = true;
				}
			}
			if (removed) {
				equipmentRepository.flush();
			}
		}
		while (removed);
	}

	EquippedBonusesSnapshot equippedBonuses(UUID characterId) {
		List<ItemInstanceEntity> instances = itemInstanceRepository
				.findByOwnerCharacterIdOrderByCreatedAtAscIdAsc(characterId);
		Map<UUID, ItemDefinitionEntity> definitions = loadDefinitions(instances);
		return equippedBonuses(
				equippedItemIds(characterId),
				instances,
				definitions,
				loadCatalogModifiers(definitions.keySet()),
				loadAffixes(instances),
				affixCatalogService.load());
	}

	private EquippedBonusesSnapshot equippedBonuses(
			Map<EquipmentSlot, UUID> equipped,
			List<ItemInstanceEntity> instances,
			Map<UUID, ItemDefinitionEntity> definitions,
			Map<UUID, List<ItemStatCalculator.AppliedAffix>> catalogModifiers,
			Map<UUID, List<ItemInstanceAffixEntity>> affixesByInstance,
			AffixCatalog catalog) {
		Map<UUID, ItemInstanceEntity> instancesById = new HashMap<>();
		for (ItemInstanceEntity instance : instances) {
			instancesById.put(instance.getId(), instance);
		}
		ItemStats total = ItemStats.empty();
		ArmorCategory heaviestArmor = null;
		for (UUID itemId : equipped.values()) {
			if (itemId == null) {
				continue;
			}
			ItemInstanceEntity instance = instancesById.get(itemId);
			if (instance == null) {
				continue;
			}
			ItemDefinitionEntity definition = definitions.get(instance.getItemDefinitionId());
			if (definition == null) {
				definition = requireDefinition(instance.getItemDefinitionId());
			}
			total = total.plus(statsOf(instance, definition, catalogModifiers, affixesByInstance, catalog));
			heaviestArmor = ArmorCategory.heaviest(heaviestArmor, definition.getArmorCategory());
		}
		total = total.plusDodge(ItemBalance.armorDodge(heaviestArmor));
		return new EquippedBonusesSnapshot(
				total.weaponDamage(),
				total.armor(),
				total.accuracy(),
				total.dodge(),
				total.criticalChance(),
				total.strength(),
				total.agility(),
				total.endurance(),
				total.perception(),
				total.staminaCostReduction());
	}

	private InventoryView buildInventoryView(CharacterVitalsView vitals) {
		List<ItemInstanceEntity> instances = itemInstanceRepository
				.findByOwnerCharacterIdOrderByCreatedAtAscIdAsc(vitals.characterId());
		Map<UUID, ItemDefinitionEntity> definitions = loadDefinitions(instances);
		Map<UUID, List<ItemStatCalculator.AppliedAffix>> catalogModifiers = loadCatalogModifiers(definitions.keySet());
		Map<UUID, List<ItemInstanceAffixEntity>> affixesByInstance = loadAffixes(instances);
		AffixCatalog catalog = affixCatalogService.load();
		Map<EquipmentSlot, UUID> equippedBySlot = equippedItemIds(vitals.characterId());
		Set<UUID> equippedIds = new HashSet<>(equippedBySlot.values());
		Map<UUID, Integer> reservedQuantities = itemReservationQuery.reservedQuantities(
				instances.stream().map(ItemInstanceEntity::getId).toList());
		Map<UUID, ItemStats> statsByInstance = new HashMap<>();
		for (ItemInstanceEntity instance : instances) {
			ItemDefinitionEntity definition = definitions.get(instance.getItemDefinitionId());
			statsByInstance.put(instance.getId(), statsOf(instance, definition, catalogModifiers, affixesByInstance, catalog));
		}
		EquipmentValidator.CharacterRequirements requirements = requirementsOf(vitals);
		boolean mainHandTwoHanded = isTwoHanded(equippedBySlot.get(EquipmentSlot.MAIN_HAND), instances, definitions);

		List<InventoryItemView> items = instances.stream()
				.map(instance -> {
					ItemDefinitionEntity definition = definitions.get(instance.getItemDefinitionId());
					EquipmentSlot slot = definition.getType().isEquippable()
							? EquipmentSlot.forDefinition(definition.getEquipmentSlot(), definition.getType())
							: null;
					int listedQuantity = reservedQuantities.getOrDefault(instance.getId(), 0);
					int available = instance.getQuantity() - listedQuantity;
					List<RolledAffix> rolled = rolledAffixes(affixesByInstance.getOrDefault(instance.getId(), List.of()));
					List<ItemAffixView> affixViews = rolled.stream()
							.map(affix -> {
								var def = catalog.require(affix.affixCode());
								return new ItemAffixView(
										def.code(),
										def.kind(),
										def.displayName(),
										def.stat(),
										affix.magnitude());
							})
							.toList();
					boolean equipped = equippedIds.contains(instance.getId());
					boolean canEquip = slot != null && EquipmentValidator.canEquip(
							definition.toData(),
							listedQuantity > 0,
							requirements,
							mainHandTwoHanded);
					ItemComparisonView comparison = slot == null
							? null
							: comparison(
									slot,
									equippedBySlot.get(slot),
									statsByInstance.get(instance.getId()),
									statsByInstance);
					return new InventoryItemView(
							instance.getId(),
							definition.getId(),
							definition.getCode(),
							definition.getName(),
							ItemDisplayNames.compose(definition.getName(), rolled, catalog),
							definition.getDescription(),
							definition.getType(),
							instance.getRarity(),
							instance.getQuantity(),
							definition.getRequiredLevel(),
							definition.getRequiredStrength(),
							definition.getRequiredAgility(),
							definition.getRequiredEndurance(),
							definition.getRequiredPerception(),
							definition.getBaseValue(),
							MerchantPriceCalculator.merchantBuyPrice(
									definition.getBaseValue(),
									instance.getRarity(),
									rolled.size()),
							equipped,
							canEquip,
							definition.isTwoHanded(),
							instance.isLegacy(),
							slot,
							definition.getWeaponFamily(),
							definition.getArmorCategory(),
							isUsable(definition) && available > 0,
							listedQuantity,
							instance.getRolledWeaponDamage(),
							instance.getRolledArmorValue(),
							displayWeaponDamage(instance.getRolledWeaponDamage(), statsByInstance.get(instance.getId())),
							displayArmorValue(instance.getRolledArmorValue(), statsByInstance.get(instance.getId())),
							definition.getHealAmount(),
							statsByInstance.get(instance.getId()).accuracy(),
							statsByInstance.get(instance.getId()).criticalChance(),
							statsByInstance.get(instance.getId()).dodge(),
							statsByInstance.get(instance.getId()).strength(),
							statsByInstance.get(instance.getId()).agility(),
							statsByInstance.get(instance.getId()).endurance(),
							statsByInstance.get(instance.getId()).perception(),
							statsByInstance.get(instance.getId()).staminaCostReduction(),
							affixViews,
							comparison);
				})
				.toList();

		EquippedBonusesSnapshot bonuses = equippedBonuses(
				equippedBySlot,
				instances,
				definitions,
				catalogModifiers,
				affixesByInstance,
				catalog);
		DerivedCombatStats derivedStats = CharacterStatCalculator.calculate(
				vitals.strength(),
				vitals.agility(),
				vitals.perception(),
				bonuses.weaponDamage(),
				bonuses.armorValue(),
				bonuses.accuracy(),
				bonuses.dodge(),
				bonuses.criticalChance(),
				bonuses.strength(),
				bonuses.agility(),
				bonuses.endurance(),
				bonuses.perception());

		return new InventoryView(
				InventoryBalance.DEFAULT_CAPACITY,
				instances.size(),
				items,
				EquipmentView.from(equippedBySlot),
				derivedStats);
	}

	private ItemComparisonView comparison(
			EquipmentSlot slot,
			UUID equippedItemId,
			ItemStats candidate,
			Map<UUID, ItemStats> statsByInstance) {
		ItemStats equipped = equippedItemId == null
				? ItemStats.empty()
				: statsByInstance.getOrDefault(equippedItemId, ItemStats.empty());
		List<StatDeltaView> deltas = new ArrayList<>();
		addDelta(deltas, "Damage", equipped.weaponDamage(), candidate.weaponDamage());
		addDelta(deltas, "Armor", equipped.armor(), candidate.armor());
		addDelta(deltas, "Accuracy", equipped.accuracy(), candidate.accuracy());
		addDelta(deltas, "Critical", equipped.criticalChance(), candidate.criticalChance());
		addDelta(deltas, "Dodge", equipped.dodge(), candidate.dodge());
		addDelta(deltas, "Strength", equipped.strength(), candidate.strength());
		addDelta(deltas, "Agility", equipped.agility(), candidate.agility());
		addDelta(deltas, "Endurance", equipped.endurance(), candidate.endurance());
		addDelta(deltas, "Perception", equipped.perception(), candidate.perception());
		addDelta(deltas, "Stamina Cost", equipped.staminaCostReduction(), candidate.staminaCostReduction());
		return new ItemComparisonView(slot, equippedItemId, ComparisonVerdict.fromDeltas(deltas), deltas);
	}

	private static void addDelta(List<StatDeltaView> deltas, String stat, int equipped, int candidate) {
		if (equipped == candidate) {
			return;
		}
		deltas.add(new StatDeltaView(stat, equipped, candidate, candidate - equipped));
	}

	private ItemStats statsOf(
			ItemInstanceEntity instance,
			ItemDefinitionEntity definition,
			Map<UUID, List<ItemStatCalculator.AppliedAffix>> catalogModifiers,
			Map<UUID, List<ItemInstanceAffixEntity>> affixesByInstance,
			AffixCatalog catalog) {
		List<ItemStatCalculator.AppliedAffix> applied = affixesByInstance
				.getOrDefault(instance.getId(), List.of())
				.stream()
				.map(affix -> new ItemStatCalculator.AppliedAffix(
						catalog.require(affix.getAffixCode()).stat(),
						affix.getRolledMagnitude()))
				.toList();
		return ItemStatCalculator.calculate(
				instance.getRolledWeaponDamage(),
				instance.getRolledArmorValue(),
				catalogModifiers.getOrDefault(definition.getId(), List.of()),
				applied);
	}

	private Map<UUID, List<ItemStatCalculator.AppliedAffix>> loadCatalogModifiers(Set<UUID> definitionIds) {
		Map<UUID, List<ItemStatCalculator.AppliedAffix>> modifiers = new HashMap<>();
		if (definitionIds.isEmpty()) {
			return modifiers;
		}
		for (ItemDefinitionModifierEntity row : itemDefinitionModifierRepository.findByItemDefinitionIdIn(definitionIds)) {
			modifiers.computeIfAbsent(row.getItemDefinitionId(), key -> new ArrayList<>())
					.add(new ItemStatCalculator.AppliedAffix(row.getStat(), row.getMagnitude()));
		}
		return modifiers;
	}

	private Map<UUID, List<ItemInstanceAffixEntity>> loadAffixes(List<ItemInstanceEntity> instances) {
		List<UUID> ids = instances.stream().map(ItemInstanceEntity::getId).toList();
		Map<UUID, List<ItemInstanceAffixEntity>> byInstance = new HashMap<>();
		if (ids.isEmpty()) {
			return byInstance;
		}
		for (ItemInstanceAffixEntity affix : itemInstanceAffixRepository.findByItemInstanceIdIn(ids)) {
			byInstance.computeIfAbsent(affix.getItemInstanceId(), key -> new ArrayList<>()).add(affix);
		}
		return byInstance;
	}

	private static List<RolledAffix> rolledAffixes(List<ItemInstanceAffixEntity> entities) {
		return entities.stream()
				.map(entity -> new RolledAffix(
						entity.getAffixCode(),
						entity.getKind(),
						entity.getOrdinal(),
						entity.getRolledMagnitude()))
				.toList();
	}

	private void persistOwnedInstance(
			UUID characterId,
			ItemDefinitionEntity definition,
			GeneratedItem generated,
			boolean legacy,
			Instant now) {
		ItemInstanceEntity instance = new ItemInstanceEntity(
				UUID.randomUUID(),
				definition.getId(),
				characterId,
				1,
				false,
				generated.rarity(),
				generated.rolledWeaponDamage(),
				generated.rolledArmorValue(),
				legacy,
				now);
		itemInstanceRepository.saveAndFlush(instance);
		for (RolledAffix affix : generated.affixes()) {
			itemInstanceAffixRepository.save(new ItemInstanceAffixEntity(
					UUID.randomUUID(),
					instance.getId(),
					affix.kind(),
					affix.ordinal(),
					affix.affixCode(),
					affix.magnitude()));
		}
		itemInstanceAffixRepository.flush();
	}

	private GeneratedItem rollItem(ItemDefinitionEntity definition, AffixCatalog catalog) {
		if (!definition.getType().isEquippable()) {
			return new GeneratedItem(
					definition.getRarity(),
					definition.getWeaponDamage(),
					definition.getArmorValue(),
					List.of());
		}
		return ItemGenerator.generate(definition.toData(), catalog, randomProvider);
	}

	private static Integer displayWeaponDamage(Integer rolledWeapon, ItemStats stats) {
		if (stats == null) {
			return rolledWeapon;
		}
		if (rolledWeapon == null && stats.weaponDamage() == 0) {
			return null;
		}
		return stats.weaponDamage();
	}

	private static Integer displayArmorValue(Integer rolledArmor, ItemStats stats) {
		if (stats == null) {
			return rolledArmor;
		}
		if (rolledArmor == null && stats.armor() == 0) {
			return null;
		}
		return stats.armor();
	}

	private ItemInstanceEntity copyDefinitionInstance(
			UUID characterId,
			ItemDefinitionEntity definition,
			int quantity,
			boolean stackable,
			Instant now) {
		return new ItemInstanceEntity(
				UUID.randomUUID(),
				definition.getId(),
				characterId,
				quantity,
				stackable,
				definition.getRarity(),
				definition.getWeaponDamage(),
				definition.getArmorValue(),
				false,
				now);
	}

	private boolean isMainHandTwoHanded(UUID characterId) {
		return equipmentRepository.findWithLockByCharacterIdAndSlot(characterId, EquipmentSlot.MAIN_HAND)
				.map(equipped -> {
					ItemInstanceEntity instance = itemInstanceRepository.findById(equipped.getItemInstanceId())
							.orElse(null);
					if (instance == null) {
						return false;
					}
					return requireDefinition(instance.getItemDefinitionId()).isTwoHanded();
				})
				.orElse(false);
	}

	private static boolean isTwoHanded(
			UUID mainHandItemId,
			List<ItemInstanceEntity> instances,
			Map<UUID, ItemDefinitionEntity> definitions) {
		if (mainHandItemId == null) {
			return false;
		}
		for (ItemInstanceEntity instance : instances) {
			if (instance.getId().equals(mainHandItemId)) {
				ItemDefinitionEntity definition = definitions.get(instance.getItemDefinitionId());
				return definition != null && definition.isTwoHanded();
			}
		}
		return false;
	}

	private static EquipmentValidator.CharacterRequirements requirementsOf(CharacterVitalsView vitals) {
		return new EquipmentValidator.CharacterRequirements(
				vitals.level(),
				vitals.strength(),
				vitals.agility(),
				vitals.endurance(),
				vitals.perception());
	}

	private Map<EquipmentSlot, UUID> equippedItemIds(UUID characterId) {
		Map<EquipmentSlot, UUID> equipped = new EnumMap<>(EquipmentSlot.class);
		for (EquipmentEntity row : equipmentRepository.findByCharacterId(characterId)) {
			equipped.put(row.getSlot(), row.getItemInstanceId());
		}
		return equipped;
	}

	private Map<UUID, ItemDefinitionEntity> loadDefinitions(List<ItemInstanceEntity> instances) {
		Set<UUID> ids = new HashSet<>();
		for (ItemInstanceEntity instance : instances) {
			ids.add(instance.getItemDefinitionId());
		}
		Map<UUID, ItemDefinitionEntity> definitions = new HashMap<>();
		for (ItemDefinitionEntity definition : itemDefinitionRepository.findAllById(ids)) {
			definitions.put(definition.getId(), definition);
		}
		return definitions;
	}

	/**
	 * The single definition of "can be used", shared by {@link #use} and the inventory view so
	 * the client never has to re-derive the rule.
	 */
	private static boolean isUsable(ItemDefinitionEntity definition) {
		return definition.getType() == ItemType.CONSUMABLE && definition.getHealAmount() != null;
	}

	private int reservedQuantity(UUID itemInstanceId) {
		return itemReservationQuery.reservedQuantity(itemInstanceId);
	}

	private int unreservedQuantity(ItemInstanceEntity instance) {
		return Math.max(0, instance.getQuantity() - reservedQuantity(instance.getId()));
	}

	private ItemInstanceEntity requireOwnedInstance(UUID characterId, UUID itemInstanceId) {
		ItemInstanceEntity instance = itemInstanceRepository.findWithLockById(itemInstanceId)
				.orElseThrow(InventoryErrors::itemNotFound);
		if (!instance.getOwnerCharacterId().equals(characterId)) {
			throw InventoryErrors.itemNotOwned();
		}
		return instance;
	}

	private ItemDefinitionEntity requireDefinition(UUID definitionId) {
		return itemDefinitionRepository.findById(definitionId)
				.orElseThrow(() -> InventoryErrors.itemDefinitionMissing(definitionId.toString()));
	}

	private void ensureCapacity(UUID characterId, int slotsNeeded) {
		long used = itemInstanceRepository.countByOwnerCharacterId(characterId);
		if (!InventoryBalance.hasRoom((int) used, slotsNeeded)) {
			throw InventoryErrors.inventoryFull();
		}
	}

	record EquippedBonusesSnapshot(
			int weaponDamage,
			int armorValue,
			int accuracy,
			int dodge,
			int criticalChance,
			int strength,
			int agility,
			int endurance,
			int perception,
			int staminaCostReduction
	) {
	}
}
