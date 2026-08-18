package com.example.game.world.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.game.quest.application.QuestView;
import com.example.game.quest.domain.QuestCategory;
import com.example.game.quest.domain.QuestListStatus;

class NpcQuestBadgeTest {

	@Test
	void prefersTurnInOverActiveAndAvailable() {
		assertThat(NpcApplicationService.questBadge("BREN", List.of(
				quest(QuestListStatus.AVAILABLE),
				quest(QuestListStatus.ACTIVE),
				quest(QuestListStatus.READY_TO_TURN_IN))))
				.containsExactly("TURN_IN");
	}

	@Test
	void prefersActiveOverAvailable() {
		assertThat(NpcApplicationService.questBadge("BREN", List.of(
				quest(QuestListStatus.AVAILABLE),
				quest(QuestListStatus.ACTIVE))))
				.containsExactly("ACTIVE");
	}

	@Test
	void availableWhenNoActiveQuest() {
		assertThat(NpcApplicationService.questBadge("BREN", List.of(quest(QuestListStatus.AVAILABLE))))
				.containsExactly("AVAILABLE_QUEST");
	}

	@Test
	void emptyWhenNpcIsUnrelated() {
		assertThat(NpcApplicationService.questBadge("OTHER", List.of(quest(QuestListStatus.ACTIVE)))).isEmpty();
	}

	private static QuestView quest(QuestListStatus status) {
		return QuestView.of(
				"QST_TEST",
				"Test",
				"d",
				QuestCategory.MAIN,
				status,
				1,
				"BREN",
				"Bren",
				"BREN",
				"Bren",
				null,
				null,
				false,
				true,
				List.of(),
				List.of(),
				List.of());
	}
}
