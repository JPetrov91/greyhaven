package com.example.game.pvp.application;

public record PvpSettlementView(
		int attackerRatingDelta,
		int defenderRatingDelta,
		int attackerMarks,
		int defenderMarks,
		boolean applied
) {
}
