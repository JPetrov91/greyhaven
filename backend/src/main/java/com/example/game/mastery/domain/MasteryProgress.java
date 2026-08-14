package com.example.game.mastery.domain;

/**
 * Server-authoritative mastery XP progress within the current mastery level.
 */
public record MasteryProgress(
		int level,
		int totalExperience,
		int experienceIntoCurrentLevel,
		Integer experienceRequiredForNextLevel,
		Integer experienceRemaining,
		double progressPercent,
		boolean maxLevel
) {

	public static MasteryProgress from(int level, int totalExperience) {
		if (level < MasteryBalance.STARTING_LEVEL || level > MasteryBalance.MAX_LEVEL) {
			throw new IllegalArgumentException("level out of range");
		}
		if (totalExperience < 0) {
			throw new IllegalArgumentException("totalExperience must be non-negative");
		}
		if (level >= MasteryBalance.MAX_LEVEL) {
			return new MasteryProgress(level, totalExperience, 0, null, null, 100.0, true);
		}
		int intoCurrent = totalExperience - MasteryBalance.cumulativeXpForLevel(level);
		int required = MasteryBalance.xpToNextLevel(level);
		int remaining = Math.max(0, required - intoCurrent);
		double percent = required == 0
				? 100.0
				: Math.round(intoCurrent * 1000.0 / required) / 10.0;
		return new MasteryProgress(
				level,
				totalExperience,
				Math.max(0, intoCurrent),
				required,
				remaining,
				percent,
				false);
	}
}
