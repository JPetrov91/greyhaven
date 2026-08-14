package com.example.game.character.domain;

import com.example.game.shared.balance.GameBalance;
import com.example.game.shared.balance.GameBalanceCatalog;

/**
 * Isolated combat-derived-stat constants. Formulas live in {@link CharacterStatCalculator}.
 */
public final class CombatBalance {

	private static final GameBalance.Combat VALUES = GameBalanceCatalog.get().combat();

	public static final double PHYSICAL_DAMAGE_PER_STRENGTH = VALUES.physicalDamagePerStrength();

	public static final int BASE_ACCURACY = VALUES.baseAccuracy();
	public static final double ACCURACY_PER_PERCEPTION = VALUES.accuracyPerPerception();

	public static final double DODGE_PER_AGILITY = VALUES.dodgePerAgility();

	public static final double BASE_CRITICAL_CHANCE = VALUES.baseCriticalChance();
	public static final double CRITICAL_CHANCE_PER_PERCEPTION = VALUES.criticalChancePerPerception();

	private CombatBalance() {
	}
}
