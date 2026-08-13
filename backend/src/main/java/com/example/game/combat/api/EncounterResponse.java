package com.example.game.combat.api;

import java.util.UUID;

import com.example.game.combat.domain.EncounterStatus;

public record EncounterResponse(
		UUID id,
		EncounterStatus status,
		MonsterResponse monster
) {
}
