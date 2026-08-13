package com.example.game.market.application;

import java.time.Instant;
import java.util.UUID;

import com.example.game.item.domain.ItemRarity;
import com.example.game.item.domain.ItemType;
import com.example.game.market.domain.MarketListingStatus;

public record MarketListingView(
		UUID id,
		UUID sellerCharacterId,
		String sellerName,
		UUID itemInstanceId,
		String itemCode,
		String itemName,
		ItemType itemType,
		ItemRarity rarity,
		int quantity,
		int price,
		MarketListingStatus status,
		Instant createdAt,
		Instant soldAt,
		boolean ownListing
) {
}
