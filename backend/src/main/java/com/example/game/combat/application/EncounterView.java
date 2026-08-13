package com.example.game.combat.application;

import java.util.UUID;

import com.example.game.combat.domain.EncounterStatus;

public record EncounterView(
		UUID id,
		EncounterStatus status,
		MonsterView monster
) {
}
