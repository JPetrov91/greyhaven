package com.example.game.expedition.api;

import java.util.List;

public record ExpeditionRewardsResponse(
		int xp,
		int gold,
		int injuryDamage,
		List<ExpeditionRewardItemResponse> items
) {
}
