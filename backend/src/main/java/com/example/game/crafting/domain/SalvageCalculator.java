package com.example.game.crafting.domain;

import java.util.ArrayList;
import java.util.List;

import com.example.game.item.domain.ItemRarity;
import com.example.game.item.domain.ItemType;

/**
 * Converts unwanted equipment into materials. Equipped and listed checks stay in application code.
 */
public final class SalvageCalculator {

	private SalvageCalculator() {
	}

	public record CatalogLine(String resultItemCode, int baseQuantity) {
	}

	public record SalvageOutput(String itemCode, int quantity) {
	}

	public static boolean isSalvageable(ItemType type) {
		return type != null && type.isEquippable();
	}

	public static List<SalvageOutput> calculate(ItemType type, ItemRarity rarity, List<CatalogLine> catalog) {
		if (!isSalvageable(type)) {
			throw new IllegalArgumentException("only equipment can be salvaged");
		}
		if (rarity == null) {
			throw new IllegalArgumentException("rarity is required");
		}
		if (catalog == null || catalog.isEmpty()) {
			throw new IllegalArgumentException("salvage catalog is required");
		}
		int multiplier = CraftingBalance.salvageMultiplier(rarity);
		List<SalvageOutput> outputs = new ArrayList<>();
		for (CatalogLine line : catalog) {
			int quantity = Math.multiplyExact(line.baseQuantity(), multiplier);
			if (quantity > 0) {
				outputs.add(new SalvageOutput(line.resultItemCode(), quantity));
			}
		}
		return List.copyOf(outputs);
	}
}
