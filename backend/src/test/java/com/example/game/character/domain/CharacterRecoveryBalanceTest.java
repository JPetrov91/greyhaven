package com.example.game.character.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CharacterRecoveryBalanceTest {

	@Test
	void recoveryRatesComeFromConfigurableLevelBands() {
		assertThat(CharacterRecoveryBalance.ratesForLevel(1).healthPercentPerMinute()).isEqualTo(20.0);
		assertThat(CharacterRecoveryBalance.ratesForLevel(1).staminaPercentPerMinute()).isEqualTo(40.0);
		assertThat(CharacterRecoveryBalance.ratesForLevel(5).healthPercentPerMinute()).isEqualTo(20.0);

		assertThat(CharacterRecoveryBalance.ratesForLevel(6).healthPercentPerMinute()).isEqualTo(15.0);
		assertThat(CharacterRecoveryBalance.ratesForLevel(10).staminaPercentPerMinute()).isEqualTo(30.0);

		assertThat(CharacterRecoveryBalance.ratesForLevel(11).healthPercentPerMinute()).isEqualTo(10.0);
		assertThat(CharacterRecoveryBalance.ratesForLevel(20).staminaPercentPerMinute()).isEqualTo(20.0);

		assertThat(CharacterRecoveryBalance.ratesForLevel(21).healthPercentPerMinute()).isEqualTo(7.5);
		assertThat(CharacterRecoveryBalance.ratesForLevel(30).staminaPercentPerMinute()).isEqualTo(15.0);
	}
}
