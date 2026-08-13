package com.example.game.world.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class LocationActionsTest {

	@Test
	void citySquareHasInspectMoveAndNearby() {
		assertThat(LocationActions.forCode(LocationCodes.CITY_SQUARE))
				.containsExactly(
						LocationAction.INSPECT,
						LocationAction.MOVE,
						LocationAction.VIEW_NEARBY);
	}

	@Test
	void forestIncludesEncounterAndExpeditionActions() {
		assertThat(LocationActions.forCode(LocationCodes.FOREST))
				.contains(
						LocationAction.SEARCH_ENCOUNTER,
						LocationAction.START_EXPEDITION,
						LocationAction.MOVE);
	}

	@Test
	void unknownCodeIsRejected() {
		assertThatThrownBy(() -> LocationActions.forCode("MOON_BASE"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("MOON_BASE");
	}
}
