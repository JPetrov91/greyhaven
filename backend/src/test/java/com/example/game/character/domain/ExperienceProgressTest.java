package com.example.game.character.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ExperienceProgressTest {

	@Test
	void currentLevelProgressIsDerivedFromTotalExperience() {
		ExperienceProgress progress = ExperienceProgress.from(11, 8470);

		assertThat(progress.level()).isEqualTo(11);
		assertThat(progress.totalExperience()).isEqualTo(8470);
		assertThat(progress.experienceIntoCurrentLevel()).isEqualTo(1240);
		assertThat(progress.experienceRequiredForNextLevel()).isEqualTo(2000);
		assertThat(progress.experienceRemaining()).isEqualTo(760);
		assertThat(progress.progressPercent()).isEqualTo(62.0);
		assertThat(progress.maxLevel()).isFalse();
	}

	@Test
	void multipleLevelCurveStillReportsIntoCurrentLevel() {
		ExperienceProgress afterJump = ExperienceProgress.from(4, 800);

		assertThat(afterJump.experienceIntoCurrentLevel()).isEqualTo(240);
		assertThat(afterJump.experienceRequiredForNextLevel()).isEqualTo(400);
		assertThat(afterJump.experienceRemaining()).isEqualTo(160);
	}

	@Test
	void maxLevelOmitsNextLevelRequirements() {
		ExperienceProgress progress = ExperienceProgress.from(
				CharacterBalance.MAX_LEVEL,
				ProgressionBalance.cumulativeXpForLevel(CharacterBalance.MAX_LEVEL));

		assertThat(progress.maxLevel()).isTrue();
		assertThat(progress.experienceRequiredForNextLevel()).isNull();
		assertThat(progress.experienceRemaining()).isNull();
		assertThat(progress.progressPercent()).isEqualTo(100.0);
		assertThat(progress.experienceIntoCurrentLevel()).isZero();
	}
}
