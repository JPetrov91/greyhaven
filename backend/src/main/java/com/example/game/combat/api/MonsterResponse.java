package com.example.game.combat.api;

import java.util.UUID;

public record MonsterResponse(
		UUID id,
		String code,
		String name,
		int level,
		int maxHealth,
		String archetype,
		String tier
) {
}
