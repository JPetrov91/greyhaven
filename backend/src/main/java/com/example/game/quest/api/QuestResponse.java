package com.example.game.quest.api;

import java.util.List;

public record QuestResponse(
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
		List<QuestObjectiveResponse> objectives,
		List<QuestRewardResponse> rewards,
		List<String> unlocks
) {
}
