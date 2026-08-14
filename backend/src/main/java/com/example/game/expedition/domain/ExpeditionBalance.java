package com.example.game.expedition.domain;

import java.time.Duration;

/**
 * Balance knobs for Forest Patrol and strategy modifiers.
 */
public final class ExpeditionBalance {

	public static final Duration FOREST_PATROL_DURATION = Duration.ofMinutes(20);

	private ExpeditionBalance() {
	}

	public static int injuryChancePercent(ExpeditionStrategy strategy) {
		return switch (strategy) {
			case CAUTIOUS -> 10;
			case BALANCED -> 25;
			case AGGRESSIVE -> 45;
		};
	}

	public static int injuryDamageMin(ExpeditionStrategy strategy) {
		return switch (strategy) {
			case CAUTIOUS -> 3;
			case BALANCED -> 5;
			case AGGRESSIVE -> 8;
		};
	}

	public static int injuryDamageMax(ExpeditionStrategy strategy) {
		return switch (strategy) {
			case CAUTIOUS -> 8;
			case BALANCED -> 12;
			case AGGRESSIVE -> 18;
		};
	}

	public static int goldMin(ExpeditionStrategy strategy) {
		return switch (strategy) {
			case CAUTIOUS -> 2;
			case BALANCED -> 5;
			case AGGRESSIVE -> 8;
		};
	}

	public static int goldMax(ExpeditionStrategy strategy) {
		return switch (strategy) {
			case CAUTIOUS -> 12;
			case BALANCED -> 20;
			case AGGRESSIVE -> 35;
		};
	}

	public static int xpMin(ExpeditionStrategy strategy) {
		return switch (strategy) {
			case CAUTIOUS -> 8;
			case BALANCED -> 15;
			case AGGRESSIVE -> 22;
		};
	}

	public static int xpMax(ExpeditionStrategy strategy) {
		return switch (strategy) {
			case CAUTIOUS -> 18;
			case BALANCED -> 30;
			case AGGRESSIVE -> 45;
		};
	}

	/**
	 * Chance of finding nothing meaningful (still may roll tiny residual XP/gold of 0).
	 */
	public static int emptyHaulChancePercent(ExpeditionStrategy strategy) {
		return switch (strategy) {
			case CAUTIOUS -> 20;
			case BALANCED -> 12;
			case AGGRESSIVE -> 8;
		};
	}

	public static int materialChancePercent(ExpeditionStrategy strategy) {
		return switch (strategy) {
			case CAUTIOUS -> 35;
			case BALANCED -> 50;
			case AGGRESSIVE -> 65;
		};
	}

	public static int potionChancePercent(ExpeditionStrategy strategy) {
		return switch (strategy) {
			case CAUTIOUS -> 20;
			case BALANCED -> 30;
			case AGGRESSIVE -> 40;
		};
	}

	public static int commonGearChancePercent(ExpeditionStrategy strategy) {
		return switch (strategy) {
			case CAUTIOUS -> 8;
			case BALANCED -> 15;
			case AGGRESSIVE -> 22;
		};
	}

	public static int rareGearChancePercent(ExpeditionStrategy strategy) {
		return switch (strategy) {
			case CAUTIOUS -> 4;
			case BALANCED -> 8;
			case AGGRESSIVE -> 12;
		};
	}

	public static int herbChancePercent(ExpeditionStrategy strategy) {
		return switch (strategy) {
			case CAUTIOUS -> 25;
			case BALANCED -> 40;
			case AGGRESSIVE -> 50;
		};
	}
}
