package com.example.game.world.api;

import java.util.List;

public record NearbyCharactersResponse(
		List<NearbyCharacterResponse> characters,
		boolean truncated,
		int limit,
		long totalCount
) {
}
