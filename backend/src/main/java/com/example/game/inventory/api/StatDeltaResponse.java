package com.example.game.inventory.api;

public record StatDeltaResponse(
		String stat,
		int equippedValue,
		int candidateValue,
		int delta
) {
}
