package com.example.game.inventory.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.example.game.inventory.domain.EquipmentSlot;
import com.example.game.inventory.infrastructure.EquipmentEntity;
import com.example.game.inventory.infrastructure.EquipmentRepository;
import com.example.game.item.application.ItemCatalogService;
import com.example.game.item.application.ItemDefinitionView;
import com.example.game.item.domain.ItemType;
import com.example.game.item.domain.WeaponFamily;
import com.example.game.item.infrastructure.ItemInstanceEntity;
import com.example.game.item.infrastructure.ItemInstanceRepository;

@Component
public class InventoryEquippedWeaponQuery implements EquippedWeaponQuery {

	private final EquipmentRepository equipmentRepository;
	private final ItemInstanceRepository itemInstanceRepository;
	private final ItemCatalogService itemCatalogService;

	public InventoryEquippedWeaponQuery(
			EquipmentRepository equipmentRepository,
			ItemInstanceRepository itemInstanceRepository,
			ItemCatalogService itemCatalogService) {
		this.equipmentRepository = equipmentRepository;
		this.itemInstanceRepository = itemInstanceRepository;
		this.itemCatalogService = itemCatalogService;
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
		ItemDefinitionView definition = itemCatalogService.findByIds(
				List.of(instance.get().getItemDefinitionId()))
				.get(instance.get().getItemDefinitionId());
		if (definition == null || definition.type() != ItemType.WEAPON) {
			return Optional.empty();
		}
		return Optional.ofNullable(definition.weaponFamily());
	}
}
