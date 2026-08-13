package com.example.game.inventory.api;

import java.util.UUID;

public record InventoryItemResponse(
		UUID id,
		UUID definitionId,
		String code,
		String name,
		String description,
		String type,
		String rarity,
		int quantity,
		int requiredLevel,
		int baseValue,
		boolean equipped,
		String equipmentSlot,
		boolean usable,
		Integer weaponDamage,
		Integer armorValue,
		Integer healAmount
) {
}
