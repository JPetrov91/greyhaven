package com.example.game.combat.api;

public record CombatTechniqueOptionResponse(
		String code,
		String name,
		String description,
		int staminaCost,
		String disabledReason
) {
}
