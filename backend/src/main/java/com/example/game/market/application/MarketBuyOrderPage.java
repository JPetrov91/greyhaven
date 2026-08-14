package com.example.game.market.application;

import java.util.List;

public record MarketBuyOrderPage(
		List<MarketBuyOrderView> orders,
		int page,
		int size,
		long total,
		boolean truncated
) {
}
