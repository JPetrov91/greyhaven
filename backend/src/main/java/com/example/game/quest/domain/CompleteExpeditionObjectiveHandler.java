package com.example.game.quest.domain;

public final class CompleteExpeditionObjectiveHandler implements ObjectiveHandler {

	@Override
	public boolean supports(QuestObjectiveType type) {
		return type == QuestObjectiveType.COMPLETE_EXPEDITION;
	}

	@Override
	public boolean apply(
			QuestObjectiveSpec spec,
			ObjectiveProgress progress,
			QuestProgressFact fact,
			ItemQuantitySource items) {
		if (!(fact instanceof ExpeditionCompletedFact expedition)
				|| !spec.targetCode().equals(expedition.expeditionType())) {
			return false;
		}
		return progress.add(1, spec.requiredAmount());
	}
}
