package com.example.game.quest.domain;

public final class VisitLocationObjectiveHandler implements ObjectiveHandler {

	@Override
	public boolean supports(QuestObjectiveType type) {
		return type == QuestObjectiveType.VISIT_LOCATION;
	}

	@Override
	public boolean apply(
			QuestObjectiveSpec spec,
			ObjectiveProgress progress,
			QuestProgressFact fact,
			ItemQuantitySource items) {
		if (!(fact instanceof LocationVisitedFact visit) || !spec.targetCode().equals(visit.locationCode())) {
			return false;
		}
		return progress.setAmount(1, spec.requiredAmount());
	}
}
