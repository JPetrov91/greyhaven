package com.example.game.combat.domain;

import java.util.ArrayList;
import java.util.List;

import com.example.game.shared.domain.RandomProvider;

/**
 * Pure loot rolls from definition tables.
 */
public final class LootGenerator {

	private LootGenerator() {
	}

	public static List<LootDrop> generate(List<LootTableEntry> table, RandomProvider random) {
		List<LootDrop> drops = new ArrayList<>();
		if (table == null || table.isEmpty()) {
			return drops;
		}
		for (LootTableEntry entry : table) {
			if (random.chancePercent(entry.dropChancePercent())) {
				int quantity = random.nextInt(entry.quantityMin(), entry.quantityMax());
				drops.add(new LootDrop(entry.itemDefinitionId(), entry.itemCode(), quantity));
			}
		}
		return List.copyOf(drops);
	}

	public static int rollGold(int goldMin, int goldMax, RandomProvider random) {
		return random.nextInt(goldMin, goldMax);
	}
}
