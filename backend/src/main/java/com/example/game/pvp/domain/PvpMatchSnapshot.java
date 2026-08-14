package com.example.game.pvp.domain;

public record PvpMatchSnapshot(
		int version,
		PvpCombatantSnapshot attacker,
		PvpCombatantSnapshot defender,
		ArenaDefenseStrategy defense
) {
}
