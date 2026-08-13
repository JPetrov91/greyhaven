package com.example.game.combat.domain;

/**
 * Snapshot of derived player combat stats used by {@link CombatEngine}.
 */
public record CombatantStats(
		int physicalDamage,
		int accuracy,
		int dodge,
		int criticalChance,
		int armor,
		int agility
) {
}
