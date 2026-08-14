package com.example.game.character.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CharacterProgressionTest {

	@Test
	void singleLevelUpAwardsTwoAttributePoints() {
		CharacterProgression.ProgressionResult result = CharacterProgression.applyExperience(1, 0, 100);

		assertThat(result.level()).isEqualTo(2);
		assertThat(result.experience()).isEqualTo(100);
		assertThat(result.unspentAttributePointsGained()).isEqualTo(2);
	}

	@Test
	void multiLevelXpAwardsStackedPoints() {
		CharacterProgression.ProgressionResult result = CharacterProgression.applyExperience(1, 0, 800);

		assertThat(result.level()).isEqualTo(4);
		assertThat(result.experience()).isEqualTo(800);
		assertThat(result.unspentAttributePointsGained()).isEqualTo(6);
	}

	@Test
	void maxLevelCapsExperienceAndStopsAwardingPoints() {
		CharacterProgression.ProgressionResult result = CharacterProgression.applyExperience(
				CharacterBalance.MAX_LEVEL,
				ProgressionBalance.cumulativeXpForLevel(CharacterBalance.MAX_LEVEL),
				500);

		assertThat(result.level()).isEqualTo(CharacterBalance.MAX_LEVEL);
		assertThat(result.experience()).isEqualTo(ProgressionBalance.cumulativeXpForLevel(CharacterBalance.MAX_LEVEL));
		assertThat(result.unspentAttributePointsGained()).isZero();
	}

	@Test
	void zeroGainCatchUpAwardsPendingLevelsOnThePhase2Curve() {
		CharacterProgression.ProgressionResult result = CharacterProgression.applyExperience(5, 1600, 0);

		assertThat(result.level()).isEqualTo(6);
		assertThat(result.experience()).isEqualTo(1600);
		assertThat(result.unspentAttributePointsGained()).isEqualTo(2);
	}

	@Test
	void rejectsNegativeXp() {
		assertThatThrownBy(() -> CharacterProgression.applyExperience(1, 0, -1))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
