package com.example.game.quest.domain;

public final class WinArenaMatchObjectiveHandler implements ObjectiveHandler {

	@Override
	public boolean supports(QuestObjectiveType type) {
		return type == QuestObjectiveType.WIN_ARENA_MATCH;
	}

	@Override
	public boolean apply(
			QuestObjectiveSpec spec,
			ObjectiveProgress progress,
			QuestProgressFact fact,
			ItemQuantitySource items) {
		if (!(fact instanceof ArenaWonFact arena) || !spec.targetCode().equals(arena.matchKind())) {
			return false;
		}
		return progress.add(1, spec.requiredAmount());
	}
}
