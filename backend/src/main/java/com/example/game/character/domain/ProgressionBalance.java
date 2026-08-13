package com.example.game.character.domain;

import com.example.game.shared.balance.GameBalance;
import com.example.game.shared.balance.GameBalanceCatalog;

/**
 * XP thresholds and level-up rewards. Thresholds are cumulative XP required to reach the next level.
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
}
