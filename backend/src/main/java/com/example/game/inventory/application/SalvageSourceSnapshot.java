package com.example.game.inventory.application;

import java.util.UUID;

import com.example.game.item.domain.ItemRarity;
import com.example.game.item.domain.ItemType;

public record SalvageSourceSnapshot(
		UUID itemInstanceId,
		UUID itemDefinitionId,
		String itemCode,
		ItemType type,
		ItemRarity rarity,
		boolean equipped,
		int listedQuantity
) {
}
