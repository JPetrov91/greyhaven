package com.example.game.market.api;

import java.util.UUID;

public record MerchantPurchaseResponse(
		UUID merchantId,
		UUID itemDefinitionId,
		String itemCode,
		String itemName,
		int quantity,
		int pricePaid,
		int goldRemaining
) {
}
