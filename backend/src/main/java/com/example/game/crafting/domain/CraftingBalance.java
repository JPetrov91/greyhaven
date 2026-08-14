package com.example.game.crafting.domain;

import com.example.game.item.domain.ItemRarity;
import com.example.game.shared.balance.GameBalance;
import com.example.game.shared.balance.GameBalanceCatalog;

/**
 * Profession ranks, recipe XP, salvage yields, and crafting rarity shifts.
 */
public final class CraftingBalance {

	private static final GameBalance.Crafting VALUES = GameBalanceCatalog.get().crafting();

	public static final int STARTING_RANK = 1;
	public static final int MAX_RANK = VALUES.maxRank();
	public static final int XP_PER_RECIPE = VALUES.xpPerRecipe();
	public static final int RANK_RARITY_BONUS_PER_RANK = VALUES.rankRarityBonusPerRank();

	private CraftingBalance() {
	}

	public static int cumulativeXpForRank(int rank) {
		if (rank < STARTING_RANK || rank > MAX_RANK) {
			throw new IllegalArgumentException("rank out of range");
		}
		return VALUES.cumulativeXpToReachRank()[rank];
	}

	public static int salvageMultiplier(ItemRarity rarity) {
		return switch (rarity) {
			case COMMON -> VALUES.commonSalvageMultiplier();
			case UNCOMMON -> VALUES.uncommonSalvageMultiplier();
			case RARE -> VALUES.rareSalvageMultiplier();
			case EPIC -> VALUES.epicSalvageMultiplier();
		};
	}
}
