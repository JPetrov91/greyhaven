package com.example.game.pvp.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ArenaRatingCalculatorTest {

	@Test
	void equalRatingsSwapTheSameMagnitude() {
		ArenaRatingCalculator.RatingChange win = ArenaRatingCalculator.of(1000, 1000, true, 1.0);
		assertThat(win.attackerDelta()).isEqualTo(12);
		assertThat(win.defenderDelta()).isEqualTo(-12);
		assertThat(ArenaRatingCalculator.apply(1000, win.attackerDelta())).isEqualTo(1012);
	}

	@Test
	void repeatMultiplierZeroesRatingReward() {
		ArenaRatingCalculator.RatingChange change = ArenaRatingCalculator.of(1000, 1000, true, 0.0);
		assertThat(change.attackerDelta()).isZero();
		assertThat(change.defenderDelta()).isZero();
	}

	@Test
	void underdogWinAwardsMoreThanFavoriteWin() {
		int underdog = ArenaRatingCalculator.of(900, 1100, true, 1.0).attackerDelta();
		int favorite = ArenaRatingCalculator.of(1100, 900, true, 1.0).attackerDelta();
		assertThat(underdog).isGreaterThan(favorite);
	}

	@Test
	void ratingFloorIsHonored() {
		assertThat(ArenaRatingCalculator.apply(5, -20)).isEqualTo(PvPBalance.RATING_FLOOR);
	}
}
