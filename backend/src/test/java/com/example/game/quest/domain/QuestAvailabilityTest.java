package com.example.game.quest.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;

class QuestAvailabilityTest {

	@Test
	void requiresLevelPrerequisiteAndNotStarted() {
		assertThat(QuestAvailability.isAvailable(1, 1, null, Set.of(), false)).isTrue();
		assertThat(QuestAvailability.isAvailable(1, 2, null, Set.of(), false)).isFalse();
		assertThat(QuestAvailability.isAvailable(2, 1, "QST_A", Set.of(), false)).isFalse();
		assertThat(QuestAvailability.isAvailable(2, 1, "QST_A", Set.of("QST_A"), false)).isTrue();
		assertThat(QuestAvailability.isAvailable(2, 1, "QST_A", Set.of("QST_A"), true)).isFalse();
		assertThat(QuestAvailability.isAvailable(1, 1, null, Set.of("QST_REPEAT"), false)).isTrue();
	}
}
