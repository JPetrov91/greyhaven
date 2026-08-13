package com.example.game.character.application;

import java.time.Instant;
import java.util.UUID;

/**
 * Application-layer character snapshot. Controllers map this to API DTOs.
 */
public record CharacterView(
		UUID id,
		UUID accountId,
		String name,
		int level,
		int experience,
		int strength,
		int agility,
		int endurance,
		int perception,
		int currentHealth,
		int maxHealth,
		int currentStamina,
		int maxStamina,
		int gold,
		UUID currentLocationId,
		Instant createdAt,
		Instant updatedAt
) {
}
