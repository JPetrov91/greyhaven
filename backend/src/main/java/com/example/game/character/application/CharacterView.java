package com.example.game.character.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.example.game.character.domain.DerivedCombatStats;
import com.example.game.character.domain.ExperienceProgress;

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
		int arenaRating,
		int arenaMarks,
		int unspentAttributePoints,
		ExperienceProgress progression,
		UUID currentLocationId,
		DerivedCombatStats derivedStats,
		Instant createdAt,
		Instant updatedAt,
		List<String> unlocks
) {
}
