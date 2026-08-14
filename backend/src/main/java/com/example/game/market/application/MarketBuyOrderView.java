package com.example.game.market.application;

import java.time.Instant;
import java.util.UUID;

import com.example.game.item.domain.ItemType;
import com.example.game.market.domain.MarketBuyOrderStatus;

public record MarketBuyOrderView(
		UUID id,
		UUID buyerCharacterId,
		String buyerName,
		UUID itemDefinitionId,
		String itemCode,
		String itemName,
		ItemType itemType,
		int remainingQuantity,
		int originalQuantity,
		int maxUnitPrice,
		int reservedGold,
		int postingFeePaid,
		MarketBuyOrderStatus status,
		Instant createdAt,
		boolean ownOrder
) {
}
