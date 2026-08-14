package com.example.game.combat.api;

import com.example.game.combat.domain.CombatAction;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CombatActionRequest(
		@NotNull CombatAction action,
		@NotNull @PositiveOrZero Integer expectedRoundNumber,
		String techniqueCode
) {
}
