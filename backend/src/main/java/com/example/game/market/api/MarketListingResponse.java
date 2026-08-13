package com.example.game.market.api;

import java.time.Instant;
import java.util.UUID;

public record MarketListingResponse(
		UUID id,
		UUID sellerCharacterId,
		String sellerName,
		UUID itemInstanceId,
		String itemCode,
		String itemName,
		String itemType,
		String rarity,
		int quantity,
		int price,
		String status,
		Instant createdAt,
		Instant soldAt,
		boolean ownListing
) {
}
