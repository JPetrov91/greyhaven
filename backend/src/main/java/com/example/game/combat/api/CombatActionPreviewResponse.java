package com.example.game.combat.api;

import com.example.game.combat.domain.CombatAction;

public record CombatActionPreviewResponse(
		CombatAction action,
		String techniqueCode,
		String name,
		String description,
		int staminaCost,
		Integer hitChancePercent,
		String disabledReason
) {
}
