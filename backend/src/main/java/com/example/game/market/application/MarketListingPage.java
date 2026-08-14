package com.example.game.market.application;

import java.util.List;

public record MarketListingPage(
		List<MarketListingView> listings,
		int page,
		int size,
		long total,
		boolean truncated
) {
}
