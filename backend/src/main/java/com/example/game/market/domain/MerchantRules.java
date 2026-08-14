package com.example.game.market.domain;

/**
 * Quantity checks shared by NPC purchase and sale.
 */
public final class MerchantRules {

	private MerchantRules() {
	}

	public static boolean isValidQuantity(int quantity, int available) {
		return quantity >= 1 && quantity <= available;
	}

	public static boolean isValidPurchaseQuantity(int quantity, boolean stackable) {
		if (quantity < 1 || quantity > MerchantBalance.MAX_PURCHASE_QUANTITY) {
			return false;
		}
		return stackable || quantity == 1;
	}
}
