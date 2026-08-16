package com.example.game.quest.domain;

import java.util.Set;

public final class QuestBoardRules {

	private QuestBoardRules() {
	}

	public static boolean visibleOnBoard(
			boolean enabled,
			boolean repeatable,
			QuestStatus persistedStatus,
			String prerequisiteQuestCode,
			Set<String> completedQuestCodes) {
		if (!enabled) {
			return false;
		}
		if (prerequisiteQuestCode != null && !prerequisiteQuestCode.isBlank()
				&& !completedQuestCodes.contains(prerequisiteQuestCode)) {
			return false;
		}
		if (persistedStatus == QuestStatus.COMPLETED && !repeatable) {
			return false;
		}
		return true;
	}

	public static QuestBoardListState listState(
			int characterLevel,
			int minLevel,
			QuestStatus persistedStatus) {
		if (persistedStatus == QuestStatus.ACTIVE) {
			return QuestBoardListState.ACTIVE;
		}
		if (persistedStatus == QuestStatus.READY_TO_TURN_IN) {
			return QuestBoardListState.READY_TO_TURN_IN;
		}
		if (persistedStatus == QuestStatus.COMPLETED) {
			return QuestBoardListState.COMPLETED;
		}
		if (characterLevel < minLevel) {
			return QuestBoardListState.UNAVAILABLE;
		}
		return QuestBoardListState.AVAILABLE;
	}
}
