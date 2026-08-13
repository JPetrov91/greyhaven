package com.example.game.market.application;

import java.util.List;

/**
 * Newest active listings for one browse request, plus whether older rows were omitted.
 */
public record MarketListingPage(
		List<MarketListingView> listings,
		boolean truncated
) {

	static MarketListingPage ofNewest(List<MarketListingView> fetchedNewestFirst, int limit) {
		if (limit < 1) {
			throw new IllegalArgumentException("limit must be positive");
		}
		if (fetchedNewestFirst.size() > limit) {
			return new MarketListingPage(List.copyOf(fetchedNewestFirst.subList(0, limit)), true);
		}
		return new MarketListingPage(List.copyOf(fetchedNewestFirst), false);
	}
}
