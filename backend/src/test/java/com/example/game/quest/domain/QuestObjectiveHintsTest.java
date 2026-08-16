package com.example.game.quest.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class QuestObjectiveHintsTest {

	@Test
	void mapsObjectiveTypesToActionHints() {
		assertThat(QuestObjectiveHints.hintOf(QuestObjectiveType.TALK_TO_NPC)).isEqualTo(QuestActionHint.TALK);
		assertThat(QuestObjectiveHints.hintOf(QuestObjectiveType.SEARCH_LOCATION)).isEqualTo(QuestActionHint.SEARCH);
		assertThat(QuestObjectiveHints.hintOf(QuestObjectiveType.KILL)).isEqualTo(QuestActionHint.FIGHT);
		assertThat(QuestObjectiveHints.hintOf(QuestObjectiveType.VISIT_LOCATION)).isEqualTo(QuestActionHint.TRAVEL);
	}

	@Test
	void resolvesActionLocationFromObjectiveTarget() {
		assertThat(QuestObjectiveHints.locationCodeOf(
				QuestObjectiveType.VISIT_LOCATION, "NORTH_ROAD", "SEWERS", "CITY_SQUARE"))
				.isEqualTo("NORTH_ROAD");
		assertThat(QuestObjectiveHints.locationCodeOf(
				QuestObjectiveType.TALK_TO_NPC, "CAPTAIN_VARRO", "NORTH_ROAD", "CITY_SQUARE"))
				.isEqualTo("CITY_SQUARE");
		assertThat(QuestObjectiveHints.locationCodeOf(
				QuestObjectiveType.KILL, "BANDIT", "NORTH_ROAD", null))
				.isEqualTo("NORTH_ROAD");
	}
}
