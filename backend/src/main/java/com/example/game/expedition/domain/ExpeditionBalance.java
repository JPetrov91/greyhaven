package com.example.game.expedition.domain;

import java.time.Duration;

import com.example.game.shared.balance.GameBalance;
import com.example.game.shared.balance.GameBalanceCatalog;

/**
 * Balance knobs for Forest Patrol and strategy modifiers.
 */
public final class ExpeditionBalance {

	private static final GameBalance.Expedition VALUES = GameBalanceCatalog.get().expedition();

	public static final Duration FOREST_PATROL_DURATION = Duration.ofMinutes(VALUES.forestPatrolDurationMinutes());

	private ExpeditionBalance() {
	}

	public static int injuryChancePercent(ExpeditionStrategy strategy) {
		return knobs(strategy).injuryChancePercent();
	}

	public static int injuryDamageMin(ExpeditionStrategy strategy) {
		return knobs(strategy).injuryDamageMin();
	}

	public static int injuryDamageMax(ExpeditionStrategy strategy) {
		return knobs(strategy).injuryDamageMax();
	}

	public static int goldMin(ExpeditionStrategy strategy) {
		return knobs(strategy).goldMin();
	}

	public static int goldMax(ExpeditionStrategy strategy) {
		return knobs(strategy).goldMax();
	}

	public static int xpMin(ExpeditionStrategy strategy) {
		return knobs(strategy).xpMin();
	}

	public static int xpMax(ExpeditionStrategy strategy) {
		return knobs(strategy).xpMax();
	}

	/**
	 * Chance of finding nothing meaningful (still may roll tiny residual XP/gold of 0).
	 */
	public static int emptyHaulChancePercent(ExpeditionStrategy strategy) {
		return knobs(strategy).emptyHaulChancePercent();
	}

	public static int materialChancePercent(ExpeditionStrategy strategy) {
		return knobs(strategy).materialChancePercent();
	}

	public static int potionChancePercent(ExpeditionStrategy strategy) {
		return knobs(strategy).potionChancePercent();
	}

	public static int commonGearChancePercent(ExpeditionStrategy strategy) {
		return knobs(strategy).commonGearChancePercent();
	}

	public static int rareGearChancePercent(ExpeditionStrategy strategy) {
		return knobs(strategy).rareGearChancePercent();
	}

	public static int herbChancePercent(ExpeditionStrategy strategy) {
		return knobs(strategy).herbChancePercent();
	}

	private static GameBalance.ExpeditionStrategyKnobs knobs(ExpeditionStrategy strategy) {
		return switch (strategy) {
			case CAUTIOUS -> VALUES.cautious();
			case BALANCED -> VALUES.balanced();
			case AGGRESSIVE -> VALUES.aggressive();
		};
	}
}
