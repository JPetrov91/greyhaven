package com.example.game.crafting.application;

import com.example.game.crafting.domain.Profession;

public record ProfessionView(
		Profession profession,
		int rank,
		int xp,
		int xpToNextRank,
		boolean maxRank
) {
}
