package com.example.game.expedition.domain;

/**
 * A planned loot line keyed by stable item code (mapped to definitions at persistence time).
 */
public record ExpeditionLootDrop(String itemCode, int quantity) {

	public ExpeditionLootDrop {
		if (itemCode == null || itemCode.isBlank()) {
			throw new IllegalArgumentException("itemCode is required");
		}
		if (quantity < 1) {
			throw new IllegalArgumentException("quantity must be positive");
		}
	}
}
