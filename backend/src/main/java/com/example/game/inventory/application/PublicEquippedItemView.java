package com.example.game.inventory.application;

import java.util.List;

import com.example.game.inventory.domain.EquipmentSlot;
import com.example.game.item.domain.ItemRarity;

public record PublicEquippedItemView(
		EquipmentSlot slot,
		String code,
		String displayName,
		ItemRarity rarity,
		Integer weaponDamage,
		Integer armorValue,
		List<ItemAffixView> affixes
) {
}
