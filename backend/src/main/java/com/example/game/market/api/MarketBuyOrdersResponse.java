package com.example.game.market.api;

import java.util.List;

public record MarketBuyOrdersResponse(
		List<MarketBuyOrderResponse> orders,
		boolean truncated,
		int page,
		int size,
		long total
) {
}
