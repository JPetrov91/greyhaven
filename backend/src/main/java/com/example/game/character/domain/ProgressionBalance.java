package com.example.game.character.domain;

import com.example.game.shared.balance.GameBalance;
import com.example.game.shared.balance.GameBalanceCatalog;

/**
 * XP thresholds, level-up rewards, and respec costs. Thresholds are cumulative XP required to
 * reach a level.
 */
public final class ProgressionBalance {

	private static final GameBalance.Progression VALUES = GameBalanceCatalog.get().progression();

	/**
	 * Index 0 unused. {@code XP_TO_REACH_LEVEL[n]} is total XP needed to reach level {@code n}
	 * from level 1 (cumulative).
	 */
	private static final int[] CUMULATIVE_XP_TO_REACH_LEVEL = VALUES.cumulativeXpToReachLevel().clone();

	public static final int ATTRIBUTE_POINTS_PER_LEVEL = VALUES.attributePointsPerLevel();
	public static final int MAX_ATTRIBUTE_VALUE = VALUES.maxAttributeValue();
	public static final int FREE_RESPEC_MAX_LEVEL = VALUES.freeRespecMaxLevel();
	public static final int RESPEC_BASE_GOLD = VALUES.respecBaseGold();
	public static final int RESPEC_GOLD_PER_LEVEL = VALUES.respecGoldPerLevel();

	private ProgressionBalance() {
	}

	/**
	 * Cumulative XP required to be at least this level.
	 */
	public static int cumulativeXpForLevel(int level) {
		if (level < CharacterBalance.STARTING_LEVEL) {
			throw new IllegalArgumentException("level below minimum");
		}
		if (level > CharacterBalance.MAX_LEVEL) {
			return CUMULATIVE_XP_TO_REACH_LEVEL[CharacterBalance.MAX_LEVEL];
		}
		return CUMULATIVE_XP_TO_REACH_LEVEL[level];
	}

	/**
	 * XP needed to advance from {@code level} to {@code level + 1}, or 0 at max level.
	 */
	public static int xpToNextLevel(int level) {
		if (level >= CharacterBalance.MAX_LEVEL) {
			return 0;
		}
		return cumulativeXpForLevel(level + 1) - cumulativeXpForLevel(level);
	}

	public static int respecGoldCost(int level) {
		if (level < CharacterBalance.STARTING_LEVEL || level > CharacterBalance.MAX_LEVEL) {
			throw new IllegalArgumentException("level out of range");
		}
		if (level <= FREE_RESPEC_MAX_LEVEL) {
			return 0;
		}
		return RESPEC_BASE_GOLD + (level * RESPEC_GOLD_PER_LEVEL);
	}
}
