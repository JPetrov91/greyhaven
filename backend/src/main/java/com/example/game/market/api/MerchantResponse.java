package com.example.game.market.api;

import java.util.List;
import java.util.UUID;

public record MerchantResponse(
		UUID id,
		String code,
		String name,
		String title,
		String description,
		String merchantType,
		String portraitCode,
		List<MerchantStockItemResponse> stock
) {
}
