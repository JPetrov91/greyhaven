package com.example.game.combat.api;

public record CombatLootPreviewResponse(
		String itemName,
		int dropChancePercent
) {
}
