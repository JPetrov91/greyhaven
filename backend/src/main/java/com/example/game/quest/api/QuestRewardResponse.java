package com.example.game.quest.api;

public record QuestRewardResponse(
		String kind,
		int amount,
		String itemCode,
		String itemName,
		String unlockCode
) {
}
