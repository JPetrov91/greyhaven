package com.example.game.quest.domain;

import java.util.UUID;

public record DungeonCompletedFact(String dungeonCode, UUID runId) implements QuestProgressFact {

	@Override
	public QuestProgressSourceKind dedupeKind() {
		return QuestProgressSourceKind.DUNGEON_RUN;
	}

	@Override
	public String dedupeId() {
		return runId.toString();
	}
}
