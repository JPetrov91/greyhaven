package com.example.game.quest.application;

import com.example.game.quest.domain.QuestObjectiveType;

public record QuestObjectiveView(
		String type,
		String targetCode,
		int requiredAmount,
		int currentAmount,
		boolean completed,
		String displayText,
		boolean consumeOnTurnIn,
		String actionHint
) {

	public static QuestObjectiveView of(
			QuestObjectiveType type,
			String targetCode,
			int requiredAmount,
			int currentAmount,
			boolean completed,
			String displayText,
			boolean consumeOnTurnIn) {
		return new QuestObjectiveView(
				type.name(),
				targetCode,
				requiredAmount,
				currentAmount,
				completed,
				displayText,
				consumeOnTurnIn,
				com.example.game.quest.domain.QuestObjectiveHints.hintOf(type).name());
	}
}
