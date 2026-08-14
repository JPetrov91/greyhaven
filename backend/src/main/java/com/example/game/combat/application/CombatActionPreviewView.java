package com.example.game.combat.application;

import com.example.game.combat.domain.CombatAction;

public record CombatActionPreviewView(
		CombatAction action,
		String techniqueCode,
		String name,
		String description,
		int staminaCost,
		Integer hitChancePercent,
		String disabledReason
) {
}
