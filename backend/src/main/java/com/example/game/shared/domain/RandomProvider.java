package com.example.game.shared.domain;

/**
 * Controllable randomness for combat, loot, and encounters. Tests inject scripted sequences.
 */
public interface RandomProvider {

	/**
	 * Inclusive bounds.
	 */
	int nextInt(int minInclusive, int maxInclusive);

	/**
	 * Chance in the closed range {@code [0, 100]}. Returns true when the roll is strictly less
	 * than {@code percentChance}.
	 */
	default boolean chancePercent(int percentChance) {
		if (percentChance <= 0) {
			return false;
		}
		if (percentChance >= 100) {
			return true;
		}
		return nextInt(0, 99) < percentChance;
	}
}
