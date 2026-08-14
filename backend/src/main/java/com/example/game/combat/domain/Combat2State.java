package com.example.game.combat.domain;

import java.util.List;
import java.util.Map;

import com.example.game.mastery.domain.TechniqueEffectSpec;

/**
 * Immutable Combat 2.0 snapshot fed into {@link CombatEngine}.
 */
public record Combat2State(
		int roundNumber,
		int playerHealth,
		int playerMaxHealth,
		int playerStamina,
		int playerMaxStamina,
		int enemyHealth,
		int enemyMaxHealth,
		int enemyStamina,
		int enemyMaxStamina,
		CombatSessionStatus status,
		CombatantStats playerStats,
		MonsterCombatProfile enemy,
		List<StatusInstance> playerStatuses,
		List<StatusInstance> enemyStatuses,
		List<String> availableTechniqueCodes,
		Map<String, TechniqueEffectSpec> techniqueSpecs,
		TechniqueEffectSpec masteryPassive,
		int staminaCostReduction,
		boolean lastEnemyMissed,
		boolean lastPlayerGuarded
) {
}
