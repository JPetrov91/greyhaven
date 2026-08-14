package com.example.game.combat.domain;

import java.util.List;

/**
 * Known combat state available to enemy AI. Never includes a queued future player action.
 */
public record EnemyAiView(
		EnemyAiArchetype archetype,
		int ownHealth,
		int ownMaxHealth,
		int ownStamina,
		int ownMaxStamina,
		List<StatusInstance> ownStatuses,
		int playerHealth,
		int playerMaxHealth,
		List<StatusInstance> playerStatuses,
		StatusType signatureStatus,
		MonsterTier tier
) {

	public MonsterTier tier() {
		return tier == null ? MonsterTier.NORMAL : tier;
	}

	public boolean enraged() {
		MonsterTier rank = tier();
		return (rank == MonsterTier.MINI_BOSS || rank == MonsterTier.BOSS) && ownHealthPercent() < 50;
	}

	public int playerHealthPercent() {
		if (playerMaxHealth <= 0) {
			return 0;
		}
		return (int) Math.round(playerHealth * 100.0 / playerMaxHealth);
	}

	public int ownHealthPercent() {
		if (ownMaxHealth <= 0) {
			return 0;
		}
		return (int) Math.round(ownHealth * 100.0 / ownMaxHealth);
	}

	public int ownStaminaPercent() {
		if (ownMaxStamina <= 0) {
			return 0;
		}
		return (int) Math.round(ownStamina * 100.0 / ownMaxStamina);
	}
}
