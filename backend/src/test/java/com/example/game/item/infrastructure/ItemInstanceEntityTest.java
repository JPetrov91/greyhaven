package com.example.game.item.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.example.game.item.domain.ItemRarity;

class ItemInstanceEntityTest {

	@Test
	void increaseQuantityRejectsIntegerOverflow() {
		ItemInstanceEntity stack = new ItemInstanceEntity(
				UUID.randomUUID(),
				UUID.randomUUID(),
				UUID.randomUUID(),
				Integer.MAX_VALUE,
				true,
				ItemRarity.COMMON,
				null,
				null,
				false,
				Instant.parse("2026-01-01T00:00:00Z"));

		assertThatThrownBy(() -> stack.increaseQuantity(1)).isInstanceOf(ArithmeticException.class);
		assertThat(stack.getQuantity()).isEqualTo(Integer.MAX_VALUE);
	}
}
