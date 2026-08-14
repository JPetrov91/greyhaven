package com.example.game.character.api;

public record ProgressionResponse(
		int level,
		int totalExperience,
		int experienceIntoCurrentLevel,
		Integer experienceRequiredForNextLevel,
		Integer experienceRemaining,
		double progressPercent,
		boolean maxLevel
) {
}
