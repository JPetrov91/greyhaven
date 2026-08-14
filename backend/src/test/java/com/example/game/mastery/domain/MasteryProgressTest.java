package com.example.game.mastery.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class MasteryProgressTest {

	@Test
	void reportsProgressWithinLevel() {
		MasteryProgress progress = MasteryProgress.from(1, 100);

		assertThat(progress.experienceIntoCurrentLevel()).isEqualTo(20);
		assertThat(progress.experienceRequiredForNextLevel()).isEqualTo(120);
		assertThat(progress.experienceRemaining()).isEqualTo(100);
		assertThat(progress.maxLevel()).isFalse();
	}

	@Test
	void maxLevelIsComplete() {
		MasteryProgress progress = MasteryProgress.from(10, 15000);

		assertThat(progress.experienceRequiredForNextLevel()).isNull();
		assertThat(progress.progressPercent()).isEqualTo(100.0);
		assertThat(progress.maxLevel()).isTrue();
	}
}
