package com.example.game.quest.domain;

public final class CompleteDungeonObjectiveHandler implements ObjectiveHandler {

	@Override
	public boolean supports(QuestObjectiveType type) {
		return type == QuestObjectiveType.COMPLETE_DUNGEON;
	}

	@Override
	public boolean apply(
			QuestObjectiveSpec spec,
			ObjectiveProgress progress,
			QuestProgressFact fact,
			ItemQuantitySource items) {
		if (!(fact instanceof DungeonCompletedFact dungeon) || !spec.targetCode().equals(dungeon.dungeonCode())) {
			return false;
		}
		return progress.add(1, spec.requiredAmount());
	}
}
