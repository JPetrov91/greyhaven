package com.example.game.market.application;

import java.util.UUID;

public record MerchantPurchaseView(
		UUID merchantId,
		UUID itemDefinitionId,
		String itemCode,
		String itemName,
		int quantity,
		int pricePaid,
		int goldRemaining
) {
}
