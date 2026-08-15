package com.example.game.shared.balance;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

/**
 * Loads {@code game-balance.yml} once from the classpath. No Spring dependency.
 */
public final class GameBalanceCatalog {

	private static final String RESOURCE = "/game-balance.yml";
	private static final GameBalance INSTANCE = load();

	private GameBalanceCatalog() {
	}

	public static GameBalance get() {
		return INSTANCE;
	}

	static GameBalance loadFrom(InputStream yamlStream) {
		Yaml yaml = new Yaml();
		@SuppressWarnings("unchecked")
		Map<String, Object> root = yaml.load(yamlStream);
		if (root == null) {
			throw new IllegalStateException("game-balance.yml is empty");
		}
		return new GameBalance(
				parseCharacter(map(root, "character")),
				parseProgression(map(root, "progression")),
				parseCombat(map(root, "combat")),
				parseRecovery(map(root, "recovery")),
				parseMastery(map(root, "mastery")),
				parseInventory(map(root, "inventory")),
				parseItems(map(root, "items")),
				parseMarket(map(root, "market")),
				parseCrafting(map(root, "crafting")),
				parsePvp(map(root, "pvp")),
				parseExpedition(map(root, "expedition")));
	}

	private static GameBalance load() {
		try (InputStream in = GameBalanceCatalog.class.getResourceAsStream(RESOURCE)) {
			if (in == null) {
				throw new IllegalStateException("Missing classpath resource " + RESOURCE);
			}
			return loadFrom(in);
		}
		catch (IOException ex) {
			throw new UncheckedIOException("Failed to read " + RESOURCE, ex);
		}
	}

	private static GameBalance.Character parseCharacter(Map<String, Object> node) {
		return new GameBalance.Character(
				intValue(node, "startingLevel"),
				intValue(node, "startingExperience"),
				intValue(node, "startingStrength"),
				intValue(node, "startingAgility"),
				intValue(node, "startingEndurance"),
				intValue(node, "startingPerception"),
				intValue(node, "startingGold"),
				intValue(node, "maxLevel"),
				intValue(node, "baseMaxHealth"),
				intValue(node, "healthPerEndurance"),
				intValue(node, "healthPerLevel"),
				intValue(node, "baseMaxStamina"),
				intValue(node, "staminaPerEndurance"),
				intValue(node, "staminaPerAgility"),
				intValue(node, "defeatRecoveryPercent"));
	}

	private static GameBalance.Progression parseProgression(Map<String, Object> node) {
		return new GameBalance.Progression(
				intValue(node, "attributePointsPerLevel"),
				intValue(node, "maxAttributeValue"),
				intValue(node, "freeRespecMaxLevel"),
				intValue(node, "respecBaseGold"),
				intValue(node, "respecGoldPerLevel"),
				intArray(node, "cumulativeXpToReachLevel"));
	}

	private static GameBalance.Combat parseCombat(Map<String, Object> node) {
		return new GameBalance.Combat(
				doubleValue(node, "physicalDamagePerStrength"),
				intValue(node, "baseAccuracy"),
				doubleValue(node, "accuracyPerPerception"),
				doubleValue(node, "dodgePerAgility"),
				doubleValue(node, "baseCriticalChance"),
				doubleValue(node, "criticalChancePerPerception"),
				intValue(node, "minHitChance"),
				intValue(node, "maxHitChance"),
				intValue(node, "critChanceCap"),
				intValue(node, "criticalDamageMult"),
				intValue(node, "armorK"),
				intValue(node, "minDamageAfterArmor"),
				doubleValue(node, "armorBreakPerStack"),
				doubleValue(node, "guardedDamageTakenMult"),
				intValue(node, "bleedDamagePerStack"),
				intValue(node, "poisonDamagePerStack"),
				intValue(node, "bleedMaxStacks"),
				intValue(node, "poisonMaxStacks"),
				intValue(node, "armorBreakMaxStacks"),
				intValue(node, "stunImmunityRounds"),
				intValue(node, "offBalanceDodgePenalty"),
				intValue(node, "offBalanceAccuracyPenalty"),
				intValue(node, "counterDamagePercent"),
				intValue(node, "cleaveVsGuardedPercent"),
				intValue(node, "advancedDamagePercent"),
				intValue(node, "advancedHpThresholdPercent"),
				intValue(node, "staminaRegenPerRound"),
				doubleValue(node, "staminaRegenPerAgility"),
				intValue(node, "enemyStaminaRegenPerRound"),
				intValue(node, "defendStaminaRestore"),
				intValue(node, "enemyBasicStaminaCost"),
				intValue(node, "enemyHeavyStaminaCost"),
				intValue(node, "enemyStatusAttackStaminaCost"));
	}

