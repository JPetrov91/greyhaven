package com.example.game.quest.domain;

public final class AcquireItemObjectiveHandler implements ObjectiveHandler {

	@Override
	public boolean supports(QuestObjectiveType type) {
		return type == QuestObjectiveType.ACQUIRE_ITEM;
	}

	@Override
	public boolean apply(
			QuestObjectiveSpec spec,
			ObjectiveProgress progress,
			QuestProgressFact fact,
			ItemQuantitySource items) {
		if (!(fact instanceof ItemsGrantedFact granted) || !spec.targetCode().equals(granted.itemCode())) {
			return false;
		}
		return progress.add(granted.quantity(), spec.requiredAmount());
	}
}
