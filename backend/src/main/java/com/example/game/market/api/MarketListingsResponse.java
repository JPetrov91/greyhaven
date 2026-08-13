package com.example.game.market.api;

import java.util.List;

public record MarketListingsResponse(
		List<MarketListingResponse> listings,
		boolean truncated
) {
}
