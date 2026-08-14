package com.example.game.combat.application;

import java.util.List;
import java.util.UUID;

import com.example.game.combat.domain.CombatSessionStatus;

public record CombatView(
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
		MonsterView monster,
		boolean potionAvailable,
		boolean playerStunned,
		List<CombatStatusView> playerStatuses,
		List<CombatStatusView> enemyStatuses,
		List<CombatTechniqueOptionView> techniques,
		CoreActionCostsView coreActionCosts,
		List<CombatEventView> events,
		CombatRewardsView rewards
) {
}
