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
import org.springframework.transaction.annotation.Transactional;

import com.example.game.character.application.CharacterVitalsService;
import com.example.game.character.application.CharacterVitalsView;
import com.example.game.character.domain.CharacterStatCalculator;
import com.example.game.character.domain.DerivedCombatStats;
import com.example.game.inventory.domain.EquipmentSlot;
import com.example.game.inventory.domain.InventoryBalance;
import com.example.game.inventory.infrastructure.EquipmentEntity;
import com.example.game.inventory.infrastructure.EquipmentRepository;
import com.example.game.item.domain.ItemType;
import com.example.game.item.infrastructure.ItemDefinitionEntity;
import com.example.game.item.infrastructure.ItemDefinitionRepository;
import com.example.game.item.infrastructure.ItemInstanceEntity;
import com.example.game.item.infrastructure.ItemInstanceRepository;

@Service
public class InventoryApplicationService {

	private final ItemDefinitionRepository itemDefinitionRepository;
	private final ItemInstanceRepository itemInstanceRepository;
	private final EquipmentRepository equipmentRepository;
	private final CharacterVitalsService characterVitalsService;
	private final Clock clock;

	public InventoryApplicationService(
			ItemDefinitionRepository itemDefinitionRepository,
			ItemInstanceRepository itemInstanceRepository,
			EquipmentRepository equipmentRepository,
			CharacterVitalsService characterVitalsService,
			Clock clock) {
		this.itemDefinitionRepository = itemDefinitionRepository;
		this.itemInstanceRepository = itemInstanceRepository;
		this.equipmentRepository = equipmentRepository;
		this.characterVitalsService = characterVitalsService;
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
		ItemInstanceEntity instance = requireOwnedInstance(vitals.characterId(), itemInstanceId);
		ItemDefinitionEntity definition = requireDefinition(instance.getItemDefinitionId());

		if (!definition.getType().isEquippable()) {
			throw InventoryErrors.itemNotEquippable();
		}
		if (vitals.level() < definition.getRequiredLevel()) {
			throw InventoryErrors.equipRequirementsNotMet();
		}

		EquipmentSlot slot = EquipmentSlot.forItemType(definition.getType());
		equipmentRepository.findWithLockByCharacterIdAndSlot(vitals.characterId(), slot)
				.ifPresentOrElse(
						existing -> existing.equip(instance.getId()),
						() -> equipmentRepository.saveAndFlush(new EquipmentEntity(
								UUID.randomUUID(),
								vitals.characterId(),
								slot,
								instance.getId())));

		return buildInventoryView(vitals);
	}

	@Transactional
	public InventoryView unequip(UUID accountId, UUID itemInstanceId) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
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
		ItemInstanceEntity instance = requireOwnedInstance(vitals.characterId(), itemInstanceId);
		ItemDefinitionEntity definition = requireDefinition(instance.getItemDefinitionId());

