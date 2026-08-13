package com.example.game.market.domain;

import java.util.UUID;

/**
 * Pure marketplace rules kept out of persistence and HTTP types.
 */
public final class MarketRules {

	private MarketRules() {
	}

	public static boolean isValidPrice(int price) {
		return price >= 1;
	}

	public static boolean isValidQuantity(int quantity, int available) {
		return quantity >= 1 && quantity <= available;
	}

	public static int availableQuantity(int stackQuantity, int reservedQuantity) {
		if (stackQuantity < 0 || reservedQuantity < 0) {
			throw new IllegalArgumentException("quantities must be non-negative");
		}
		return Math.max(0, stackQuantity - reservedQuantity);
	}

	public static boolean isOwnListing(UUID buyerCharacterId, UUID sellerCharacterId) {
		if (buyerCharacterId == null || sellerCharacterId == null) {
			throw new IllegalArgumentException("character ids are required");
		}
		return buyerCharacterId.equals(sellerCharacterId);
	}
}
