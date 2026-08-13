package com.example.game.combat.application;

import java.util.UUID;

public record EncounterSearchView(
		boolean found,
		UUID encounterId,
		MonsterView monster
) {
	public static EncounterSearchView nothing() {
		return new EncounterSearchView(false, null, null);
	}

	public static EncounterSearchView found(UUID encounterId, MonsterView monster) {
		return new EncounterSearchView(true, encounterId, monster);
	}
}
