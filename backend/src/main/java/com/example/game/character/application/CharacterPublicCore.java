package com.example.game.character.application;

import java.util.UUID;

public record CharacterPublicCore(
		UUID id,
		String name,
		int level,
		int strength,
		int agility,
		int endurance,
		int perception,
		int maxHealth,
		int maxStamina,
		int arenaRating,
		int arenaMarks
) {
}
