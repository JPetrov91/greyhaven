package com.example.game.quest.domain;

public record QuestObjectiveSpec(
		QuestObjectiveType type,
		String targetCode,
		int requiredAmount,
		boolean consumeOnTurnIn
) {
}
