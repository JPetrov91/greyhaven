package com.example.game.inventory.application;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.character.application.StarterLoadoutGranter;
import com.example.game.item.domain.ItemCodes;
import com.example.game.item.infrastructure.ItemDefinitionEntity;
import com.example.game.item.infrastructure.ItemDefinitionRepository;
import com.example.game.item.infrastructure.ItemInstanceEntity;
import com.example.game.item.infrastructure.ItemInstanceRepository;

/**
 * Grants and equips the MVP starter loadout inside the character-creation transaction.
 */
@Component
public class GreyhavenStarterLoadoutGranter implements StarterLoadoutGranter {

	private final InventoryApplicationService inventoryApplicationService;
	private final ItemInstanceRepository itemInstanceRepository;
	private final ItemDefinitionRepository itemDefinitionRepository;

	public GreyhavenStarterLoadoutGranter(
			InventoryApplicationService inventoryApplicationService,
			ItemInstanceRepository itemInstanceRepository,
			ItemDefinitionRepository itemDefinitionRepository) {
		this.inventoryApplicationService = inventoryApplicationService;
		this.itemInstanceRepository = itemInstanceRepository;
		this.itemDefinitionRepository = itemDefinitionRepository;
	}

	@Override
	@Transactional
	public void grantStarterLoadout(UUID characterId) {
		inventoryApplicationService.grantItems(characterId, ItemCodes.RUSTY_SWORD, 1);
		inventoryApplicationService.grantItems(characterId, ItemCodes.WORN_LEATHER_ARMOR, 1);
		inventoryApplicationService.grantItems(characterId, ItemCodes.HEALING_POTION, 2);

		Map<UUID, ItemDefinitionEntity> definitions = itemDefinitionRepository.findAll().stream()
				.collect(Collectors.toMap(ItemDefinitionEntity::getId, Function.identity()));

		for (ItemInstanceEntity instance : itemInstanceRepository
				.findByOwnerCharacterIdOrderByCreatedAtAscIdAsc(characterId)) {
			ItemDefinitionEntity definition = definitions.get(instance.getItemDefinitionId());
			if (definition != null && definition.getType().isEquippable()) {
				inventoryApplicationService.equipOwnedItem(characterId, instance.getId());
			}
		}
	}
}
