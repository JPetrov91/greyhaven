package com.example.game.inventory.domain;

import com.example.game.item.domain.ItemType;

public enum EquipmentSlot {
	HEAD,
	CHEST,
	HANDS,
	LEGS,
	FEET,
	MAIN_HAND,
	OFF_HAND,
	AMULET,
	RING;

	public static EquipmentSlot forItemType(ItemType type) {
		return switch (type) {
			case WEAPON -> MAIN_HAND;
			case ARMOR -> CHEST;
			case CONSUMABLE, MATERIAL -> throw new IllegalArgumentException(
					"Item type " + type + " cannot be equipped");
		};
	}

	/**
	 * Prefers the persisted Phase 2 slot when present; otherwise maps the Phase 1 item type.
	 */
	public static EquipmentSlot forDefinition(EquipmentSlot stored, ItemType type) {
		if (stored != null) {
			return stored;
		}
		return forItemType(type);
	}
}
