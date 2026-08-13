package com.example.game.combat.application;

import java.util.List;
import java.util.UUID;

import com.example.game.combat.domain.CombatSessionStatus;

public record CombatView(
		UUID id,
		UUID encounterId,
		CombatSessionStatus status,
		int roundNumber,
		int playerHealth,
		int playerMaxHealth,
		int playerStamina,
		int playerMaxStamina,
		int enemyHealth,
		int enemyMaxHealth,
		MonsterView monster,
		boolean potionAvailable,
		List<CombatEventView> events,
		CombatRewardsView rewards
) {
}
