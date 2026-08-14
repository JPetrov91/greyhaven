package com.example.game.shared.balance;

import java.util.List;

/**
 * Immutable snapshot of {@code game-balance.yml}.
 */
public record GameBalance(
		Character character,
		Progression progression,
		Combat combat,
		Recovery recovery,
		Mastery mastery,
		Inventory inventory,
		Items items
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
			int healthPerLevel,
			int baseMaxStamina,
			int staminaPerEndurance,
			int staminaPerAgility,
			int defeatRecoveryPercent
	) {
	}

	public record Progression(
			int attributePointsPerLevel,
			int maxAttributeValue,
			int freeRespecMaxLevel,
			int respecBaseGold,
			int respecGoldPerLevel,
			int[] cumulativeXpToReachLevel
	) {
	}

	public record Combat(
			double physicalDamagePerStrength,
			int baseAccuracy,
			double accuracyPerPerception,
			double dodgePerAgility,
			double baseCriticalChance,
			double criticalChancePerPerception
	) {
	}

	public record Recovery(List<RecoveryBand> bands) {
	}

	public record RecoveryBand(
			int maxLevel,
			double healthPercentPerMinute,
			double staminaPercentPerMinute
	) {
	}

	public record Mastery(
			int maxLevel,
			int xpPerVictory,
			int[] unlockLevels,
			int[] cumulativeXpToReachLevel
	) {
	}

	public record Inventory(int defaultCapacity) {
	}

	public record Items(
			int baseRollPercentMin,
			int baseRollPercentMax,
			int commonWeight,
			int uncommonWeight,
			int rareWeight,
			int epicWeight,
			int commonAffixes,
			int uncommonAffixes,
			int rareAffixes,
			int epicAffixes,
			int lightDodge,
			int mediumDodge,
			int heavyDodge
	) {
	}
}
