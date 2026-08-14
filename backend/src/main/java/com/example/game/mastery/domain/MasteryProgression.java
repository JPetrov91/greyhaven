package com.example.game.mastery.domain;

/**
 * Pure mastery XP / level math. Does not touch persistence.
 */
public final class MasteryProgression {

	private MasteryProgression() {
	}

	public record ProgressionResult(int level, int experience, int levelsGained) {
	}

	public static ProgressionResult applyExperience(int currentLevel, int currentExperience, int xpGain) {
		if (xpGain < 0) {
			throw new IllegalArgumentException("xpGain must be non-negative");
		}
		if (currentLevel < MasteryBalance.STARTING_LEVEL || currentLevel > MasteryBalance.MAX_LEVEL) {
			throw new IllegalArgumentException("currentLevel out of range");
		}
		if (currentExperience < 0) {
			throw new IllegalArgumentException("currentExperience must be non-negative");
		}

		int level = currentLevel;
		int experience = currentExperience + xpGain;
		int levelsGained = 0;

		while (level < MasteryBalance.MAX_LEVEL
				&& experience >= MasteryBalance.cumulativeXpForLevel(level + 1)) {
			level++;
			levelsGained++;
		}

		if (level >= MasteryBalance.MAX_LEVEL) {
			experience = Math.min(experience, MasteryBalance.cumulativeXpForLevel(MasteryBalance.MAX_LEVEL));
		}

		return new ProgressionResult(level, experience, levelsGained);
	}
}
