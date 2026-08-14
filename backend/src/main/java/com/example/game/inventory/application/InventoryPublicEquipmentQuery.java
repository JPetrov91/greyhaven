package com.example.game.inventory.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InventoryPublicEquipmentQuery implements PublicEquipmentQuery {

	private final InventoryApplicationService inventoryApplicationService;

	public InventoryPublicEquipmentQuery(InventoryApplicationService inventoryApplicationService) {
		this.inventoryApplicationService = inventoryApplicationService;
	}

	@Override
	@Transactional(readOnly = true)
	public List<PublicEquippedItemView> equippedItems(UUID characterId) {
		return inventoryApplicationService.publicEquippedItems(characterId);
	}

	@Override
	@Transactional(readOnly = true)
	public HealingPotionStock healingPotionStock(UUID characterId) {
		return inventoryApplicationService.healingPotionStock(characterId);
	}
}
