package com.example.game.market.api;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MarketListingResponse(
		UUID id,
		UUID sellerCharacterId,
		String sellerName,
		UUID itemInstanceId,
		UUID itemDefinitionId,
		String itemCode,
		String itemName,
		String displayName,
		String itemType,
		String rarity,
		String weaponFamily,
		int requiredLevel,
		int quantity,
		int price,
		int listingFeePaid,
		Integer saleFeePaid,
		String status,
		Instant createdAt,
		Instant soldAt,
		boolean ownListing,
		List<ItemAffixApiResponse> affixes
) {
}
