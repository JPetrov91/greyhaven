package com.example.game.quest.domain;

import java.util.UUID;

public record CombatVictoryFact(String monsterCode, UUID sessionId) implements QuestProgressFact {

	@Override
	public QuestProgressSourceKind dedupeKind() {
		return QuestProgressSourceKind.COMBAT_SESSION;
	}

	@Override
	public String dedupeId() {
		return sessionId.toString();
	}
}
