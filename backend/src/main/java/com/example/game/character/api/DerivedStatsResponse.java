package com.example.game.character.api;

public record DerivedStatsResponse(
		int physicalDamage,
		int accuracy,
		int dodge,
		int criticalChance,
		int armor
) {
}
