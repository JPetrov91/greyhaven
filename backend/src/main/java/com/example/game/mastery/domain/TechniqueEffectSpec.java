package com.example.game.mastery.domain;

/**
 * Data-driven combat contract for Task 6. CombatEngine must not resolve these values yet.
 */
public record TechniqueEffectSpec(
		String effectCode,
		int staminaCost,
		int accuracyModifier,
		int damagePercentModifier,
		String appliesStatus,
		int statusStacks,
		int statusDurationRounds,
		String tags
) {
}
