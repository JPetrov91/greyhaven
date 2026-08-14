package com.example.game.combat.application;

public record CombatTechniqueOptionView(
		String code,
		String name,
		String description,
		int staminaCost,
		String disabledReason
) {
}
