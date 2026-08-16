package com.example.game.sparring.domain;

import java.util.List;

/**
 * Catalog of Sparring Yard drill partners. Names are fixed per level; combat ratings are rolled
 * at fight start and must not pretend to be player characters.
 */
public final class SparringBots {

	public static final int MIN_LEVEL = 1;
	public static final int MAX_LEVEL = 10;
	public static final int MAX_PLAYER_LEVEL = 10;
	public static final int RANKED_ARENA_MIN_LEVEL = 11;

	private static final List<String> NAMES = List.of(
			"Green Recruit",
			"Street Sparrer",
			"Watch Cadet",
			"Yard Regular",
			"Militia Drillman",
			"Veteran Sparrer",
			"Watch Corporal",
			"Yard Sergeant",
			"Drill Champion",
			"Watch Provost");

	private SparringBots() {
	}

	public static boolean isValidBotLevel(int level) {
		return level >= MIN_LEVEL && level <= MAX_LEVEL;
	}

	public static String codeForLevel(int level) {
		requireLevel(level);
		return "SPARRING_BOT_L%02d".formatted(level);
	}

	public static String nameForLevel(int level) {
		requireLevel(level);
		return NAMES.get(level - 1);
	}

	public static boolean isBotCode(String code) {
		return code != null && code.startsWith("SPARRING_BOT_L");
	}

	public static List<SparringBotCatalogEntry> catalog() {
		return List.of(
				entry(1), entry(2), entry(3), entry(4), entry(5),
				entry(6), entry(7), entry(8), entry(9), entry(10));
	}

	private static SparringBotCatalogEntry entry(int level) {
		return new SparringBotCatalogEntry(level, nameForLevel(level), codeForLevel(level));
	}

	private static void requireLevel(int level) {
		if (!isValidBotLevel(level)) {
			throw new IllegalArgumentException("sparring bot level must be 1-10");
		}
	}
}
