package com.example.game.market.domain;

import com.example.game.shared.balance.GameBalance;
import com.example.game.shared.balance.GameBalanceCatalog;

/**
 * Player-market fees and browse page sizes. Loaded from {@code game-balance.yml}.
 */
public final class MarketBalance {

	private static final GameBalance.Market VALUES = GameBalanceCatalog.get().market();

	public static final double LISTING_FEE_PERCENT = VALUES.listingFeePercent();
	public static final double SALE_FEE_PERCENT = VALUES.saleFeePercent();
	public static final int LISTING_PAGE_SIZE = VALUES.listingPageSize();
	public static final int MAX_LISTING_PAGE_SIZE = VALUES.maxListingPageSize();

	private MarketBalance() {
	}
}
