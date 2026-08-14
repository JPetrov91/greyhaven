package com.example.game.pvp.application;

import java.util.UUID;

import com.example.game.combat.domain.CombatAction;
import com.example.game.pvp.domain.ArenaDefenseStrategy;

public record ArenaProfileView(
		UUID characterId,
		int rating,
		int marks,
		ArenaDefenseStrategy defense,
		CombatAction[] preferredActionOptions
) {
}