	private static GameBalance.Recovery parseRecovery(Map<String, Object> node) {
		Object value = node.get("bands");
		if (!(value instanceof List<?> list) || list.isEmpty()) {
			throw new IllegalStateException("game-balance.yml missing list 'recovery.bands'");
		}
		List<GameBalance.RecoveryBand> bands = new ArrayList<>(list.size());
		for (Object element : list) {
			if (!(element instanceof Map<?, ?> raw)) {
				throw new IllegalStateException("game-balance.yml recovery.bands must contain objects");
			}
			@SuppressWarnings("unchecked")
			Map<String, Object> band = (Map<String, Object>) raw;
			bands.add(new GameBalance.RecoveryBand(
					intValue(band, "maxLevel"),
					doubleValue(band, "healthPercentPerMinute"),
					doubleValue(band, "staminaPercentPerMinute")));
		}
		return new GameBalance.Recovery(List.copyOf(bands));
	}

	private static GameBalance.Mastery parseMastery(Map<String, Object> node) {
		return new GameBalance.Mastery(
				intValue(node, "maxLevel"),
				intValue(node, "xpPerVictory"),
				intArray(node, "unlockLevels"),
				intArray(node, "cumulativeXpToReachLevel"));
	}

	private static GameBalance.Inventory parseInventory(Map<String, Object> node) {
		return new GameBalance.Inventory(intValue(node, "defaultCapacity"));
	}

	private static GameBalance.Market parseMarket(Map<String, Object> node) {
		Map<String, Object> rarityModifiers = map(node, "rarityModifiers");
		return new GameBalance.Market(
				doubleValue(node, "merchantBuyMultiplier"),
				doubleValue(node, "merchantSellMultiplier"),
				doubleValue(node, "affixValuePerAffix"),
				intValue(node, "maxMerchantPurchaseQuantity"),
				doubleValue(node, "listingFeePercent"),
				doubleValue(node, "saleFeePercent"),
				intValue(node, "listingPageSize"),
				intValue(node, "maxListingPageSize"),
				doubleValue(rarityModifiers, "COMMON"),
				doubleValue(rarityModifiers, "UNCOMMON"),
				doubleValue(rarityModifiers, "RARE"),
				doubleValue(rarityModifiers, "EPIC"));
	}

	private static GameBalance.Crafting parseCrafting(Map<String, Object> node) {
		Map<String, Object> salvage = map(node, "salvageRarityMultipliers");
		return new GameBalance.Crafting(
				intValue(node, "maxRank"),
				intValue(node, "xpPerRecipe"),
				intValue(node, "rankRarityBonusPerRank"),
				intArray(node, "cumulativeXpToReachRank"),
				intValue(salvage, "COMMON"),
				intValue(salvage, "UNCOMMON"),
				intValue(salvage, "RARE"),
				intValue(salvage, "EPIC"));
	}

