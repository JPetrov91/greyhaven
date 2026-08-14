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
				parseInventory(map(root, "inventory")));
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
				doubleValue(node, "criticalChancePerPerception"));
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

	private static GameBalance.Inventory parseInventory(Map<String, Object> node) {
		return new GameBalance.Inventory(intValue(node, "defaultCapacity"));
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
