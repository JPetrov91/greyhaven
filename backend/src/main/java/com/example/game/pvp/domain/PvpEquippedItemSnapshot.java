package com.example.game.pvp.domain;

import java.util.List;

import com.example.game.inventory.domain.EquipmentSlot;
import com.example.game.item.domain.ItemRarity;

public record PvpEquippedItemSnapshot(
		EquipmentSlot slot,
		String code,
		String displayName,
		ItemRarity rarity,
		Integer weaponDamage,
		Integer armorValue,
		List<PvpAffixSnapshot> affixes
) {
}
