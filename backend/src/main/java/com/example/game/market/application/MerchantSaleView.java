package com.example.game.market.application;

import java.util.UUID;

public record MerchantSaleView(
		UUID itemInstanceId,
		String itemCode,
		String itemName,
		int quantity,
		int goldAwarded,
		int goldRemaining
) {
}
