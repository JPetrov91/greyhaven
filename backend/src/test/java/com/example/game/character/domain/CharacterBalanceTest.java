package com.example.game.character.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CharacterBalanceTest {

	@Test
	void startingDefaultsMatchMvpSpec() {
		assertThat(CharacterBalance.STARTING_LEVEL).isEqualTo(1);
		assertThat(CharacterBalance.STARTING_STRENGTH).isEqualTo(5);
		assertThat(CharacterBalance.STARTING_AGILITY).isEqualTo(5);
		assertThat(CharacterBalance.STARTING_ENDURANCE).isEqualTo(5);
		assertThat(CharacterBalance.STARTING_PERCEPTION).isEqualTo(5);
		assertThat(CharacterBalance.STARTING_GOLD).isEqualTo(100);
	}

	@Test
	void derivedVitalsUsePhase2Formulas() {
		assertThat(CharacterBalance.maxHealth(5, 1)).isEqualTo(165);
		assertThat(CharacterBalance.maxStamina(5, 5)).isEqualTo(85);
	}

	@Test
	void defeatRecoveryRestoresHalfOfMaximum() {
		assertThat(CharacterBalance.DEFEAT_RECOVERY_PERCENT).isEqualTo(50);
		assertThat(CharacterBalance.defeatRecovery(160)).isEqualTo(80);
		assertThat(CharacterBalance.defeatRecovery(1)).isEqualTo(1);
	}
}
