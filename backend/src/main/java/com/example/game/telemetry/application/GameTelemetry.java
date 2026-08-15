package com.example.game.telemetry.application;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.game.character.domain.XpSource;
import com.example.game.combat.domain.CombatEventType;
import com.example.game.combat.domain.CombatSessionStatus;
import com.example.game.item.domain.ItemRarity;
import com.example.game.item.domain.WeaponFamily;
import com.example.game.telemetry.domain.GoldCreateReason;
import com.example.game.telemetry.domain.GoldDestroyReason;
import com.example.game.telemetry.domain.ItemCreateSource;
import com.example.game.telemetry.domain.TelemetryCategory;
import com.example.game.telemetry.domain.TelemetryEventType;
import com.example.game.telemetry.domain.TelemetryPayload;

/**
 * Typed telemetry writes so gameplay services do not assemble ad-hoc maps.
 */
public final class GameTelemetry {

	private static final Pattern DAMAGE = Pattern.compile(" for (\\d+) damage");

	private GameTelemetry() {
	}

	public static void xpEarned(GameTelemetryRecorder recorder, UUID characterId, int amount, XpSource source) {
		if (amount < 1) {
			return;
		}
		recorder.record(
				TelemetryCategory.PROGRESSION,
				TelemetryEventType.XP_EARNED,
				characterId,
				TelemetryPayload.of("amount", amount, "source", source.name()));
	}

	public static void levelUp(GameTelemetryRecorder recorder, UUID characterId, int previousLevel, int newLevel) {
		if (newLevel <= previousLevel) {
			return;
		}
		recorder.record(
				TelemetryCategory.PROGRESSION,
				TelemetryEventType.LEVEL_UP,
				characterId,
				TelemetryPayload.of("previousLevel", previousLevel, "newLevel", newLevel));
	}

	public static void attributesAllocated(
			GameTelemetryRecorder recorder,
			UUID characterId,
			int strengthDelta,
			int agilityDelta,
			int enduranceDelta,
			int perceptionDelta) {
		recorder.record(
				TelemetryCategory.PROGRESSION,
				TelemetryEventType.ATTRIBUTES_ALLOCATED,
				characterId,
				TelemetryPayload.of(
						"strength", strengthDelta,
						"agility", agilityDelta,
						"endurance", enduranceDelta,
						"perception", perceptionDelta));
	}

	public static void respec(GameTelemetryRecorder recorder, UUID characterId, int goldSpent, int level) {
		recorder.record(
				TelemetryCategory.PROGRESSION,
				TelemetryEventType.RESPEC,
				characterId,
				TelemetryPayload.of("goldSpent", goldSpent, "level", level));
	}

	public static void goldCreated(
			GameTelemetryRecorder recorder,
			UUID characterId,
			int amount,
			GoldCreateReason reason) {
		if (amount < 1) {
			return;
		}
		recorder.record(
				TelemetryCategory.ECONOMY,
				TelemetryEventType.GOLD_CREATED,
				characterId,
				TelemetryPayload.of("amount", amount, "reason", reason.name()));
	}

	public static void goldDestroyed(
			GameTelemetryRecorder recorder,
			UUID characterId,
			int amount,
			GoldDestroyReason reason) {
		if (amount < 1) {
			return;
		}
		recorder.record(
				TelemetryCategory.ECONOMY,
				TelemetryEventType.GOLD_DESTROYED,
				characterId,
				TelemetryPayload.of("amount", amount, "reason", reason.name()));
	}

	public static void marketTrade(
			GameTelemetryRecorder recorder,
			UUID buyerId,
			UUID sellerId,
			int gross,
			int feeDestroyed,
			int sellerProceeds,
			String channel) {
		recorder.record(
				TelemetryCategory.ECONOMY,
				TelemetryEventType.MARKET_TRADE,
				buyerId,
				TelemetryPayload.of(
						"sellerCharacterId", sellerId.toString(),
						"gross", gross,
						"feeDestroyed", feeDestroyed,
						"sellerProceeds", sellerProceeds,
						"channel", channel));
	}

	public static void itemCreated(
			GameTelemetryRecorder recorder,
			UUID characterId,
			String itemCode,
			ItemRarity rarity,
			int quantity,
			ItemCreateSource source) {
		if (quantity < 1) {
			return;
		}
		recorder.record(
				TelemetryCategory.ECONOMY,
				TelemetryEventType.ITEM_CREATED,
				characterId,
				TelemetryPayload.of(
						"itemCode", itemCode,
						"rarity", rarity == null ? "NONE" : rarity.name(),
						"quantity", quantity,
						"source", source.name()));
	}

	public static void combatStarted(
			GameTelemetryRecorder recorder,
			UUID characterId,
			WeaponFamily weaponFamily,
			String techniqueCodes,
			int monsterLevel,
			String monsterTier) {
		recorder.record(
				TelemetryCategory.COMBAT,
				TelemetryEventType.COMBAT_STARTED,
				characterId,
				TelemetryPayload.of(
						"weaponFamily", weaponFamily == null ? "NONE" : weaponFamily.name(),
						"techniqueCodes", techniqueCodes == null ? "" : techniqueCodes,
						"monsterLevel", monsterLevel,
						"monsterTier", monsterTier));
	}

