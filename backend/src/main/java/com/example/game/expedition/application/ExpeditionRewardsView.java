package com.example.game.expedition.application;

import java.util.List;

public record ExpeditionRewardsView(
		int xp,
		int gold,
		int injuryDamage,
		List<ExpeditionRewardItemView> items
) {
}
