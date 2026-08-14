package com.example.game.combat.api;

import java.util.List;

public record CombatRewardsResponse(
		int xp,
		int gold,
		int previousLevel,
		int newLevel,
		int attributePointsGained,
		List<CombatRewardItemResponse> items
) {
}
