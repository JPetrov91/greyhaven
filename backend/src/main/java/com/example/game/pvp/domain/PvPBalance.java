package com.example.game.pvp.domain;

import com.example.game.shared.balance.GameBalance;
import com.example.game.shared.balance.GameBalanceCatalog;

/**
 * Isolated Arena/duel numeric rules.
 */
public final class PvPBalance {

	private static final GameBalance.Pvp VALUES = GameBalanceCatalog.get().pvp();

	public static final int STARTING_RATING = VALUES.startingRating();
	public static final int RATING_K_FACTOR = VALUES.ratingKFactor();
	public static final int RATING_FLOOR = VALUES.ratingFloor();
	public static final int REPEAT_WINDOW_HOURS = VALUES.repeatWindowHours();
	public static final double REPEAT_RATING_MULTIPLIER = VALUES.repeatRatingMultiplier();
	public static final int MARKS_PER_WIN = VALUES.marksPerWin();
	public static final int MARKS_PER_LOSS = VALUES.marksPerLoss();
	public static final int MAX_SNAPSHOT_POTIONS = VALUES.maxSnapshotPotions();
	public static final int MAX_ARENA_CHALLENGES_PER_DAY = VALUES.maxArenaChallengesPerDay();
	public static final int OPPONENT_RATING_BAND = VALUES.opponentRatingBand();
	public static final int OPPONENTS_PAGE_SIZE = VALUES.opponentsPageSize();
	public static final int HISTORY_PAGE_SIZE = VALUES.historyPageSize();
	public static final int DUEL_CHALLENGE_TTL_MINUTES = VALUES.duelChallengeTtlMinutes();
	public static final int DUEL_ACTION_TIMEOUT_MINUTES = VALUES.duelActionTimeoutMinutes();
	public static final int DUEL_EXPIRE_MINUTES = VALUES.duelExpireMinutes();
	public static final int HEAL_HP_DEFAULT = VALUES.healWhenHpPercentBelowDefault();
	public static final int DEFEND_STAMINA_DEFAULT = VALUES.defendWhenStaminaPercentBelowDefault();
	public static final int FINISHER_HP_DEFAULT = VALUES.finisherWhenEnemyHpPercentBelowDefault();
	public static final int SNAPSHOT_VERSION = 1;

	private PvPBalance() {
	}

	public static int marksAwarded(boolean winner, boolean forfeit, double multiplier) {
		if (forfeit) {
			return 0;
		}
		int base = winner ? MARKS_PER_WIN : MARKS_PER_LOSS;
		return (int) Math.round(base * Math.max(0.0, multiplier));
	}
}
