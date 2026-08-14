package com.example.game.character.domain;

/**
 * Pure derived-stat calculation. Keeps combat formulas out of JPA entities and controllers.
 */
public final class CharacterStatCalculator {

	private CharacterStatCalculator() {
	}

	public static DerivedCombatStats calculate(
			int strength,
			int agility,
			int perception,
			int weaponDamage,
			int armorValue) {
		int physicalDamage = (int) Math.round(weaponDamage + (strength * CombatBalance.PHYSICAL_DAMAGE_PER_STRENGTH));
		int accuracy = (int) Math.round(
				CombatBalance.BASE_ACCURACY + (perception * CombatBalance.ACCURACY_PER_PERCEPTION));
		int dodge = (int) Math.round(agility * CombatBalance.DODGE_PER_AGILITY);
		int criticalChance = (int) Math.round(
				CombatBalance.BASE_CRITICAL_CHANCE
						+ (perception * CombatBalance.CRITICAL_CHANCE_PER_PERCEPTION));
		return new DerivedCombatStats(
				physicalDamage,
				accuracy,
				dodge,
				criticalChance,
				armorValue);
	}
}
