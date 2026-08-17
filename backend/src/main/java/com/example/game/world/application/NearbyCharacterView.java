package com.example.game.world.application;

import java.util.UUID;

public record NearbyCharacterView(
		UUID id,
		String name,
		int level,
		String avatarCode
) {
}
