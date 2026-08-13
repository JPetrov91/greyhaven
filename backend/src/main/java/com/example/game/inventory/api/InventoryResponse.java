package com.example.game.inventory.api;

import java.util.List;

import com.example.game.character.api.DerivedStatsResponse;

public record InventoryResponse(
		int capacity,
		int usedSlots,
		List<InventoryItemResponse> items,
		EquipmentResponse equipment,
		DerivedStatsResponse derivedStats
) {
}
