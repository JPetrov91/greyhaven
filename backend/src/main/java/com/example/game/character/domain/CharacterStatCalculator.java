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
		return calculate(strength, agility, perception, weaponDamage, armorValue, 0, 0, 0, 0, 0, 0, 0);
	}

	public static DerivedCombatStats calculate(
			int strength,
			int agility,
			int perception,
			int weaponDamage,
			int armorValue,
			int accuracyBonus,
			int dodgeBonus,
			int criticalChanceBonus,
			int strengthBonus,
			int agilityBonus,
			int enduranceBonus,
			int perceptionBonus) {
		int totalStrength = strength + strengthBonus;
		int totalAgility = agility + agilityBonus;
		int totalPerception = perception + perceptionBonus;
		int physicalDamage = (int) Math.round(
				weaponDamage + (totalStrength * CombatBalance.PHYSICAL_DAMAGE_PER_STRENGTH));
		int accuracy = (int) Math.round(
				CombatBalance.BASE_ACCURACY
						+ (totalPerception * CombatBalance.ACCURACY_PER_PERCEPTION)
						+ accuracyBonus);
		int dodge = (int) Math.round((totalAgility * CombatBalance.DODGE_PER_AGILITY) + dodgeBonus);
		int criticalChance = (int) Math.round(
				CombatBalance.BASE_CRITICAL_CHANCE
						+ (totalPerception * CombatBalance.CRITICAL_CHANCE_PER_PERCEPTION)
						+ criticalChanceBonus);
		return new DerivedCombatStats(
				physicalDamage,
				accuracy,
				dodge,
				criticalChance,
				armorValue);
	}
}
