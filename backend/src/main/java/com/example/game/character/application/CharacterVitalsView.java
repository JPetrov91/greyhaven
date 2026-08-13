package com.example.game.character.application;

import java.util.UUID;

/**
 * Character vitals and attributes needed by inventory/combat systems.
 */
public record CharacterVitalsView(
		UUID characterId,
		int level,
		int strength,
		int agility,
		int endurance,
		int perception,
		int currentHealth,
		int maxHealth,
		int currentStamina,
		int maxStamina
) {
}
