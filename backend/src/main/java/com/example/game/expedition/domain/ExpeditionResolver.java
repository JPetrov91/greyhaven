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
		maybeAdd(drops, ItemCodes.WOLF_PELT, ExpeditionBalance.materialChancePercent(strategy), 1, 2, random);
		maybeAdd(drops, ItemCodes.HEALING_POTION, ExpeditionBalance.potionChancePercent(strategy), 1, 1, random);
		maybeAdd(drops, ItemCodes.OLD_DAGGER, ExpeditionBalance.commonGearChancePercent(strategy), 1, 1, random);
		maybeAdd(drops, ItemCodes.IRON_AXE, ExpeditionBalance.rareGearChancePercent(strategy), 1, 1, random);
		maybeAdd(drops, ItemCodes.LEATHER_ARMOR, ExpeditionBalance.rareGearChancePercent(strategy), 1, 1, random);
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
}
