package com.example.game.combat.api;

import java.util.UUID;

public record EncounterSearchResponse(
		boolean found,
		UUID encounterId,
		MonsterResponse monster,
		String flavour
) {
}
