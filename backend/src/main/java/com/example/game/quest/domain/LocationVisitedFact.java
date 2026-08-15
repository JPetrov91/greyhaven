package com.example.game.quest.domain;

public record LocationVisitedFact(String locationCode) implements QuestProgressFact {

	@Override
	public QuestProgressSourceKind dedupeKind() {
		return null;
	}

	@Override
	public String dedupeId() {
		return null;
	}
}
