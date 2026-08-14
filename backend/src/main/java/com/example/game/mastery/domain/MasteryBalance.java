package com.example.game.mastery.domain;

import com.example.game.shared.balance.GameBalance;
import com.example.game.shared.balance.GameBalanceCatalog;

/**
 * Mastery XP thresholds and victory grants. Thresholds are cumulative XP required to reach a
 * mastery level.
 */
public final class MasteryBalance {

	private static final GameBalance.Mastery VALUES = GameBalanceCatalog.get().mastery();

	private static final int[] CUMULATIVE_XP_TO_REACH_LEVEL = VALUES.cumulativeXpToReachLevel().clone();
	private static final int[] UNLOCK_LEVELS = VALUES.unlockLevels().clone();

	public static final int MAX_LEVEL = VALUES.maxLevel();
	public static final int XP_PER_VICTORY = VALUES.xpPerVictory();
	public static final int STARTING_LEVEL = 0;

	private MasteryBalance() {
	}

	public static int cumulativeXpForLevel(int level) {
		if (level < STARTING_LEVEL) {
			throw new IllegalArgumentException("level below minimum");
		}
		if (level > MAX_LEVEL) {
			return CUMULATIVE_XP_TO_REACH_LEVEL[MAX_LEVEL];
		}
		return CUMULATIVE_XP_TO_REACH_LEVEL[level];
	}

	public static int xpToNextLevel(int level) {
		if (level >= MAX_LEVEL) {
			return 0;
		}
		return cumulativeXpForLevel(level + 1) - cumulativeXpForLevel(level);
	}

	public static int[] unlockLevels() {
		return UNLOCK_LEVELS.clone();
	}

	public static boolean isUnlockLevel(int level) {
		for (int unlock : UNLOCK_LEVELS) {
			if (unlock == level) {
				return true;
			}
		}
		return false;
	}
}
