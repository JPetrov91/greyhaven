package com.example.game.quest.application;

import java.util.List;

public record QuestBoardEntryView(
		String code,
		String name,
		String shortDescription,
		String questType,
		String listState,
		int recommendedLevel,
		String difficulty,
		List<QuestRewardView> rewards
) {
}