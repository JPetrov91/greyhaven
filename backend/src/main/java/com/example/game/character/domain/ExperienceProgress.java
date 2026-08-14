package com.example.game.character.domain;

/**
 * Server-authoritative XP progress within the current level. Total lifetime XP stays on the
 * character row; this view is derived so the client never copies the XP table.
 */
public record ExperienceProgress(
		int level,
		int totalExperience,
		int experienceIntoCurrentLevel,
		Integer experienceRequiredForNextLevel,
		Integer experienceRemaining,
		double progressPercent,
		boolean maxLevel
) {

	public static ExperienceProgress from(int level, int totalExperience) {
		if (level < CharacterBalance.STARTING_LEVEL || level > CharacterBalance.MAX_LEVEL) {
			throw new IllegalArgumentException("level out of range");
		}
		if (totalExperience < 0) {
			throw new IllegalArgumentException("totalExperience must be non-negative");
		}
		if (level >= CharacterBalance.MAX_LEVEL) {
			return new ExperienceProgress(
					level,
					totalExperience,
					0,
					null,
					null,
					100.0,
					true);
		}
		int intoCurrent = totalExperience - ProgressionBalance.cumulativeXpForLevel(level);
		int required = ProgressionBalance.xpToNextLevel(level);
		int remaining = Math.max(0, required - intoCurrent);
		double percent = required == 0
				? 100.0
				: Math.round(intoCurrent * 1000.0 / required) / 10.0;
		return new ExperienceProgress(
				level,
				totalExperience,
				Math.max(0, intoCurrent),
				required,
				remaining,
				percent,
				false);
	}
}
