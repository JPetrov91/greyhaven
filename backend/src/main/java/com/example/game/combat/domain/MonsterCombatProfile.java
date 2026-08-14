package com.example.game.combat.domain;

/**
 * Snapshot of monster combat ratings taken when a Combat 2.0 session starts.
 */
public record MonsterCombatProfile(
		String name,
		int level,
		int damageMin,
		int damageMax,
		int armor,
		int accuracy,
		int dodge,
		int criticalChance,
		int maxHealth,
		int maxStamina,
		EnemyAiArchetype archetype,
		StatusType signatureStatus
) {
}
