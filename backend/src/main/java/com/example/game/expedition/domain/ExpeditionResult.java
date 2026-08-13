package com.example.game.expedition.domain;

import java.util.List;

/**
 * One-shot resolution outcome for an expedition. Persisted so repeated inspect/claim cannot reroll.
 */
public record ExpeditionResult(
		int xp,
		int gold,
		int injuryDamage,
		List<ExpeditionLootDrop> items
) {

	public ExpeditionResult {
		if (xp < 0) {
			throw new IllegalArgumentException("xp must be non-negative");
		}
		if (gold < 0) {
			throw new IllegalArgumentException("gold must be non-negative");
		}
		if (injuryDamage < 0) {
			throw new IllegalArgumentException("injuryDamage must be non-negative");
		}
		items = items == null ? List.of() : List.copyOf(items);
	}
}
