package com.example.game.market.domain;

/**
 * Gold destroyed by listing and sale fees. Never credits a character.
 */
public final class MarketFeeCalculator {

	private MarketFeeCalculator() {
	}

	public static int fee(int price, double percent) {
		if (price < 1) {
			throw new IllegalArgumentException("price must be at least 1");
		}
		if (percent < 0) {
			throw new IllegalArgumentException("percent must be non-negative");
		}
		if (percent == 0) {
			return 0;
		}
		return (int) Math.ceil(price * percent);
	}

	public static int listingFee(int price) {
		return fee(price, MarketBalance.LISTING_FEE_PERCENT);
	}

	public static int buyOrderPostingFee(int escrowGold) {
		return listingFee(escrowGold);
	}

	public static int saleFee(int price) {
		return fee(price, MarketBalance.SALE_FEE_PERCENT);
	}

	public static int sellerProceeds(int price) {
		return Math.max(0, price - saleFee(price));
	}
}
