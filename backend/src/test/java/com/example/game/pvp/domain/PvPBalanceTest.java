package com.example.game.pvp.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PvPBalanceTest {

	@Test
	void repeatMultiplierAndForfeitZeroMarks() {
		assertThat(PvPBalance.marksAwarded(true, false, 1.0)).isEqualTo(8);
		assertThat(PvPBalance.marksAwarded(false, false, 1.0)).isEqualTo(2);
		assertThat(PvPBalance.marksAwarded(true, false, 0.0)).isZero();
		assertThat(PvPBalance.marksAwarded(false, true, 1.0)).isZero();
		assertThat(PvPBalance.marksAwarded(true, true, 1.0)).isZero();
	}
}
