package com.example.game.character.domain;

/**
 * Pure XP / level-up math. Does not touch persistence.
 */
public final class CharacterProgression {

	private CharacterProgression() {
	}

	public record ProgressionResult(int level, int experience, int unspentAttributePointsGained) {
	}

	/**
	 * Applies {@code xpGain} to the current totals and returns the new level / experience /
	 * attribute points gained from any level-ups (capped at {@link CharacterBalance#MAX_LEVEL}).
	 */
	public static ProgressionResult applyExperience(int currentLevel, int currentExperience, int xpGain) {
		if (xpGain < 0) {
			throw new IllegalArgumentException("xpGain must be non-negative");
		}
		if (currentLevel < CharacterBalance.STARTING_LEVEL || currentLevel > CharacterBalance.MAX_LEVEL) {
			throw new IllegalArgumentException("currentLevel out of range");
		}
		if (currentExperience < 0) {
			throw new IllegalArgumentException("currentExperience must be non-negative");
		}

		int level = currentLevel;
		int experience = currentExperience + xpGain;
		int pointsGained = 0;

		while (level < CharacterBalance.MAX_LEVEL
				&& experience >= ProgressionBalance.cumulativeXpForLevel(level + 1)) {
			level++;
			pointsGained += ProgressionBalance.ATTRIBUTE_POINTS_PER_LEVEL;
		}

		if (level >= CharacterBalance.MAX_LEVEL) {
			experience = Math.min(experience, ProgressionBalance.cumulativeXpForLevel(CharacterBalance.MAX_LEVEL));
		}

		return new ProgressionResult(level, experience, pointsGained);
	}
}
