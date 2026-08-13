package com.example.game.item.application;

import java.util.UUID;

import com.example.game.item.domain.ItemRarity;
import com.example.game.item.domain.ItemType;

/**
 * Identity of an item definition as seen by other modules.
 */
public record ItemDefinitionView(
		UUID id,
		String code,
		String name,
		ItemType type,
		ItemRarity rarity
) {
}
