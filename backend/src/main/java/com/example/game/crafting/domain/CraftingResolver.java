package com.example.game.crafting.domain;

import java.time.Duration;
import java.time.Instant;

import com.example.game.item.domain.AffixCatalog;
import com.example.game.item.domain.GeneratedItem;
import com.example.game.item.domain.ItemDefinitionData;
import com.example.game.item.domain.ItemGenerator;
import com.example.game.item.domain.ItemRarity;
import com.example.game.shared.domain.RandomProvider;

/**
 * Plans a crafting result once. Equipment uses {@link ItemGenerator} with a recipe rarity band.
 */
public final class CraftingResolver {

	private CraftingResolver() {
	}

	public record PlannedCraft(
			Instant completesAt,
			GeneratedItem generated,
			int professionXp
	) {
	}

	public static PlannedCraft resolve(
			Instant startedAt,
			int durationSeconds,
			ItemDefinitionData output,
			AffixCatalog catalog,
			RandomProvider random,
			ItemRarity minRarity,
			ItemRarity maxRarity,
			int professionRank,
			int professionXp) {
		if (startedAt == null) {
			throw new IllegalArgumentException("startedAt is required");
		}
		if (durationSeconds < 1) {
			throw new IllegalArgumentException("durationSeconds must be positive");
		}
		if (output == null) {
			throw new IllegalArgumentException("output is required");
		}
		GeneratedItem generated;
		if (output.type().isEquippable()) {
			generated = ItemGenerator.generateCrafted(
					output,
					catalog,
					random,
					minRarity,
					maxRarity,
					professionRank,
					CraftingBalance.RANK_RARITY_BONUS_PER_RANK);
		}
		else {
			generated = ItemGenerator.generate(output, catalog, random);
		}
		return new PlannedCraft(
				startedAt.plus(Duration.ofSeconds(durationSeconds)),
				generated,
				professionXp);
	}
}
