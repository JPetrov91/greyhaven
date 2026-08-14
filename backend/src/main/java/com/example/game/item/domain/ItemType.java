package com.example.game.item.domain;

public enum ItemType {
	WEAPON,
	ARMOR,
	CONSUMABLE,
	MATERIAL,
	ACCESSORY;

	public boolean isStackable() {
		return this == CONSUMABLE || this == MATERIAL;
	}

	public boolean isEquippable() {
		return this == WEAPON || this == ARMOR || this == ACCESSORY;
	}
}
