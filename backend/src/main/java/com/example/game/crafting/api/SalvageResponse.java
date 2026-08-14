package com.example.game.crafting.api;

import java.util.List;

public record SalvageResponse(
		String sourceItemCode,
		String sourceItemName,
		List<SalvageResultResponse> results
) {
}
