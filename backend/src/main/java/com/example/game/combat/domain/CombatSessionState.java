package com.example.game.combat.domain;

/**
 * Immutable combat snapshot fed into {@link CombatEngine}.
 */
public record CombatSessionState(
		int roundNumber,
		int playerHealth,
		int playerMaxHealth,
		int playerStamina,
		int playerMaxStamina,
		int enemyHealth,
		CombatSessionStatus status,
		CombatantStats playerStats,
		MonsterCombatStats monster
) {
}
