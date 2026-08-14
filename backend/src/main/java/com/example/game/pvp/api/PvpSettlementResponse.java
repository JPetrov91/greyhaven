package com.example.game.pvp.api;

public record PvpSettlementResponse(
		int attackerRatingDelta,
		int defenderRatingDelta,
		int attackerMarks,
		int defenderMarks,
		boolean applied
) {
}
