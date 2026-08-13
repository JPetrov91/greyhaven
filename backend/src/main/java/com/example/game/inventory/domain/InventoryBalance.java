package com.example.game.inventory.domain;

/**
 * Isolated inventory capacity rules for the MVP.
 */
public final class InventoryBalance {

	public static final int DEFAULT_CAPACITY = 40;

	private InventoryBalance() {
	}

	public static boolean hasRoom(int usedSlots, int slotsNeeded) {
		return usedSlots + slotsNeeded <= DEFAULT_CAPACITY;
	}
}
