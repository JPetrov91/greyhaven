package com.example.game.character.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ProgressionBalanceTest {

	@Test
	void respecIsFreeThroughLevelTen() {
		assertThat(ProgressionBalance.respecGoldCost(1)).isZero();
		assertThat(ProgressionBalance.respecGoldCost(10)).isZero();
	}

	@Test
	void respecCostsScaleAfterLevelTen() {
		assertThat(ProgressionBalance.respecGoldCost(11)).isEqualTo(500 + 11 * 100);
		assertThat(ProgressionBalance.respecGoldCost(30)).isEqualTo(500 + 30 * 100);
	}
}
