package com.example.game.shared.balance;

/**
 * Immutable snapshot of {@code game-balance.yml}.
 */
public record GameBalance(
		Character character,
		Progression progression,
		Inventory inventory
) {

	public record Character(
			int startingLevel,
			int startingExperience,
			int startingStrength,
			int startingAgility,
			int startingEndurance,
			int startingPerception,
			int startingGold,
			int maxLevel,
			int baseMaxHealth,
			int healthPerEndurance,
			int baseMaxStamina,
			int staminaPerEndurance,
			int staminaPerAgility,
			int defeatRecoveryPercent
	) {
	}

	public record Progression(
			int attributePointsPerLevel,
			int maxAttributeValue,
			int[] cumulativeXpToReachLevel
	) {
	}

	public record Inventory(int defaultCapacity) {
	}
}
