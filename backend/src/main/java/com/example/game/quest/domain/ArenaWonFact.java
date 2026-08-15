package com.example.game.quest.domain;

import java.util.UUID;

public record ArenaWonFact(String matchKind, UUID matchId) implements QuestProgressFact {

	@Override
	public QuestProgressSourceKind dedupeKind() {
		return QuestProgressSourceKind.ARENA_MATCH;
	}

	@Override
	public String dedupeId() {
		return matchId.toString();
	}
}
