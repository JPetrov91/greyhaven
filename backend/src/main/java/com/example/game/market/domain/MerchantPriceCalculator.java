package com.example.game.market.domain;

import com.example.game.item.domain.ItemRarity;

/**
 * Deterministic NPC merchant prices. Buy is what the merchant pays the player; sell is the shop
 * price charged to the player. Rarity is not reapplied because catalog {@code baseValue} already
 * encodes it; affixes still raise the merchant buy offer.
 */
public final class MerchantPriceCalculator {

	private MerchantPriceCalculator() {
	}

	public static int merchantSellPrice(int baseValue, ItemRarity rarity) {
		requireRarity(rarity);
		return scaledPrice(baseValue, 0, MerchantBalance.SELL_MULTIPLIER);
	}

	public static int merchantBuyPrice(int baseValue, ItemRarity rarity, int affixCount) {
		requireRarity(rarity);
		return scaledPrice(baseValue, affixCount, MerchantBalance.BUY_MULTIPLIER);
	}

	static int scaledPrice(int baseValue, int affixCount, double multiplier) {
		if (baseValue < 0) {
			throw new IllegalArgumentException("base value must be non-negative");
		}
		if (affixCount < 0) {
			throw new IllegalArgumentException("affix count must be non-negative");
		}
		if (baseValue == 0) {
			return 0;
		}
		double affixModifier = 1.0 + (affixCount * MerchantBalance.AFFIX_VALUE_PER_AFFIX);
		int rounded = (int) Math.round(baseValue * affixModifier * multiplier);
		return Math.max(1, rounded);
	}

	private static void requireRarity(ItemRarity rarity) {
		if (rarity == null) {
			throw new IllegalArgumentException("rarity is required");
		}
	}
}
