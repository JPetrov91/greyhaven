package com.example.game.quest.domain;

public final class QuestObjectiveHints {

	private QuestObjectiveHints() {
	}

	public static QuestActionHint hintOf(QuestObjectiveType type) {
		return switch (type) {
			case TALK_TO_NPC -> QuestActionHint.TALK;
			case SEARCH_LOCATION -> QuestActionHint.SEARCH;
			case KILL, DEFEAT_ENEMY -> QuestActionHint.FIGHT;
			case VISIT_LOCATION, COLLECT, ACQUIRE_ITEM, CRAFT_ITEM, COMPLETE_EXPEDITION, COMPLETE_DUNGEON, WIN_ARENA_MATCH ->
					QuestActionHint.TRAVEL;
		};
	}

	public static String locationCodeOf(
			QuestObjectiveType type,
			String targetCode,
			String objectiveLocationCode,
			String npcLocationCode) {
		return switch (type) {
			case VISIT_LOCATION, SEARCH_LOCATION -> targetCode;
			case TALK_TO_NPC -> npcLocationCode;
			default -> objectiveLocationCode;
		};
	}
}
