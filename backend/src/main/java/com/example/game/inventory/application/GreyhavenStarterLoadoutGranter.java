package com.example.game.inventory.application;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.character.application.StarterLoadoutGranter;
import com.example.game.item.application.ItemCatalogService;
import com.example.game.item.application.ItemDefinitionView;
import com.example.game.item.domain.ItemCodes;
import com.example.game.item.infrastructure.ItemInstanceEntity;
import com.example.game.item.infrastructure.ItemInstanceRepository;
import com.example.game.telemetry.application.GameTelemetry;
import com.example.game.telemetry.application.GameTelemetryRecorder;
import com.example.game.telemetry.domain.ItemCreateSource;

/**
 * Grants and equips the MVP starter loadout inside the character-creation transaction.
 */
@Component
public class GreyhavenStarterLoadoutGranter implements StarterLoadoutGranter {

	private final InventoryApplicationService inventoryApplicationService;
	private final ItemInstanceRepository itemInstanceRepository;
	private final ItemCatalogService itemCatalogService;
	private final GameTelemetryRecorder gameTelemetryRecorder;

	public GreyhavenStarterLoadoutGranter(
			InventoryApplicationService inventoryApplicationService,
			ItemInstanceRepository itemInstanceRepository,
			ItemCatalogService itemCatalogService,
			GameTelemetryRecorder gameTelemetryRecorder) {
		this.inventoryApplicationService = inventoryApplicationService;
		this.itemInstanceRepository = itemInstanceRepository;
		this.itemCatalogService = itemCatalogService;
		this.gameTelemetryRecorder = gameTelemetryRecorder;
	}

	@Override
	@Transactional
	public void grantStarterLoadout(UUID characterId) {
		inventoryApplicationService.grantCatalogExact(characterId, ItemCodes.WORN_LEATHER_ARMOR, 1);
		inventoryApplicationService.grantItems(characterId, ItemCodes.HEALING_POTION, 2);

		List<ItemInstanceEntity> instances = itemInstanceRepository
				.findByOwnerCharacterIdOrderByCreatedAtAscIdAsc(characterId);
		Map<UUID, ItemDefinitionView> definitions = itemCatalogService.findByIds(
				instances.stream().map(ItemInstanceEntity::getItemDefinitionId).toList());
		for (ItemInstanceEntity instance : instances) {
			ItemDefinitionView definition = definitions.get(instance.getItemDefinitionId());
			if (definition != null) {
				GameTelemetry.itemCreated(
						gameTelemetryRecorder,
						characterId,
						definition.code(),
						definition.rarity(),
						instance.getQuantity(),
						ItemCreateSource.STARTER);
			}
		}

		for (ItemInstanceEntity instance : instances) {
			ItemDefinitionView definition = definitions.get(instance.getItemDefinitionId());
			if (definition != null && definition.type().isEquippable()) {
				inventoryApplicationService.equipOwnedItem(characterId, instance.getId());
			}
		}
	}
}
