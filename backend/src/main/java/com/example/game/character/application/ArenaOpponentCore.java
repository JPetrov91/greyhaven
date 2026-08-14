package com.example.game.character.application;

import java.util.UUID;

public record ArenaOpponentCore(
		UUID id,
		String name,
		int level,
		int arenaRating
) {
}
