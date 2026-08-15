package com.example.game.quest.domain;

public final class CollectObjectiveHandler implements ObjectiveHandler {

	@Override
	public boolean supports(QuestObjectiveType type) {
		return type == QuestObjectiveType.COLLECT;
	}

	@Override
	public boolean apply(
			QuestObjectiveSpec spec,
			ObjectiveProgress progress,
			QuestProgressFact fact,
			ItemQuantitySource items) {
		if (!(fact instanceof InventoryChangedFact) && !(fact instanceof ItemsGrantedFact granted
				&& spec.targetCode().equals(granted.itemCode()))) {
			return false;
		}
		return progress.setAmount(items.unreservedQuantity(spec.targetCode()), spec.requiredAmount());
	}
}
