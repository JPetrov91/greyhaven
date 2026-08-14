package com.example.game.combat.domain;

import java.util.UUID;

public record LootTableEntry(
		UUID itemDefinitionId,
		String itemCode,
		int dropChancePercent,
		int quantityMin,
		int quantityMax,
		boolean oncePerCharacter
) {
}
