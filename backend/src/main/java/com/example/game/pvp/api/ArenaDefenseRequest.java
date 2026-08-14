package com.example.game.pvp.api;

import com.example.game.combat.domain.CombatAction;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ArenaDefenseRequest(
		@NotNull CombatAction preferredAction,
		String preferredTechniqueCode,
		@Min(0) @Max(100) int healWhenHpPercentBelow,
		@Min(0) @Max(100) int defendWhenStaminaPercentBelow,
		@Min(0) @Max(100) int finisherWhenEnemyHpPercentBelow,
		String finisherTechniqueCode
) {
}
