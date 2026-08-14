package com.example.game.combat.api;

import java.util.List;
import java.util.UUID;

import com.example.game.combat.domain.CombatSessionStatus;

public record CombatResponse(
		UUID id,
		UUID encounterId,
		CombatSessionStatus status,
		int rulesVersion,
		int roundNumber,
		int playerHealth,
		int playerMaxHealth,
		int playerStamina,
		int playerMaxStamina,
		int enemyHealth,
		int enemyMaxHealth,
		int enemyStamina,
		int enemyMaxStamina,
		MonsterResponse monster,
		boolean potionAvailable,
		boolean playerStunned,
		List<CombatStatusResponse> playerStatuses,
		List<CombatStatusResponse> enemyStatuses,
		List<CombatTechniqueOptionResponse> techniques,
		CoreActionCostsResponse coreActionCosts,
		List<CombatEventResponse> events,
		CombatRewardsResponse rewards,
		CombatIntentResponse enemyIntent,
		List<CombatActionPreviewResponse> actionPreviews,
		List<CombatLootPreviewResponse> possibleLoot
) {
}
