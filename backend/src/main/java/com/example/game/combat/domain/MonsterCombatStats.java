package com.example.game.combat.domain;

public record MonsterCombatStats(
		String name,
		int level,
		int damageMin,
		int damageMax
) {
}
