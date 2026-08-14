package com.example.game.inventory.application;

import java.util.List;
import java.util.UUID;

import com.example.game.item.domain.ItemRarity;
import com.example.game.item.domain.ItemType;
import com.example.game.item.domain.WeaponFamily;

public record MarketItemDisplay(
		UUID itemInstanceId,
		String displayName,
		ItemType itemType,
		ItemRarity rarity,
		WeaponFamily weaponFamily,
		int requiredLevel,
		List<ItemAffixView> affixes
) {
}
