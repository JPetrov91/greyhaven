package com.example.game.market.api;

public record ItemAffixApiResponse(
		String code,
		String kind,
		String displayName,
		String stat,
		int magnitude
) {
}
