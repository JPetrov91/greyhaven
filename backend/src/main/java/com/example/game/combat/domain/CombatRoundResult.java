package com.example.game.combat.domain;

import java.util.List;

public record CombatRoundResult(
		int roundNumber,
		int playerHealth,
		int playerStamina,
		int enemyHealth,
		CombatSessionStatus status,
		List<CombatEvent> events
) {
}
