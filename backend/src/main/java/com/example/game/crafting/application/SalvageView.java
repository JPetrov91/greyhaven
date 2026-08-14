package com.example.game.crafting.application;

import java.util.List;

public record SalvageView(
		String sourceItemCode,
		String sourceItemName,
		List<SalvageResultView> results
) {
}
