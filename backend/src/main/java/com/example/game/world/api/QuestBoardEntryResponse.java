package com.example.game.world.api;

import java.util.List;

import com.example.game.quest.api.QuestRewardResponse;

public record QuestBoardEntryResponse(
		String code,
		String name,
		String shortDescription,
		String questType,
		String listState,
		int recommendedLevel,
		String difficulty,
		List<QuestRewardResponse> rewards
) {
}
