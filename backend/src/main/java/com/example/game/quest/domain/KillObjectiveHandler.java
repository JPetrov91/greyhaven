package com.example.game.quest.domain;

public final class KillObjectiveHandler implements ObjectiveHandler {

	@Override
	public boolean supports(QuestObjectiveType type) {
		return type == QuestObjectiveType.KILL || type == QuestObjectiveType.DEFEAT_ENEMY;
	}

	@Override
	public boolean apply(
			QuestObjectiveSpec spec,
			ObjectiveProgress progress,
			QuestProgressFact fact,
			ItemQuantitySource items) {
		if (!(fact instanceof CombatVictoryFact victory) || !spec.targetCode().equals(victory.monsterCode())) {
			return false;
		}
		return progress.add(1, spec.requiredAmount());
	}
}
