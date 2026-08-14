package com.example.game.market.domain;

import com.example.game.item.domain.ItemRarity;
import com.example.game.shared.balance.GameBalance;
import com.example.game.shared.balance.GameBalanceCatalog;

/**
 * NPC merchant buy/sell multipliers. Loaded from {@code game-balance.yml}.
 */
public final class MerchantBalance {

	private static final GameBalance.Market VALUES = GameBalanceCatalog.get().market();

	public static final double BUY_MULTIPLIER = VALUES.merchantBuyMultiplier();
	public static final double SELL_MULTIPLIER = VALUES.merchantSellMultiplier();
	public static final double AFFIX_VALUE_PER_AFFIX = VALUES.affixValuePerAffix();
	public static final int MAX_PURCHASE_QUANTITY = VALUES.maxMerchantPurchaseQuantity();

	private MerchantBalance() {
	}

	/**
	 * Catalog {@code baseValue} already scales with rarity. Merchant buy/sell prices must not
	 * multiply these again.
	 */
	public static double rarityModifier(ItemRarity rarity) {
		return switch (rarity) {
			case COMMON -> VALUES.commonRarityModifier();
			case UNCOMMON -> VALUES.uncommonRarityModifier();
			case RARE -> VALUES.rareRarityModifier();
			case EPIC -> VALUES.epicRarityModifier();
		};
	}
}
