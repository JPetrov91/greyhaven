package com.example.game.combat.domain;

/**
 * Snapshot of derived player combat stats used by {@link CombatEngine}.
 */
public record CombatantStats(
		int physicalDamage,
		int accuracy,
		int dodge,
		int criticalChance,
		int armor,
		int agility,
		int weaponDamageMin,
		int weaponDamageMax,
		double strengthDamage,
		int blockSoakMin,
		int blockSoakMax
) {

	public CombatantStats(
			int physicalDamage,
			int accuracy,
			int dodge,
			int criticalChance,
			int armor,
			int agility) {
		this(physicalDamage, accuracy, dodge, criticalChance, armor, agility, 0, 0, 0, 0, 0);
	}

	public boolean rollsWeaponRange() {
		return weaponDamageMax > weaponDamageMin && weaponDamageMin > 0;
	}

	public boolean hasBlockSoak() {
		return blockSoakMax > 0 && blockSoakMax >= blockSoakMin;
	}
}
