package com.example.game.pvp.api;

import java.util.List;
import java.util.UUID;

public record ArenaOpponentListResponse(
		List<ArenaOpponentResponse> opponents,
		int page,
		int size,
		boolean hasMore
) {
	public record ArenaOpponentResponse(UUID id, String name, int level, int rating) {
	}
}
