package com.example.game.combat.application;

import com.example.game.combat.domain.CombatEventType;

public record CombatEventView(
		int roundNumber,
		int sequenceNumber,
		CombatEventType type,
		String message
) {
}
