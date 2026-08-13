package com.example.game.combat.api;

import com.example.game.combat.domain.CombatEventType;

public record CombatEventResponse(
		int roundNumber,
		int sequenceNumber,
		CombatEventType type,
		String message
) {
}
