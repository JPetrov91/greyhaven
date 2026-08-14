package com.example.game.combat.application;

import java.util.List;

public record CombatRewardsView(
		int xp,
		int gold,
		int previousLevel,
		int newLevel,
		int attributePointsGained,
		List<CombatRewardItemView> items
) {
}
