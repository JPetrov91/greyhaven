package com.example.game.inventory.domain;

import com.example.game.shared.balance.GameBalanceCatalog;

/**
 * Isolated inventory capacity rules for the MVP.
 */
public final class InventoryBalance {

	public static final int DEFAULT_CAPACITY = GameBalanceCatalog.get().inventory().defaultCapacity();

	private InventoryBalance() {
	}

	public static boolean hasRoom(int usedSlots, int slotsNeeded) {
		return usedSlots + slotsNeeded <= DEFAULT_CAPACITY;
	}
}
