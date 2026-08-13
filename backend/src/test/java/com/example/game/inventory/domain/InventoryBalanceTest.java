package com.example.game.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InventoryBalanceTest {

	@Test
	void defaultCapacityIsFortySlots() {
		assertThat(InventoryBalance.DEFAULT_CAPACITY).isEqualTo(40);
	}

	@Test
	void hasRoomRejectsOverflow() {
		assertThat(InventoryBalance.hasRoom(39, 1)).isTrue();
		assertThat(InventoryBalance.hasRoom(40, 1)).isFalse();
		assertThat(InventoryBalance.hasRoom(38, 2)).isTrue();
		assertThat(InventoryBalance.hasRoom(39, 2)).isFalse();
	}
}
