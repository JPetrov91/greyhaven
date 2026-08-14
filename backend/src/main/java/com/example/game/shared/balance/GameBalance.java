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
		Items items,
		Market market,
		Crafting crafting,
		Pvp pvp
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
			double criticalChancePerPerception,
			int minHitChance,
			int maxHitChance,
			int critChanceCap,
			int criticalDamageMult,
			int armorK,
			int minDamageAfterArmor,
			double armorBreakPerStack,
			double guardedDamageTakenMult,
			int bleedDamagePerStack,
			int poisonDamagePerStack,
			int bleedMaxStacks,
			int poisonMaxStacks,
			int armorBreakMaxStacks,
			int stunImmunityRounds,
			int offBalanceDodgePenalty,
			int offBalanceAccuracyPenalty,
			int counterDamagePercent,
			int cleaveVsGuardedPercent,
			int advancedDamagePercent,
			int advancedHpThresholdPercent,
			int staminaRegenPerRound,
			double staminaRegenPerAgility,
			int enemyStaminaRegenPerRound,
			int defendStaminaRestore,
			int enemyBasicStaminaCost,
			int enemyHeavyStaminaCost,
			int enemyStatusAttackStaminaCost
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

	public record Market(
			double merchantBuyMultiplier,
			double merchantSellMultiplier,
			double affixValuePerAffix,
			int maxMerchantPurchaseQuantity,
			double listingFeePercent,
			double saleFeePercent,
			int listingPageSize,
			int maxListingPageSize,
			double commonRarityModifier,
			double uncommonRarityModifier,
			double rareRarityModifier,
			double epicRarityModifier
	) {
	}

	public record Crafting(
			int maxRank,
			int xpPerRecipe,
			int rankRarityBonusPerRank,
			int[] cumulativeXpToReachRank,
			int commonSalvageMultiplier,
			int uncommonSalvageMultiplier,
			int rareSalvageMultiplier,
			int epicSalvageMultiplier
	) {
	}

	public record Pvp(
			int startingRating,
			int ratingKFactor,
			int ratingFloor,
			int repeatWindowHours,
			double repeatRatingMultiplier,
			int marksPerWin,
			int marksPerLoss,
			int maxSnapshotPotions,
			int maxArenaChallengesPerDay,
			int opponentRatingBand,
			int opponentsPageSize,
			int historyPageSize,
			int duelChallengeTtlMinutes,
			int duelActionTimeoutMinutes,
			int duelExpireMinutes,
			int healWhenHpPercentBelowDefault,
			int defendWhenStaminaPercentBelowDefault,
			int finisherWhenEnemyHpPercentBelowDefault
	) {
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
