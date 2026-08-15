package com.example.game.quest.domain;

import java.util.UUID;

public record ExpeditionCompletedFact(String expeditionType, UUID expeditionId) implements QuestProgressFact {

	@Override
	public QuestProgressSourceKind dedupeKind() {
		return QuestProgressSourceKind.EXPEDITION;
	}

	@Override
	public String dedupeId() {
		return expeditionId.toString();
	}
}
