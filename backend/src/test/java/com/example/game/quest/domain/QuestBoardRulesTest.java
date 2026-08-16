package com.example.game.quest.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

class QuestBoardRulesTest {

	@Test
	void hidesDisabledCompletedAndGatedQuests() {
		assertThat(QuestBoardRules.visibleOnBoard(false, false, null, null, Set.of())).isFalse();
		assertThat(QuestBoardRules.visibleOnBoard(true, false, QuestStatus.COMPLETED, null, Set.of())).isFalse();
		assertThat(QuestBoardRules.visibleOnBoard(true, true, QuestStatus.COMPLETED, null, Set.of())).isTrue();
		assertThat(QuestBoardRules.visibleOnBoard(true, false, null, "QST_A", Set.of())).isFalse();
		assertThat(QuestBoardRules.visibleOnBoard(true, false, null, "QST_A", Set.of("QST_A"))).isTrue();
		assertThat(QuestBoardRules.visibleOnBoard(true, false, QuestStatus.ACTIVE, null, Set.of())).isTrue();
	}

	@Test
	void marksLevelRestrictedQuestsUnavailable() {
		assertThat(QuestBoardRules.listState(1, 4, null)).isEqualTo(QuestBoardListState.UNAVAILABLE);
		assertThat(QuestBoardRules.listState(4, 4, null)).isEqualTo(QuestBoardListState.AVAILABLE);
		assertThat(QuestBoardRules.listState(1, 4, QuestStatus.ACTIVE)).isEqualTo(QuestBoardListState.ACTIVE);
		assertThat(QuestBoardRules.listState(1, 1, QuestStatus.READY_TO_TURN_IN))
				.isEqualTo(QuestBoardListState.READY_TO_TURN_IN);
	}
}
