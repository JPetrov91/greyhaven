package com.example.game.combat.application;

import java.util.List;

public record CombatRewardsView(
		int xp,
		int gold,
		List<CombatRewardItemView> items
) {
}
