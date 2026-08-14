package com.example.game.market.api;

import java.util.UUID;

public record MerchantSaleResponse(
		UUID itemInstanceId,
		String itemCode,
		String itemName,
		int quantity,
		int goldAwarded,
		int goldRemaining
) {
}
