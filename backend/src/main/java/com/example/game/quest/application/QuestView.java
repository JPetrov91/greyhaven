package com.example.game.quest.application;

import java.util.List;

import com.example.game.quest.domain.QuestCategory;
import com.example.game.quest.domain.QuestListStatus;

public record QuestView(
		String code,
		String name,
		String description,
		String category,
		String status,
		int recommendedLevel,
		String startNpcCode,
		String startNpcName,
		String turnInNpcCode,
		String turnInNpcName,
		String nextQuestCode,
		String nextQuestName,
		boolean repeatable,
		boolean tracked,
		List<QuestObjectiveView> objectives,
		List<QuestRewardView> rewards,
		List<String> unlocks,
		String kitFamily,
		String lastSearchOutcome,
		String completeText
) {

	public static QuestView of(
			String code,
			String name,
			String description,
			QuestCategory category,
			QuestListStatus status,
			int recommendedLevel,
			String startNpcCode,
			String startNpcName,
			String turnInNpcCode,
			String turnInNpcName,
			String nextQuestCode,
			String nextQuestName,
			boolean repeatable,
			boolean tracked,
			List<QuestObjectiveView> objectives,
			List<QuestRewardView> rewards,
			List<String> unlocks) {
		return of(
				code,
				name,
				description,
				category,
				status,
				recommendedLevel,
				startNpcCode,
				startNpcName,
				turnInNpcCode,
				turnInNpcName,
				nextQuestCode,
				nextQuestName,
				repeatable,
				tracked,
				objectives,
				rewards,
				unlocks,
				null,
				null,
				null);
	}

	public static QuestView of(
			String code,
			String name,
			String description,
			QuestCategory category,
			QuestListStatus status,
			int recommendedLevel,
			String startNpcCode,
			String startNpcName,
			String turnInNpcCode,
			String turnInNpcName,
			String nextQuestCode,
			String nextQuestName,
			boolean repeatable,
			boolean tracked,
			List<QuestObjectiveView> objectives,
			List<QuestRewardView> rewards,
			List<String> unlocks,
			String kitFamily,
			String lastSearchOutcome,
			String completeText) {
		return new QuestView(
				code,
				name,
				description,
				category.name(),
				status.name(),
				recommendedLevel,
				startNpcCode,
				startNpcName,
				turnInNpcCode,
				turnInNpcName,
				nextQuestCode,
				nextQuestName,
				repeatable,
				tracked,
				objectives,
				rewards,
				unlocks,
				kitFamily,
				lastSearchOutcome,
				completeText);
	}
}
