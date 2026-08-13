package com.example.game.inventory.application;

import java.util.UUID;

import com.example.game.inventory.domain.EquipmentSlot;
import com.example.game.item.domain.ItemRarity;
import com.example.game.item.domain.ItemType;

public record InventoryItemView(
		UUID id,
		UUID definitionId,
		String code,
		String name,
		String description,
		ItemType type,
		ItemRarity rarity,
		int quantity,
		int requiredLevel,
		int baseValue,
		boolean equipped,
		EquipmentSlot equipmentSlot,
		boolean usable,
		Integer weaponDamage,
		Integer armorValue,
		Integer healAmount
) {
}
