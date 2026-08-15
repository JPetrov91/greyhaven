package com.example.game.quest.domain;

import java.util.Set;

public final class QuestAvailability {

	private QuestAvailability() {
	}

	public static boolean isAvailable(
			int characterLevel,
			int minLevel,
			String prerequisiteQuestCode,
			Set<String> completedQuestCodes,
			boolean hasOpenOrLockedQuest) {
		if (hasOpenOrLockedQuest) {
			return false;
		}
		if (characterLevel < minLevel) {
			return false;
		}
		if (prerequisiteQuestCode != null && !prerequisiteQuestCode.isBlank()
				&& !completedQuestCodes.contains(prerequisiteQuestCode)) {
			return false;
		}
		return true;
	}
}
