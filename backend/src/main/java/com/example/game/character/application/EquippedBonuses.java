package com.example.game.character.application;

/**
 * Equipment contribution to derived combat stats. Provided by the inventory module.
 */
public record EquippedBonuses(int weaponDamage, int armorValue) {

	public static EquippedBonuses none() {
		return new EquippedBonuses(0, 0);
	}
}
