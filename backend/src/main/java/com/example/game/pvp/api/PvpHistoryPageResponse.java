package com.example.game.pvp.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.example.game.pvp.domain.PvpHistoryResult;
import com.example.game.pvp.domain.PvpMatchKind;

public record PvpHistoryPageResponse(
		List<PvpHistoryEntryResponse> entries,
		int page,
		int size,
		boolean hasMore
) {
	public record PvpHistoryEntryResponse(
			UUID matchId,
			PvpMatchKind matchKind,
			String opponentName,
			UUID opponentId,
			PvpHistoryResult result,
			int ratingDelta,
			int marksAwarded,
			Instant createdAt
	) {
	}
}
