package com.example.game.inventory.domain;

import com.example.game.item.domain.ItemType;

public enum EquipmentSlot {
	WEAPON,
	ARMOR;

	public static EquipmentSlot forItemType(ItemType type) {
		return switch (type) {
			case WEAPON -> WEAPON;
			case ARMOR -> ARMOR;
			case CONSUMABLE, MATERIAL -> throw new IllegalArgumentException(
					"Item type " + type + " cannot be equipped");
		};
	}
}
