package com.example.game.character.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.example.game.shared.balance.GameBalance;

class CharacterRecoveryTest {

	private static final Instant T0 = Instant.parse("2026-08-14T00:00:00Z");
	private static final GameBalance.RecoveryBand FLAT = new GameBalance.RecoveryBand(5, 20.0, 40.0);

	@Test
	void noElapsedTimeYieldsNoRecovery() {
		CharacterRecovery.Result result = CharacterRecovery.apply(50, 100, 20, 50, T0, T0, FLAT);

		assertThat(result.currentHealth()).isEqualTo(50);
		assertThat(result.currentStamina()).isEqualTo(20);
	}

	@Test
	void partialMinuteRecoversAFraction() {
		Instant later = T0.plusSeconds(30);
		CharacterRecovery.Result result = CharacterRecovery.apply(0, 100, 0, 50, T0, later, FLAT);

		assertThat(result.currentHealth()).isEqualTo(10);
		assertThat(result.currentStamina()).isEqualTo(10);
	}

	@Test
	void multipleMinutesRecoverLinearly() {
		Instant later = T0.plusSeconds(3 * 60);
		CharacterRecovery.Result result = CharacterRecovery.apply(0, 100, 0, 50, T0, later, FLAT);

		assertThat(result.currentHealth()).isEqualTo(60);
		assertThat(result.currentStamina()).isEqualTo(50);
	}

	@Test
	void healthAndStaminaAreCappedAtMaximum() {
		Instant later = T0.plusSeconds(60 * 60);
		CharacterRecovery.Result result = CharacterRecovery.apply(90, 100, 40, 50, T0, later, FLAT);

		assertThat(result.currentHealth()).isEqualTo(100);
		assertThat(result.currentStamina()).isEqualTo(50);
	}

	@Test
	void higherLevelsRecoverMoreSlowly() {
		Instant later = T0.plusSeconds(60);
		CharacterRecovery.Result early = CharacterRecovery.apply(1, 0, 100, 0, 50, T0, later);
		CharacterRecovery.Result late = CharacterRecovery.apply(21, 0, 100, 0, 50, T0, later);

		assertThat(early.currentHealth()).isEqualTo(20);
		assertThat(early.currentStamina()).isEqualTo(20);
		assertThat(late.currentHealth()).isEqualTo(7);
		assertThat(late.currentStamina()).isEqualTo(7);
	}

	@Test
	void repeatingTheSameElapsedWindowDoesNotStack() {
		Instant later = T0.plusSeconds(60);
		CharacterRecovery.Result first = CharacterRecovery.apply(0, 100, 0, 50, T0, later, FLAT);
		CharacterRecovery.Result second = CharacterRecovery.apply(
				first.currentHealth(),
				100,
				first.currentStamina(),
				50,
				later,
				later,
				FLAT);

		assertThat(second.currentHealth()).isEqualTo(first.currentHealth());
		assertThat(second.currentStamina()).isEqualTo(first.currentStamina());
	}

	@Test
	void clockInstantIsTheOnlyTimeSource() {
		Instant later = T0.plusSeconds(60);
		CharacterRecovery.Result result = CharacterRecovery.apply(0, 200, 0, 100, T0, later, FLAT);

		assertThat(result.currentHealth()).isEqualTo(40);
		assertThat(result.currentStamina()).isEqualTo(40);
	}
}
