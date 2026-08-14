package com.example.game.inventory.application;

import java.util.UUID;

import com.example.game.item.domain.ItemRarity;

public record OwnedItemSnapshot(
		UUID itemInstanceId,
		UUID itemDefinitionId,
		int quantity,
		int unreservedQuantity,
		boolean equipped,
		ItemRarity rarity,
		int affixCount
) {
}
