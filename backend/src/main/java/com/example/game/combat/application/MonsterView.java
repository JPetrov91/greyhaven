package com.example.game.combat.application;

import java.util.UUID;

public record MonsterView(
		UUID id,
		String code,
		String name,
		int level,
		int maxHealth
) {
}
