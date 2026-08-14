package com.example.game.pvp.application;

import java.util.List;
import java.util.UUID;

public record ArenaOpponentListView(
		List<ArenaOpponentView> opponents,
		int page,
		int size,
		boolean hasMore
) {
	public record ArenaOpponentView(
			UUID id,
			String name,
			int level,
			int rating
	) {
	}
}
