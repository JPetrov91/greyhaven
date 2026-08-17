package com.example.game.world.application;

import java.util.List;

public record NearbyCharactersView(
		List<NearbyCharacterView> characters,
		boolean truncated,
		int limit,
		long totalCount
) {
}
