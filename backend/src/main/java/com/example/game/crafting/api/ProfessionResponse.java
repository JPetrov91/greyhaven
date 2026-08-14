package com.example.game.crafting.api;

import com.example.game.crafting.domain.Profession;

public record ProfessionResponse(
		Profession profession,
		int rank,
		int xp,
		int xpToNextRank,
		boolean maxRank
) {
}
