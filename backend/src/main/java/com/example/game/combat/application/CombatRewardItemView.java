package com.example.game.combat.application;

public record CombatRewardItemView(
		String itemCode,
		String itemName,
		int quantity,
		boolean granted
) {
}
