package com.example.game.character.application;

/**
 * Equipment contribution to derived combat stats. Provided by the inventory module.
 */
public record EquippedBonuses(
		int weaponDamage,
		int armorValue,
		int accuracy,
		int dodge,
		int criticalChance,
		int strength,
		int agility,
		int endurance,
		int perception,
		int staminaCostReduction,
		int weaponDamageMin,
		int weaponDamageMax,
		int blockSoakMin,
		int blockSoakMax
) {

	public static EquippedBonuses none() {
		return new EquippedBonuses(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
	}
}
