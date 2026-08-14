package com.example.game.inventory.application;

public record StatDeltaView(
		String stat,
		int equippedValue,
		int candidateValue,
		int delta
) {
}
