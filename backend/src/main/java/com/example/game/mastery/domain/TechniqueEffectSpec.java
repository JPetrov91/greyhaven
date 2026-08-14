package com.example.game.mastery.domain;

/**
 * Combat 2.0 resolves {@link TechniqueEffectSpec} values through {@code CombatEngine}.
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
