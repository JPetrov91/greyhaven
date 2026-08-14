package com.example.game.inventory.api;

import java.util.List;
import java.util.UUID;

public record ItemComparisonResponse(
		String slot,
		UUID equippedItemId,
		String verdict,
		List<StatDeltaResponse> deltas
) {
}
