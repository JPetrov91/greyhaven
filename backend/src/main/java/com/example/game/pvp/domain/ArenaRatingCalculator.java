package com.example.game.pvp.domain;

/**
 * Isolated rating model so the formula can be replaced later without rewriting match flow.
 */
public final class ArenaRatingCalculator {

	private ArenaRatingCalculator() {
	}

	public record RatingChange(int attackerDelta, int defenderDelta) {
	}

	public static RatingChange of(int attackerRating, int defenderRating, boolean attackerWon, double multiplier) {
		double expectedAttacker = expectedScore(attackerRating, defenderRating);
		double expectedDefender = 1.0 - expectedAttacker;
		double scoreAttacker = attackerWon ? 1.0 : 0.0;
		double scoreDefender = attackerWon ? 0.0 : 1.0;
		int attackerDelta = scaledDelta(scoreAttacker - expectedAttacker, multiplier);
		int defenderDelta = scaledDelta(scoreDefender - expectedDefender, multiplier);
		return new RatingChange(attackerDelta, defenderDelta);
	}

	public static int apply(int current, int delta) {
		return Math.max(PvPBalance.RATING_FLOOR, current + delta);
	}

	static double expectedScore(int ownRating, int opponentRating) {
		return 1.0 / (1.0 + Math.pow(10.0, (opponentRating - ownRating) / 400.0));
	}

	private static int scaledDelta(double raw, double multiplier) {
		return (int) Math.round(PvPBalance.RATING_K_FACTOR * raw * Math.max(0.0, multiplier));
	}
}
