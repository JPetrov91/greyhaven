package com.example.game.combat.application;

import java.util.UUID;

public record EncounterClosedEvent(
		UUID characterId,
		UUID encounterId,
		EncounterCloseReason reason
) {
}
