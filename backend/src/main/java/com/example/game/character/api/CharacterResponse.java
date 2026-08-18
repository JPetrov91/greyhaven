package com.example.game.character.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CharacterResponse(
		UUID id,
		UUID accountId,
		String name,
		String gender,
		String avatarCode,
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
		int arenaRating,
		int arenaMarks,
		int unspentAttributePoints,
		UUID currentLocationId,
		DerivedStatsResponse derivedStats,
		ProgressionResponse progression,
		Instant createdAt,
		Instant updatedAt,
		List<String> unlocks,
		boolean chapter1Prologue
) {
}
