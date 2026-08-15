package com.example.game.quest.domain;

import java.util.UUID;

public record CraftClaimedFact(String recipeCode, UUID jobId) implements QuestProgressFact {

	@Override
	public QuestProgressSourceKind dedupeKind() {
		return QuestProgressSourceKind.CRAFT_JOB;
	}

	@Override
	public String dedupeId() {
		return jobId.toString();
	}
}