	public static void combatEnded(
			GameTelemetryRecorder recorder,
			UUID characterId,
			CombatSessionStatus status,
			long durationMs,
			int roundCount,
			WeaponFamily weaponFamily,
			String techniqueCodes,
			List<CombatLogLine> events) {
		int damageDealt = 0;
		int damageTaken = 0;
		int statusApplied = 0;
		Map<String, Integer> techniqueUses = new LinkedHashMap<>();
		for (CombatLogLine event : events) {
			if (event.type() == CombatEventType.PLAYER_ATTACK || event.type() == CombatEventType.PLAYER_CRIT) {
				damageDealt += damageFrom(event.message());
			}
			else if (event.type() == CombatEventType.ENEMY_ATTACK) {
				damageTaken += damageFrom(event.message());
			}
			else if (event.type() == CombatEventType.STATUS_APPLIED) {
				statusApplied++;
			}
			else if (event.type() == CombatEventType.PLAYER_TECHNIQUE) {
				String code = techniqueCodeFrom(event.message());
				techniqueUses.merge(code, 1, Integer::sum);
			}
		}
		String outcome = switch (status) {
			case PLAYER_WON -> "WIN";
			case PLAYER_LOST -> "LOSS";
			case PLAYER_ESCAPED -> "RETREAT";
			case ACTIVE -> "ACTIVE";
		};
		Map<String, Object> payload = new LinkedHashMap<>();
		TelemetryPayload.put(payload, "outcome", outcome);
		TelemetryPayload.put(payload, "retreat", status == CombatSessionStatus.PLAYER_ESCAPED);
		TelemetryPayload.put(payload, "durationMs", durationMs);
		TelemetryPayload.put(payload, "roundCount", roundCount);
		TelemetryPayload.put(payload, "weaponFamily", weaponFamily == null ? "NONE" : weaponFamily.name());
		TelemetryPayload.put(payload, "techniqueCodes", techniqueCodes == null ? "" : techniqueCodes);
		TelemetryPayload.put(payload, "damageDealt", damageDealt);
		TelemetryPayload.put(payload, "damageTaken", damageTaken);
		TelemetryPayload.put(payload, "statusApplied", statusApplied);
		TelemetryPayload.put(payload, "techniqueUses", Map.copyOf(techniqueUses));
		recorder.record(TelemetryCategory.COMBAT, TelemetryEventType.COMBAT_ENDED, characterId, payload);
	}

	public static void pvpMatchSettled(
			GameTelemetryRecorder recorder,
			UUID attackerId,
			UUID defenderId,
			String mode,
			boolean attackerWon,
			WeaponFamily attackerWeapon,
			WeaponFamily defenderWeapon,
			String attackerBuild,
			String defenderBuild,
			int attackerRatingBefore,
			int attackerRatingAfter,
			int defenderRatingBefore,
			int defenderRatingAfter,
			int attackerMarks,
			int defenderMarks,
			boolean repeatOpponent) {
		recorder.record(
				TelemetryCategory.PVP,
				TelemetryEventType.PVP_MATCH_SETTLED,
				attackerId,
				TelemetryPayload.of(
						"defenderCharacterId", defenderId.toString(),
						"mode", mode,
						"attackerWon", attackerWon,
						"attackerWeaponFamily", attackerWeapon == null ? "NONE" : attackerWeapon.name(),
						"defenderWeaponFamily", defenderWeapon == null ? "NONE" : defenderWeapon.name(),
						"attackerBuild", attackerBuild,
						"defenderBuild", defenderBuild,
						"attackerRatingBefore", attackerRatingBefore,
						"attackerRatingAfter", attackerRatingAfter,
						"defenderRatingBefore", defenderRatingBefore,
						"defenderRatingAfter", defenderRatingAfter,
						"attackerMarks", attackerMarks,
						"defenderMarks", defenderMarks,
						"repeatOpponent", repeatOpponent));
	}

	public static void craftingStarted(
			GameTelemetryRecorder recorder,
			UUID characterId,
			String profession,
			String recipeCode) {
		recorder.record(
				TelemetryCategory.CRAFTING,
				TelemetryEventType.CRAFTING_STARTED,
				characterId,
				TelemetryPayload.of("profession", profession, "recipeCode", recipeCode));
	}

	public static void craftingClaimed(
			GameTelemetryRecorder recorder,
			UUID characterId,
			String profession,
			String recipeCode,
			ItemRarity rarity,
			int quantity) {
		recorder.record(
				TelemetryCategory.CRAFTING,
				TelemetryEventType.CRAFTING_CLAIMED,
				characterId,
				TelemetryPayload.of(
						"profession", profession,
						"recipeCode", recipeCode,
						"rarity", rarity == null ? "NONE" : rarity.name(),
						"quantity", quantity));
	}

	public static void professionRankUp(
			GameTelemetryRecorder recorder,
			UUID characterId,
			String profession,
			int rank) {
		recorder.record(
				TelemetryCategory.CRAFTING,
				TelemetryEventType.PROFESSION_RANK_UP,
				characterId,
				TelemetryPayload.of("profession", profession, "rank", rank));
	}

	public static void itemSalvaged(
			GameTelemetryRecorder recorder,
			UUID characterId,
			String itemCode,
			ItemRarity rarity,
			Map<String, Integer> materials) {
		recorder.record(
				TelemetryCategory.CRAFTING,
				TelemetryEventType.ITEM_SALVAGED,
				characterId,
				TelemetryPayload.of(
						"itemCode", itemCode,
						"rarity", rarity == null ? "NONE" : rarity.name(),
						"materials", Map.copyOf(materials)));
	}

	private static int damageFrom(String message) {
		if (message == null) {
			return 0;
		}
		Matcher matcher = DAMAGE.matcher(message);
		if (matcher.find()) {
			return Integer.parseInt(matcher.group(1));
		}
		return 0;
	}

	private static String techniqueCodeFrom(String message) {
		if (message == null || !message.startsWith("You use ") || !message.endsWith(".")) {
			return "UNKNOWN";
		}
		String body = message.substring("You use ".length(), message.length() - 1).trim();
		return body.replace(' ', '_').toUpperCase(Locale.ROOT);
	}

	public record CombatLogLine(CombatEventType type, String message) {
	}
}
