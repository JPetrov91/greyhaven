package com.example.game.quest.domain;

public final class CraftItemObjectiveHandler implements ObjectiveHandler {

	@Override
	public boolean supports(QuestObjectiveType type) {
		return type == QuestObjectiveType.CRAFT_ITEM;
	}

	@Override
	public boolean apply(
			QuestObjectiveSpec spec,
			ObjectiveProgress progress,
			QuestProgressFact fact,
			ItemQuantitySource items) {
		if (!(fact instanceof CraftClaimedFact craft) || !spec.targetCode().equals(craft.recipeCode())) {
			return false;
		}
		return progress.add(1, spec.requiredAmount());
	}
}
