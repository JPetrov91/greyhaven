package com.example.game.quest.application;

public record QuestPresentation(
		String shortDescription,
		String questType,
		String difficulty,
		String artworkKey,
		String boardLocationCode,
		String objectiveLocationCode,
		String locationName,
		String regionName,
		String actionHint,
		String actionTargetCode,
		String actionLocationCode
) {
}
