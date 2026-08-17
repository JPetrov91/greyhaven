package com.example.game.character.application;

import java.util.UUID;

public record CharacterAtLocationView(
		UUID characterId,
		String name,
		int level,
		String avatarCode
) {
}
