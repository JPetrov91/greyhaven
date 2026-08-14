package com.example.game.item.domain;

import java.util.List;

public final class ItemStatCalculator {

	private ItemStatCalculator() {
	}

	public static ItemStats calculate(
			Integer rolledWeaponDamage,
			Integer rolledArmorValue,
			ArmorCategory armorCategory,
			List<AppliedAffix> affixes) {
		int baseWeapon = rolledWeaponDamage == null ? 0 : rolledWeaponDamage;
		int armor = rolledArmorValue == null ? 0 : rolledArmorValue;
		int damagePercent = 0;
		int accuracy = 0;
		int criticalChance = 0;
		int dodge = 0;
		int strength = 0;
		int agility = 0;
		int endurance = 0;
		int perception = 0;
		int staminaCostReduction = 0;

		if (affixes != null) {
			for (AppliedAffix affix : affixes) {
				switch (affix.stat()) {
					case DAMAGE_PERCENT -> damagePercent += affix.magnitude();
					case ACCURACY -> accuracy += affix.magnitude();
					case CRIT_CHANCE -> criticalChance += affix.magnitude();
					case ARMOR -> armor += affix.magnitude();
					case STRENGTH -> strength += affix.magnitude();
					case AGILITY -> agility += affix.magnitude();
					case ENDURANCE -> endurance += affix.magnitude();
					case PERCEPTION -> perception += affix.magnitude();
					case DODGE -> dodge += affix.magnitude();
					case STAMINA_COST -> staminaCostReduction += affix.magnitude();
				}
			}
		}

		int weaponDamage = baseWeapon;
		if (damagePercent > 0 && baseWeapon > 0) {
			weaponDamage += (int) Math.round(baseWeapon * (damagePercent / 100.0));
		}
		return new ItemStats(
				weaponDamage,
				armor,
				accuracy,
				criticalChance,
				dodge,
				strength,
				agility,
				endurance,
				perception,
				staminaCostReduction);
	}

	public record AppliedAffix(AffixStat stat, int magnitude) {
	}
}
