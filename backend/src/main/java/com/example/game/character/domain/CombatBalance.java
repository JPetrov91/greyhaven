package com.example.game.character.domain;

/**
 * Isolated combat-derived-stat constants. Formulas live in {@link CharacterStatCalculator}.
 */
public final class CombatBalance {

	public static final double PHYSICAL_DAMAGE_PER_STRENGTH = 1.5;

	public static final int BASE_ACCURACY = 70;
	public static final int ACCURACY_PER_PERCEPTION = 2;

	public static final double DODGE_PER_AGILITY = 1.5;

	public static final double BASE_CRITICAL_CHANCE = 5.0;
	public static final double CRITICAL_CHANCE_PER_PERCEPTION = 0.5;

	private CombatBalance() {
	}
}
