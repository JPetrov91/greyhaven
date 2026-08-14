package com.example.game.inventory.api;

public record ItemAffixResponse(
		String code,
		String kind,
		String displayName,
		String stat,
		int magnitude
) {
}
