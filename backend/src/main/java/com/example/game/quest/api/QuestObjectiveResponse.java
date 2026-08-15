package com.example.game.quest.api;

public record QuestObjectiveResponse(
		String type,
		String targetCode,
		int requiredAmount,
		int currentAmount,
		boolean completed,
		String displayText,
		boolean consumeOnTurnIn
) {
}
