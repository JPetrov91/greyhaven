package com.example.game.combat.domain;

/**
 * Isolated combat action multipliers and costs.
 */
public final class ActionCombatBalance {

	public static final int QUICK_STAMINA_COST = 8;
	public static final double QUICK_DAMAGE_MULT = 1.0;
	public static final double QUICK_ACCURACY_MULT = 1.0;
	public static final int QUICK_CRIT_BONUS = 0;

	public static final int HEAVY_STAMINA_COST = 18;
	public static final double HEAVY_DAMAGE_MULT = 1.4;
	public static final double HEAVY_ACCURACY_MULT = 0.8;
	public static final int HEAVY_CRIT_BONUS = 0;

	public static final int PRECISE_STAMINA_COST = 12;
	public static final double PRECISE_DAMAGE_MULT = 0.8;
	public static final double PRECISE_ACCURACY_MULT = 1.25;
	public static final int PRECISE_CRIT_BONUS = 15;

	public static final int DEFEND_STAMINA_RESTORE = 8;
	public static final double DEFEND_DAMAGE_TAKEN_MULT = 0.5;

	public static final int RETREAT_BASE_CHANCE = 25;
	public static final int RETREAT_CHANCE_PER_AGILITY = 3;
	public static final int RETREAT_MAX_CHANCE = 90;

	public static final int MIN_HIT_CHANCE = 5;
	public static final int MAX_HIT_CHANCE = 95;

	public static final int CRITICAL_DAMAGE_MULT = 2;

	public static final int ENEMY_BASE_ACCURACY = 70;
	public static final int ENEMY_ACCURACY_PER_LEVEL = 2;
	public static final int ENEMY_DODGE_PER_LEVEL = 2;

	public static final int MIN_DAMAGE_AFTER_ARMOR = 1;

	private ActionCombatBalance() {
	}

	public static int staminaCost(CombatAction action) {
		return switch (action) {
			case QUICK_ATTACK -> QUICK_STAMINA_COST;
			case HEAVY_ATTACK -> HEAVY_STAMINA_COST;
			case PRECISE_ATTACK -> PRECISE_STAMINA_COST;
			case DEFEND, USE_POTION, RETREAT, USE_TECHNIQUE -> 0;
		};
	}

	public static double damageMultiplier(CombatAction action) {
		return switch (action) {
			case QUICK_ATTACK -> QUICK_DAMAGE_MULT;
			case HEAVY_ATTACK -> HEAVY_DAMAGE_MULT;
			case PRECISE_ATTACK -> PRECISE_DAMAGE_MULT;
			case DEFEND, USE_POTION, RETREAT, USE_TECHNIQUE -> 0.0;
		};
	}

	public static double accuracyMultiplier(CombatAction action) {
		return switch (action) {
			case QUICK_ATTACK -> QUICK_ACCURACY_MULT;
			case HEAVY_ATTACK -> HEAVY_ACCURACY_MULT;
			case PRECISE_ATTACK -> PRECISE_ACCURACY_MULT;
			case DEFEND, USE_POTION, RETREAT, USE_TECHNIQUE -> 1.0;
		};
	}

	public static int critBonus(CombatAction action) {
		return switch (action) {
			case PRECISE_ATTACK -> PRECISE_CRIT_BONUS;
			case QUICK_ATTACK -> QUICK_CRIT_BONUS;
			case HEAVY_ATTACK -> HEAVY_CRIT_BONUS;
			case DEFEND, USE_POTION, RETREAT, USE_TECHNIQUE -> 0;
		};
	}

	public static boolean isAttack(CombatAction action) {
		return action == CombatAction.QUICK_ATTACK
				|| action == CombatAction.HEAVY_ATTACK
				|| action == CombatAction.PRECISE_ATTACK;
	}

	public static int retreatChance(int agility) {
		int chance = RETREAT_BASE_CHANCE + (agility * RETREAT_CHANCE_PER_AGILITY);
		return Math.min(RETREAT_MAX_CHANCE, Math.max(0, chance));
	}

	public static int enemyAccuracy(int monsterLevel) {
		return ENEMY_BASE_ACCURACY + (monsterLevel * ENEMY_ACCURACY_PER_LEVEL);
	}

	public static int enemyDodge(int monsterLevel) {
		return monsterLevel * ENEMY_DODGE_PER_LEVEL;
	}

	public static int clampHitChance(int raw) {
		return Math.max(MIN_HIT_CHANCE, Math.min(MAX_HIT_CHANCE, raw));
	}
}
