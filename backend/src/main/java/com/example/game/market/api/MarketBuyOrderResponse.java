package com.example.game.market.api;

import java.time.Instant;
import java.util.UUID;

public record MarketBuyOrderResponse(
		UUID id,
		UUID buyerCharacterId,
		String buyerName,
		UUID itemDefinitionId,
		String itemCode,
		String itemName,
		String itemType,
		int remainingQuantity,
		int originalQuantity,
		int maxUnitPrice,
		int reservedGold,
		int postingFeePaid,
		String status,
		Instant createdAt,
		boolean ownOrder
) {
}
