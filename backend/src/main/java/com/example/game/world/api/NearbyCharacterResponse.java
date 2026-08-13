package com.example.game.world.api;

import java.util.UUID;

public record NearbyCharacterResponse(
		UUID id,
		String name,
		int level
) {
}
