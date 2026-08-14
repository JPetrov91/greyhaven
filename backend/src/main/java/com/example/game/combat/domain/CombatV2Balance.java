package com.example.game.combat.domain;

import com.example.game.shared.balance.GameBalance;
import com.example.game.shared.balance.GameBalanceCatalog;

/**
 * Combat 2.0 numeric rules. Phase 1 action costs stay in {@link ActionCombatBalance}.
 */
public final class CombatV2Balance {

	private static final GameBalance.Combat VALUES = GameBalanceCatalog.get().combat();

	private CombatV2Balance() {
	}

	public static int minHitChance() {
		return VALUES.minHitChance();
	}

	public static int maxHitChance() {
		return VALUES.maxHitChance();
	}

	public static int clampHitChance(int raw) {
		return Math.max(VALUES.minHitChance(), Math.min(VALUES.maxHitChance(), raw));
	}

	public static int critChanceCap() {
		return VALUES.critChanceCap();
	}

	public static int clampCritChance(int raw) {
		return Math.max(0, Math.min(VALUES.critChanceCap(), raw));
	}

	public static int criticalDamageMult() {
		return VALUES.criticalDamageMult();
	}

	public static int armorK() {
		return VALUES.armorK();
	}

	public static int minDamageAfterArmor() {
		return VALUES.minDamageAfterArmor();
	}

	public static double armorBreakPerStack() {
		return VALUES.armorBreakPerStack();
	}

	public static double guardedDamageTakenMult() {
		return VALUES.guardedDamageTakenMult();
	}

	public static int bleedDamagePerStack() {
		return VALUES.bleedDamagePerStack();
	}

	public static int poisonDamagePerStack() {
		return VALUES.poisonDamagePerStack();
	}

	public static int bleedMaxStacks() {
		return VALUES.bleedMaxStacks();
	}

	public static int poisonMaxStacks() {
		return VALUES.poisonMaxStacks();
	}

	public static int armorBreakMaxStacks() {
		return VALUES.armorBreakMaxStacks();
	}

	public static int stunImmunityRounds() {
		return VALUES.stunImmunityRounds();
	}

	public static int offBalanceDodgePenalty() {
		return VALUES.offBalanceDodgePenalty();
	}

	public static int offBalanceAccuracyPenalty() {
		return VALUES.offBalanceAccuracyPenalty();
	}

	public static int counterDamagePercent() {
		return VALUES.counterDamagePercent();
	}

	public static int cleaveVsGuardedPercent() {
		return VALUES.cleaveVsGuardedPercent();
	}

	public static int advancedDamagePercent() {
		return VALUES.advancedDamagePercent();
	}

	public static int advancedHpThresholdPercent() {
		return VALUES.advancedHpThresholdPercent();
	}

	public static int playerStaminaRegen(int agility) {
		return VALUES.staminaRegenPerRound()
				+ (int) Math.round(agility * VALUES.staminaRegenPerAgility());
	}

	public static int enemyStaminaRegen() {
		return VALUES.enemyStaminaRegenPerRound();
	}

	public static int defendStaminaRestore() {
		return VALUES.defendStaminaRestore();
	}

	public static int enemyBasicStaminaCost() {
		return VALUES.enemyBasicStaminaCost();
	}

	public static int enemyHeavyStaminaCost() {
		return VALUES.enemyHeavyStaminaCost();
	}

	public static int enemyStatusAttackStaminaCost() {
		return VALUES.enemyStatusAttackStaminaCost();
	}

	public static int reducedStaminaCost(int baseCost, int staminaCostReduction) {
		return Math.max(0, baseCost - Math.max(0, staminaCostReduction));
	}
}
