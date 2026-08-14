package com.example.game.mastery.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MasteryProgressionTest {

	@Test
	void gainWithoutLevelUpStaysAtZero() {
		MasteryProgression.ProgressionResult result = MasteryProgression.applyExperience(0, 0, 12);

		assertThat(result.level()).isZero();
		assertThat(result.experience()).isEqualTo(12);
		assertThat(result.levelsGained()).isZero();
	}

	@Test
	void crossingFirstThresholdReachesLevelOne() {
		MasteryProgression.ProgressionResult result = MasteryProgression.applyExperience(0, 0, 80);

		assertThat(result.level()).isEqualTo(1);
		assertThat(result.experience()).isEqualTo(80);
		assertThat(result.levelsGained()).isEqualTo(1);
	}

	@Test
	void multiLevelJumpFromZeroToFour() {
		int xp = MasteryBalance.cumulativeXpForLevel(4);
		MasteryProgression.ProgressionResult result = MasteryProgression.applyExperience(0, 0, xp);

		assertThat(result.level()).isEqualTo(4);
		assertThat(result.experience()).isEqualTo(xp);
		assertThat(result.levelsGained()).isEqualTo(4);
	}

	@Test
	void maxLevelCapsExperience() {
		int cap = MasteryBalance.cumulativeXpForLevel(MasteryBalance.MAX_LEVEL);
		MasteryProgression.ProgressionResult result = MasteryProgression.applyExperience(
				MasteryBalance.MAX_LEVEL,
				cap,
				500);

		assertThat(result.level()).isEqualTo(MasteryBalance.MAX_LEVEL);
		assertThat(result.experience()).isEqualTo(cap);
		assertThat(result.levelsGained()).isZero();
	}

	@Test
	void zeroGainDoesNotUnlockFurtherLevelsWhenAlreadyCurrent() {
		MasteryProgression.ProgressionResult result = MasteryProgression.applyExperience(4, 720, 0);

		assertThat(result.level()).isEqualTo(4);
		assertThat(result.experience()).isEqualTo(720);
		assertThat(result.levelsGained()).isZero();
	}

	@Test
	void rejectsNegativeXp() {
		assertThatThrownBy(() -> MasteryProgression.applyExperience(0, 0, -1))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void configuredUnlockThresholdsAreEvenMilestonesThroughTen() {
		assertThat(MasteryBalance.unlockLevels()).containsExactly(2, 4, 6, 8, 10);
		assertThat(MasteryBalance.isUnlockLevel(2)).isTrue();
		assertThat(MasteryBalance.isUnlockLevel(3)).isFalse();
		assertThat(MasteryBalance.isUnlockLevel(10)).isTrue();
	}
}
