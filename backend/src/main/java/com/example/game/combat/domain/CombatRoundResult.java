package com.example.game.combat.domain;

import java.util.List;

public record CombatRoundResult(
		int roundNumber,
		int playerHealth,
		int playerStamina,
		int enemyHealth,
		CombatSessionStatus status,
		List<CombatEvent> events,
		int enemyStamina,
		List<StatusInstance> playerStatuses,
		List<StatusInstance> enemyStatuses,
		boolean lastEnemyMissed,
		boolean lastPlayerGuarded
) {

	public static CombatRoundResult phase1(
			int roundNumber,
			int playerHealth,
			int playerStamina,
			int enemyHealth,
			CombatSessionStatus status,
			List<CombatEvent> events) {
		return new CombatRoundResult(
				roundNumber,
				playerHealth,
				playerStamina,
				enemyHealth,
				status,
				events,
				0,
				List.of(),
				List.of(),
				false,
				false);
	}
}
