package com.example.game.character.domain;

/**
 * XP thresholds and level-up rewards. Thresholds are cumulative XP required to reach the next level.
 */
public final class ProgressionBalance {

	/**
	 * Index 0 unused. {@code XP_TO_REACH_LEVEL[n]} is total XP needed to reach level {@code n}
	 * from level 1 (cumulative).
	 */
	private static final int[] CUMULATIVE_XP_TO_REACH_LEVEL = {
			0,   // unused
			0,   // level 1
			100, // level 2
			350, // level 3 (100+250)
			800, // level 4 (350+450)
			1500, // level 5 (800+700)
			2500, // level 6 (1500+1000)
			3700, // level 7
			5100, // level 8
			6700, // level 9
			8500  // level 10
	};

	public static final int ATTRIBUTE_POINTS_PER_LEVEL = 2;
	public static final int MAX_ATTRIBUTE_VALUE = 40;

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
