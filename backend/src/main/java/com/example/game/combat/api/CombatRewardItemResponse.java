package com.example.game.combat.api;

public record CombatRewardItemResponse(
		String itemCode,
		String itemName,
		int quantity,
		boolean granted
) {
}
