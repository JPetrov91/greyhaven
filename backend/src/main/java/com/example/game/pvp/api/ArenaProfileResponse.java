package com.example.game.pvp.api;

import java.util.UUID;

import com.example.game.combat.domain.CombatAction;
import com.example.game.pvp.domain.ArenaDefenseStrategy;

public record ArenaProfileResponse(
		UUID characterId,
		int rating,
		int marks,
		ArenaDefenseStrategy defense,
		CombatAction[] preferredActionOptions
) {
}
