package com.example.game.inventory.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class InventoryApplicationServiceBoundaryTest {

	@Test
	void applicationMethodsReturnInventoryViewNotApiDto() throws NoSuchMethodException {
		assertThat(InventoryApplicationService.class
				.getMethod("getInventory", UUID.class)
				.getReturnType())
				.isEqualTo(InventoryView.class);
		assertThat(InventoryApplicationService.class
				.getMethod("equip", UUID.class, UUID.class)
				.getReturnType())
				.isEqualTo(InventoryView.class);
		assertThat(InventoryApplicationService.class
				.getMethod("unequip", UUID.class, UUID.class)
				.getReturnType())
				.isEqualTo(InventoryView.class);
		assertThat(InventoryApplicationService.class
				.getMethod("use", UUID.class, UUID.class)
				.getReturnType())
				.isEqualTo(InventoryView.class);
	}
}
