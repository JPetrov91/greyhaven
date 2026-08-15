package com.example.game.quest.domain;

public final class TalkObjectiveHandler implements ObjectiveHandler {

	@Override
	public boolean supports(QuestObjectiveType type) {
		return type == QuestObjectiveType.TALK_TO_NPC;
	}

	@Override
	public boolean apply(
			QuestObjectiveSpec spec,
			ObjectiveProgress progress,
			QuestProgressFact fact,
			ItemQuantitySource items) {
		if (!(fact instanceof TalkFact talk) || !spec.targetCode().equals(talk.npcCode())) {
			return false;
		}
		return progress.add(1, spec.requiredAmount());
	}
}
