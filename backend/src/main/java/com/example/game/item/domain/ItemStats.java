package com.example.game.item.domain;

public record ItemStats(
		int weaponDamage,
		int armor,
		int accuracy,
		int criticalChance,
		int dodge,
		int strength,
		int agility,
		int endurance,
		int perception,
		int staminaCostReduction
) {

	public static ItemStats empty() {
		return new ItemStats(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
	}

	public ItemStats plus(ItemStats other) {
		return new ItemStats(
				weaponDamage + other.weaponDamage,
				armor + other.armor,
				accuracy + other.accuracy,
				criticalChance + other.criticalChance,
				dodge + other.dodge,
				strength + other.strength,
				agility + other.agility,
				endurance + other.endurance,
				perception + other.perception,
				staminaCostReduction + other.staminaCostReduction);
	}

	public ItemStats plusDodge(int extraDodge) {
		if (extraDodge == 0) {
			return this;
		}
		return new ItemStats(
				weaponDamage,
				armor,
				accuracy,
				criticalChance,
				dodge + extraDodge,
				strength,
				agility,
				endurance,
				perception,
				staminaCostReduction);
	}
}
