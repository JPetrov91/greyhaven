package com.example.game.inventory.application;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.example.game.inventory.domain.EquipmentSlot;
import com.example.game.inventory.infrastructure.EquipmentEntity;
import com.example.game.inventory.infrastructure.EquipmentRepository;
import com.example.game.item.domain.ItemType;
import com.example.game.item.domain.WeaponFamily;
import com.example.game.item.infrastructure.ItemDefinitionEntity;
import com.example.game.item.infrastructure.ItemDefinitionRepository;
import com.example.game.item.infrastructure.ItemInstanceEntity;
import com.example.game.item.infrastructure.ItemInstanceRepository;

@Component
public class InventoryEquippedWeaponQuery implements EquippedWeaponQuery {

	private final EquipmentRepository equipmentRepository;
	private final ItemInstanceRepository itemInstanceRepository;
	private final ItemDefinitionRepository itemDefinitionRepository;

	public InventoryEquippedWeaponQuery(
			EquipmentRepository equipmentRepository,
			ItemInstanceRepository itemInstanceRepository,
			ItemDefinitionRepository itemDefinitionRepository) {
		this.equipmentRepository = equipmentRepository;
		this.itemInstanceRepository = itemInstanceRepository;
		this.itemDefinitionRepository = itemDefinitionRepository;
	}

	@Override
	public Optional<WeaponFamily> mainHandFamily(UUID characterId) {
		Optional<EquipmentEntity> equipped = equipmentRepository.findByCharacterIdAndSlot(
				characterId,
				EquipmentSlot.MAIN_HAND);
		if (equipped.isEmpty()) {
			return Optional.empty();
		}
		Optional<ItemInstanceEntity> instance = itemInstanceRepository.findById(equipped.get().getItemInstanceId());
		if (instance.isEmpty()) {
			return Optional.empty();
		}
		Optional<ItemDefinitionEntity> definition = itemDefinitionRepository.findById(
				instance.get().getItemDefinitionId());
		if (definition.isEmpty()) {
			return Optional.empty();
		}
		ItemDefinitionEntity item = definition.get();
		if (item.getType() != ItemType.WEAPON) {
			return Optional.empty();
		}
		return Optional.ofNullable(item.getWeaponFamily());
	}
}
