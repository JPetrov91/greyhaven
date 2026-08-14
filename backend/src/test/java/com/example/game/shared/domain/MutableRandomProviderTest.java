package com.example.game.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class MutableRandomProviderTest {

	@Test
	void outOfRangeScriptedValueIsRejectedInsteadOfSkipped() {
		MutableRandomProvider random = new MutableRandomProvider();
		random.queue(150);
		assertThatThrownBy(() -> random.nextInt(0, 99))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("150");
	}

	@Test
	void emptyQueueUsesFixedSeedFallback() {
		MutableRandomProvider first = new MutableRandomProvider();
		MutableRandomProvider second = new MutableRandomProvider();
		assertThat(first.nextInt(0, 99)).isEqualTo(second.nextInt(0, 99));
	}
}
