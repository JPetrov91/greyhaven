package com.example.game.character.domain;

/**
 * Backend-calculated combat statistics derived from attributes and equipment.
 */
public record DerivedCombatStats(
		int maxHealth,
		int maxStamina,
		int physicalDamage,
		int accuracy,
		int dodge,
		int criticalChance,
		int armor
) {
}
