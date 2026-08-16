package com.example.game.quest.domain;

public final class SearchLocationObjectiveHandler implements ObjectiveHandler {

	@Override
	public boolean supports(QuestObjectiveType type) {
		return type == QuestObjectiveType.SEARCH_LOCATION;
	}

	@Override
	public boolean apply(
			QuestObjectiveSpec spec,
			ObjectiveProgress progress,
			QuestProgressFact fact,
			ItemQuantitySource items) {
		if (!(fact instanceof LocationSearchedFact search) || !spec.targetCode().equals(search.locationCode())) {
			return false;
		}
		return progress.add(1, spec.requiredAmount());
	}
}
