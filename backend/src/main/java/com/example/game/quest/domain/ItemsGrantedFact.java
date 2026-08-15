package com.example.game.quest.domain;

public record ItemsGrantedFact(String itemCode, int quantity, String grantKey) implements QuestProgressFact {

	@Override
	public QuestProgressSourceKind dedupeKind() {
		return grantKey == null || grantKey.isBlank() ? null : QuestProgressSourceKind.ITEM_GRANT;
	}

	@Override
	public String dedupeId() {
		return grantKey;
	}
}
