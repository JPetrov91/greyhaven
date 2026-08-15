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
		String turnInNpcCode,
		String nextQuestCode,
		boolean tracked,
		List<QuestObjectiveView> objectives,
		List<QuestRewardView> rewards,
		List<String> unlocks
) {

	public static QuestView of(
			String code,
			String name,
			String description,
			QuestCategory category,
			QuestListStatus status,
			int recommendedLevel,
			String startNpcCode,
			String turnInNpcCode,
			String nextQuestCode,
			boolean tracked,
			List<QuestObjectiveView> objectives,
			List<QuestRewardView> rewards,
			List<String> unlocks) {
		return new QuestView(
				code,
				name,
				description,
				category.name(),
				status.name(),
				recommendedLevel,
				startNpcCode,
				turnInNpcCode,
				nextQuestCode,
				tracked,
				objectives,
				rewards,
				unlocks);
	}
}