		if (definition.getType() != ItemType.CONSUMABLE || definition.getHealAmount() == null) {
			throw InventoryErrors.itemNotUsable();
		}
		if (equipmentRepository.existsByItemInstanceId(instance.getId())) {
			throw InventoryErrors.itemNotUsable();
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
	 * Grants items to a character, merging stackable definitions and enforcing capacity.
	 * Used by starter loadout and (later) loot systems.
	 */
	@Transactional
	public void grantItems(UUID characterId, String itemCode, int quantity) {
		if (quantity < 1) {
			throw new IllegalArgumentException("quantity must be positive");
		}
		ItemDefinitionEntity definition = itemDefinitionRepository.findByCode(itemCode)
				.orElseThrow(() -> InventoryErrors.itemDefinitionMissing(itemCode));

		if (definition.getType().isStackable()) {
			ItemInstanceEntity existing = itemInstanceRepository
					.findWithLockByOwnerCharacterIdAndItemDefinitionId(characterId, definition.getId())
					.orElse(null);
			if (existing != null) {
				existing.increaseQuantity(quantity);
				itemInstanceRepository.saveAndFlush(existing);
				return;
			}
			ensureCapacity(characterId, 1);
			itemInstanceRepository.saveAndFlush(new ItemInstanceEntity(
					UUID.randomUUID(),
					definition.getId(),
					characterId,
					quantity,
					Instant.now(clock)));
			return;
		}

		ensureCapacity(characterId, quantity);
		Instant now = Instant.now(clock);
		List<ItemInstanceEntity> created = new ArrayList<>(quantity);
		for (int i = 0; i < quantity; i++) {
			created.add(new ItemInstanceEntity(
					UUID.randomUUID(),
					definition.getId(),
					characterId,
					1,
					now));
		}
		itemInstanceRepository.saveAll(created);
		itemInstanceRepository.flush();
	}

	@Transactional
	public void equipOwnedItem(UUID characterId, UUID itemInstanceId) {
		ItemInstanceEntity instance = requireOwnedInstance(characterId, itemInstanceId);
		ItemDefinitionEntity definition = requireDefinition(instance.getItemDefinitionId());
		if (!definition.getType().isEquippable()) {
			throw InventoryErrors.itemNotEquippable();
		}
		EquipmentSlot slot = EquipmentSlot.forItemType(definition.getType());
		equipmentRepository.findWithLockByCharacterIdAndSlot(characterId, slot)
				.ifPresentOrElse(
						existing -> existing.equip(instance.getId()),
						() -> equipmentRepository.saveAndFlush(new EquipmentEntity(
								UUID.randomUUID(),
								characterId,
								slot,
								instance.getId())));
	}

	EquippedBonusesSnapshot equippedBonuses(UUID characterId) {
		Map<EquipmentSlot, UUID> equipped = equippedItemIds(characterId);
		int weaponDamage = 0;
		int armorValue = 0;

		UUID weaponId = equipped.get(EquipmentSlot.WEAPON);
		if (weaponId != null) {
			ItemInstanceEntity weapon = itemInstanceRepository.findById(weaponId).orElse(null);
			if (weapon != null) {
				ItemDefinitionEntity definition = requireDefinition(weapon.getItemDefinitionId());
				weaponDamage = definition.getWeaponDamage() == null ? 0 : definition.getWeaponDamage();
			}
		}

		UUID armorId = equipped.get(EquipmentSlot.ARMOR);
		if (armorId != null) {
			ItemInstanceEntity armor = itemInstanceRepository.findById(armorId).orElse(null);
			if (armor != null) {
				ItemDefinitionEntity definition = requireDefinition(armor.getItemDefinitionId());
				armorValue = definition.getArmorValue() == null ? 0 : definition.getArmorValue();
			}
		}

		return new EquippedBonusesSnapshot(weaponDamage, armorValue);
	}

	private InventoryView buildInventoryView(CharacterVitalsView vitals) {
		List<ItemInstanceEntity> instances = itemInstanceRepository
				.findByOwnerCharacterIdOrderByCreatedAtAscIdAsc(vitals.characterId());
		Map<UUID, ItemDefinitionEntity> definitions = loadDefinitions(instances);
		Map<EquipmentSlot, UUID> equippedBySlot = equippedItemIds(vitals.characterId());
		Set<UUID> equippedIds = new HashSet<>(equippedBySlot.values());

		List<InventoryItemView> items = instances.stream()
				.map(instance -> {
					ItemDefinitionEntity definition = definitions.get(instance.getItemDefinitionId());
					EquipmentSlot slot = definition.getType().isEquippable()
							? EquipmentSlot.forItemType(definition.getType())
							: null;
					boolean equipped = equippedIds.contains(instance.getId());
					return new InventoryItemView(
							instance.getId(),
							definition.getId(),
							definition.getCode(),
							definition.getName(),
							definition.getDescription(),
							definition.getType(),
							definition.getRarity(),
							instance.getQuantity(),
							definition.getRequiredLevel(),
							definition.getBaseValue(),
							equipped,
							equipped ? slot : null,
							definition.getWeaponDamage(),
							definition.getArmorValue(),
							definition.getHealAmount());
				})
				.toList();

		EquippedBonusesSnapshot bonuses = equippedBonuses(vitals.characterId());
		DerivedCombatStats derivedStats = CharacterStatCalculator.calculate(
				vitals.strength(),
				vitals.agility(),
				vitals.endurance(),
				vitals.perception(),
				bonuses.weaponDamage(),
				bonuses.armorValue());

		return new InventoryView(
				InventoryBalance.DEFAULT_CAPACITY,
				instances.size(),
				items,
				new EquipmentView(
						equippedBySlot.get(EquipmentSlot.WEAPON),
						equippedBySlot.get(EquipmentSlot.ARMOR)),
				derivedStats);
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

	record EquippedBonusesSnapshot(int weaponDamage, int armorValue) {
	}
}
