package com.example.game.quest.domain;

public record TalkFact(String npcCode) implements QuestProgressFact {

	@Override
	public QuestProgressSourceKind dedupeKind() {
		return null;
	}

	@Override
	public String dedupeId() {
		return null;
	}
}
