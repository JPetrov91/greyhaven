package com.example.game.item.domain;

public enum ItemType {
	WEAPON,
	ARMOR,
	CONSUMABLE,
	MATERIAL;

	public boolean isStackable() {
		return this == CONSUMABLE || this == MATERIAL;
	}

	public boolean isEquippable() {
		return this == WEAPON || this == ARMOR;
	}
}
