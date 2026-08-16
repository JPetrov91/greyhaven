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
						LocationAction.VIEW_NEARBY,
						LocationAction.TALK_NPCS);
	}

	@Test
	void tavernIncludesGlobalChatAndExpeditions() {
		assertThat(LocationActions.forCode(LocationCodes.TAVERN))
				.contains(
						LocationAction.VIEW_CHAT,
						LocationAction.START_EXPEDITION,
						LocationAction.INSPECT_EXPEDITIONS);
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
	void marketHasBrowseCreateBuyAndCancel() {
		assertThat(LocationActions.forCode(LocationCodes.MARKET))
				.containsExactly(
						LocationAction.INSPECT,
						LocationAction.MOVE,
						LocationAction.VIEW_NEARBY,
						LocationAction.BROWSE_MARKET,
						LocationAction.CREATE_LISTING,
						LocationAction.BUY_ITEM,
						LocationAction.CANCEL_LISTING,
						LocationAction.CREATE_BUY_ORDER,
						LocationAction.FULFILL_BUY_ORDER,
						LocationAction.TALK_NPCS);
	}

	@Test
	void phaseTwoLocationsKeepSafeHubsAndDungeonEntrance() {
		assertThat(LocationActions.forCode(LocationCodes.ARENA))
				.containsExactly(
						LocationAction.INSPECT,
						LocationAction.MOVE,
						LocationAction.VIEW_NEARBY,
						LocationAction.ENTER_ARENA,
						LocationAction.TALK_NPCS)
				.doesNotContain(LocationAction.SEARCH_ENCOUNTER, LocationAction.CHALLENGE_DUEL);
		assertThat(LocationActions.forCode(LocationCodes.SPARRING_YARD))
				.containsExactly(
						LocationAction.INSPECT,
						LocationAction.MOVE,
						LocationAction.VIEW_NEARBY,
						LocationAction.CHALLENGE_DUEL,
						LocationAction.START_SPARRING_DRILL);
		assertThat(LocationActions.forCode(LocationCodes.CRAFTSMEN_WARD))
				.contains(LocationAction.CRAFT, LocationAction.CLAIM_CRAFT, LocationAction.SALVAGE);
		assertThat(LocationActions.forCode(LocationCodes.ANCIENT_RUINS))
				.contains(LocationAction.SEARCH_ENCOUNTER, LocationAction.ENTER_DUNGEON);
	}

	@Test
	void unknownCodeIsRejected() {
		assertThatThrownBy(() -> LocationActions.forCode("MOON_BASE"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("MOON_BASE");
	}
}
