package com.example.game.expedition.domain;

import java.util.ArrayList;
import java.util.List;

import com.example.game.item.domain.ItemCodes;
import com.example.game.shared.domain.RandomProvider;

/**
 * Pure Forest Patrol resolution. Injectable {@link RandomProvider} keeps tests deterministic.
 */
public final class ExpeditionResolver {

	private ExpeditionResolver() {
	}

	public static ExpeditionResult resolve(ExpeditionType type, ExpeditionStrategy strategy, RandomProvider random) {
		if (type != ExpeditionType.FOREST_PATROL) {
			throw new IllegalArgumentException("unsupported expedition type: " + type);
		}
		if (strategy == null) {
			throw new IllegalArgumentException("strategy is required");
		}
		if (random == null) {
			throw new IllegalArgumentException("random is required");
		}

		int injuryDamage = 0;
		if (random.chancePercent(ExpeditionBalance.injuryChancePercent(strategy))) {
			injuryDamage = random.nextInt(
					ExpeditionBalance.injuryDamageMin(strategy),
					ExpeditionBalance.injuryDamageMax(strategy));
		}

		if (random.chancePercent(ExpeditionBalance.emptyHaulChancePercent(strategy))) {
			return new ExpeditionResult(0, 0, injuryDamage, List.of());
		}

		int xp = random.nextInt(ExpeditionBalance.xpMin(strategy), ExpeditionBalance.xpMax(strategy));
		int gold = random.nextInt(ExpeditionBalance.goldMin(strategy), ExpeditionBalance.goldMax(strategy));
		List<ExpeditionLootDrop> items = rollLoot(strategy, random);
		return new ExpeditionResult(xp, gold, injuryDamage, items);
	}

	private static List<ExpeditionLootDrop> rollLoot(ExpeditionStrategy strategy, RandomProvider random) {
		List<ExpeditionLootDrop> drops = new ArrayList<>();
		maybeAdd(drops, ItemCodes.WOLF_PELT, materialChance(strategy), 1, 2, random);
		maybeAdd(drops, ItemCodes.HEALING_POTION, potionChance(strategy), 1, 1, random);
		maybeAdd(drops, ItemCodes.OLD_DAGGER, commonGearChance(strategy), 1, 1, random);
		maybeAdd(drops, ItemCodes.IRON_SWORD, rareGearChance(strategy), 1, 1, random);
		maybeAdd(drops, ItemCodes.LEATHER_ARMOR, rareGearChance(strategy), 1, 1, random);
		return List.copyOf(drops);
	}

	private static void maybeAdd(
			List<ExpeditionLootDrop> drops,
			String itemCode,
			int chancePercent,
			int quantityMin,
			int quantityMax,
			RandomProvider random) {
		if (random.chancePercent(chancePercent)) {
			drops.add(new ExpeditionLootDrop(itemCode, random.nextInt(quantityMin, quantityMax)));
		}
	}

	private static int materialChance(ExpeditionStrategy strategy) {
		return switch (strategy) {
			case CAUTIOUS -> 35;
			case BALANCED -> 50;
			case AGGRESSIVE -> 65;
		};
	}

	private static int potionChance(ExpeditionStrategy strategy) {
		return switch (strategy) {
			case CAUTIOUS -> 20;
			case BALANCED -> 30;
			case AGGRESSIVE -> 40;
		};
	}

	private static int commonGearChance(ExpeditionStrategy strategy) {
		return switch (strategy) {
			case CAUTIOUS -> 8;
			case BALANCED -> 15;
			case AGGRESSIVE -> 22;
		};
	}

	private static int rareGearChance(ExpeditionStrategy strategy) {
		return switch (strategy) {
			case CAUTIOUS -> 3;
			case BALANCED -> 8;
			case AGGRESSIVE -> 14;
		};
	}
}
