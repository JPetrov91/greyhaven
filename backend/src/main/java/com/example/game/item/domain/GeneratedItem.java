package com.example.game.item.domain;

import java.util.List;

public record GeneratedItem(
		ItemRarity rarity,
		Integer rolledWeaponDamage,
		Integer rolledArmorValue,
		List<RolledAffix> affixes
) {
}
