package com.example.game.mastery.api;

import com.example.game.mastery.domain.MasteryProgress;

public record MasteryProgressResponse(
		int level,
		int totalExperience,
		int experienceIntoCurrentLevel,
		Integer experienceRequiredForNextLevel,
		Integer experienceRemaining,
		double progressPercent,
		boolean maxLevel
) {

	static MasteryProgressResponse from(MasteryProgress progress) {
		return new MasteryProgressResponse(
				progress.level(),
				progress.totalExperience(),
				progress.experienceIntoCurrentLevel(),
				progress.experienceRequiredForNextLevel(),
				progress.experienceRemaining(),
				progress.progressPercent(),
				progress.maxLevel());
	}
}