	private static GameBalance.Pvp parsePvp(Map<String, Object> node) {
		return new GameBalance.Pvp(
				intValue(node, "startingRating"),
				intValue(node, "ratingKFactor"),
				intValue(node, "ratingFloor"),
				intValue(node, "repeatWindowHours"),
				doubleValue(node, "repeatRatingMultiplier"),
				intValue(node, "marksPerWin"),
				intValue(node, "marksPerLoss"),
				intValue(node, "maxSnapshotPotions"),
				intValue(node, "maxArenaChallengesPerDay"),
				intValue(node, "opponentRatingBand"),
				intValue(node, "opponentsPageSize"),
				intValue(node, "historyPageSize"),
				intValue(node, "duelChallengeTtlMinutes"),
				intValue(node, "duelActionTimeoutMinutes"),
				intValue(node, "duelExpireMinutes"),
				intValue(node, "healWhenHpPercentBelowDefault"),
				intValue(node, "defendWhenStaminaPercentBelowDefault"),
				intValue(node, "finisherWhenEnemyHpPercentBelowDefault"));
	}

	private static GameBalance.Expedition parseExpedition(Map<String, Object> node) {
		Map<String, Object> strategies = map(node, "strategies");
		return new GameBalance.Expedition(
				intValue(node, "forestPatrolDurationMinutes"),
				parseExpeditionStrategy(map(strategies, "CAUTIOUS")),
				parseExpeditionStrategy(map(strategies, "BALANCED")),
				parseExpeditionStrategy(map(strategies, "AGGRESSIVE")));
	}

	private static GameBalance.ExpeditionStrategyKnobs parseExpeditionStrategy(Map<String, Object> node) {
		return new GameBalance.ExpeditionStrategyKnobs(
				intValue(node, "injuryChancePercent"),
				intValue(node, "injuryDamageMin"),
				intValue(node, "injuryDamageMax"),
				intValue(node, "goldMin"),
				intValue(node, "goldMax"),
				intValue(node, "xpMin"),
				intValue(node, "xpMax"),
				intValue(node, "emptyHaulChancePercent"),
				intValue(node, "materialChancePercent"),
				intValue(node, "potionChancePercent"),
				intValue(node, "commonGearChancePercent"),
				intValue(node, "rareGearChancePercent"),
				intValue(node, "herbChancePercent"));
	}

	private static GameBalance.Items parseItems(Map<String, Object> node) {
		Map<String, Object> rarityWeights = map(node, "rarityWeights");
		Map<String, Object> affixCounts = map(node, "affixCounts");
		Map<String, Object> armorDodge = map(node, "armorDodge");
		return new GameBalance.Items(
				intValue(node, "baseRollPercentMin"),
				intValue(node, "baseRollPercentMax"),
				intValue(rarityWeights, "COMMON"),
				intValue(rarityWeights, "UNCOMMON"),
				intValue(rarityWeights, "RARE"),
				intValue(rarityWeights, "EPIC"),
				intValue(affixCounts, "COMMON"),
				intValue(affixCounts, "UNCOMMON"),
				intValue(affixCounts, "RARE"),
				intValue(affixCounts, "EPIC"),
				intValue(armorDodge, "LIGHT"),
				intValue(armorDodge, "MEDIUM"),
				intValue(armorDodge, "HEAVY"));
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> map(Map<String, Object> parent, String key) {
		Object value = parent.get(key);
		if (!(value instanceof Map<?, ?> nested)) {
			throw new IllegalStateException("game-balance.yml missing object '" + key + "'");
		}
		return (Map<String, Object>) nested;
	}

	private static int intValue(Map<String, Object> node, String key) {
		Object value = node.get(key);
		if (value instanceof Number number) {
			return number.intValue();
		}
		throw new IllegalStateException("game-balance.yml missing integer '" + key + "'");
	}

	private static double doubleValue(Map<String, Object> node, String key) {
		Object value = node.get(key);
		if (value instanceof Number number) {
			return number.doubleValue();
		}
		throw new IllegalStateException("game-balance.yml missing number '" + key + "'");
	}

	private static int[] intArray(Map<String, Object> node, String key) {
		Object value = node.get(key);
		if (!(value instanceof List<?> list) || list.isEmpty()) {
			throw new IllegalStateException("game-balance.yml missing integer list '" + key + "'");
		}
		int[] result = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Object element = list.get(i);
			if (!(element instanceof Number number)) {
				throw new IllegalStateException("game-balance.yml list '" + key + "' must contain integers");
			}
			result[i] = number.intValue();
		}
		return result;
	}
}
