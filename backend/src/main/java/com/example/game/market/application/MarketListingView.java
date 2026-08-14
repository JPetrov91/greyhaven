package com.example.game.market.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.example.game.inventory.application.ItemAffixView;
import com.example.game.item.domain.ItemRarity;
import com.example.game.item.domain.ItemType;
import com.example.game.item.domain.WeaponFamily;
import com.example.game.market.domain.MarketListingStatus;

public record MarketListingView(
		UUID id,
		UUID sellerCharacterId,
		String sellerName,
		UUID itemInstanceId,
		UUID itemDefinitionId,
		String itemCode,
		String itemName,
		String displayName,
		ItemType itemType,
		ItemRarity rarity,
		WeaponFamily weaponFamily,
		int requiredLevel,
		int quantity,
		int price,
		int listingFeePaid,
		Integer saleFeePaid,
		MarketListingStatus status,
		Instant createdAt,
		Instant soldAt,
		boolean ownListing,
		List<ItemAffixView> affixes
) {
}
