package com.example.game.combat.api;

import java.util.List;
import java.util.UUID;

import com.example.game.combat.domain.CombatSessionStatus;

public record CombatResponse(
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
		MonsterResponse monster,
		boolean potionAvailable,
		List<CombatEventResponse> events,
		CombatRewardsResponse rewards
) {
}
