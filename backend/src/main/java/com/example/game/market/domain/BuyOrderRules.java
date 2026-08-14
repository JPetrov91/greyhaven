package com.example.game.market.domain;

import java.util.UUID;

/**
 * Buy-order escrow and partial-fill math. Persistence and locking stay in the application layer.
 */
public final class BuyOrderRules {

	private BuyOrderRules() {
	}

	public record Fill(
			int filledQuantity,
			int remainingQuantity,
			int grossGold,
			int reservedGoldAfter,
			boolean completed
	) {
	}

	public static int escrowGold(int quantity, int maxUnitPrice) {
		if (quantity < 1 || maxUnitPrice < 1) {
			throw new IllegalArgumentException("quantity and max unit price must be at least 1");
		}
		return Math.multiplyExact(quantity, maxUnitPrice);
	}

	public static boolean isOwnOrder(UUID buyerCharacterId, UUID sellerCharacterId) {
		if (buyerCharacterId == null || sellerCharacterId == null) {
			throw new IllegalArgumentException("character ids are required");
		}
		return buyerCharacterId.equals(sellerCharacterId);
	}

	public static Fill applyFill(int remainingQuantity, int reservedGold, int maxUnitPrice, int fillQuantity) {
		if (remainingQuantity < 1) {
			throw new IllegalArgumentException("order has no remaining quantity");
		}
		if (fillQuantity < 1 || fillQuantity > remainingQuantity) {
			throw new IllegalArgumentException("fill quantity is not available");
		}
		int gross = escrowGold(fillQuantity, maxUnitPrice);
		if (reservedGold < gross) {
			throw new IllegalArgumentException("reserved gold is insufficient for this fill");
		}
		int remaining = remainingQuantity - fillQuantity;
		int reservedAfter = reservedGold - gross;
		return new Fill(fillQuantity, remaining, gross, reservedAfter, remaining == 0);
	}
}
