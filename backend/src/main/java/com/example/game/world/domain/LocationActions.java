package com.example.game.world.domain;

import java.util.List;
import java.util.Map;

/**
 * Pure catalog of MVP location actions. Keeps action rules out of persistence entities.
 */
public final class LocationActions {

	private static final List<LocationAction> CITY_SQUARE_ACTIONS = List.of(
			LocationAction.INSPECT,
			LocationAction.MOVE,
			LocationAction.VIEW_NEARBY,
			LocationAction.TALK_NPCS,
			LocationAction.NOTICE_BOARD);

	private static final List<LocationAction> TAVERN_ACTIONS = List.of(
			LocationAction.INSPECT,
			LocationAction.MOVE,
			LocationAction.VIEW_NEARBY,
			LocationAction.VIEW_CHAT,
			LocationAction.START_EXPEDITION,
			LocationAction.INSPECT_EXPEDITIONS,
			LocationAction.TALK_NPCS);

	private static final List<LocationAction> MARKET_ACTIONS = List.of(
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

	private static final List<LocationAction> OLD_TOWN_ACTIONS = List.of(
			LocationAction.INSPECT,
			LocationAction.MOVE,
			LocationAction.VIEW_NEARBY,
			LocationAction.SEARCH_ENCOUNTER);

	private static final List<LocationAction> FOREST_ACTIONS = List.of(
			LocationAction.INSPECT,
			LocationAction.MOVE,
			LocationAction.VIEW_NEARBY,
			LocationAction.SEARCH_ENCOUNTER,
			LocationAction.START_EXPEDITION);

	private static final List<LocationAction> NORTH_ROAD_ACTIONS = List.of(
			LocationAction.INSPECT,
			LocationAction.MOVE,
			LocationAction.VIEW_NEARBY,
			LocationAction.SEARCH_ENCOUNTER);

	private static final List<LocationAction> ARENA_ACTIONS = List.of(
			LocationAction.INSPECT,
			LocationAction.MOVE,
			LocationAction.VIEW_NEARBY,
			LocationAction.ENTER_ARENA,
			LocationAction.TALK_NPCS);

	private static final List<LocationAction> SPARRING_YARD_ACTIONS = List.of(
			LocationAction.INSPECT,
			LocationAction.MOVE,
			LocationAction.VIEW_NEARBY,
			LocationAction.CHALLENGE_DUEL,
			LocationAction.START_SPARRING_DRILL);

	private static final List<LocationAction> CRAFTSMEN_WARD_ACTIONS = List.of(
			LocationAction.INSPECT,
			LocationAction.MOVE,
			LocationAction.VIEW_NEARBY,
			LocationAction.CRAFT,
			LocationAction.CLAIM_CRAFT,
			LocationAction.SALVAGE);

	private static final List<LocationAction> HARBOUR_ACTIONS = List.of(
			LocationAction.INSPECT,
			LocationAction.MOVE,
			LocationAction.VIEW_NEARBY,
			LocationAction.SEARCH_ENCOUNTER);

	private static final List<LocationAction> SEWERS_ACTIONS = HARBOUR_ACTIONS;
	private static final List<LocationAction> OLD_MINE_ACTIONS = HARBOUR_ACTIONS;
	private static final List<LocationAction> BANDIT_CAMP_ACTIONS = HARBOUR_ACTIONS;

	private static final List<LocationAction> ANCIENT_RUINS_ACTIONS = List.of(
			LocationAction.INSPECT,
			LocationAction.MOVE,
			LocationAction.VIEW_NEARBY,
			LocationAction.SEARCH_ENCOUNTER,
			LocationAction.ENTER_DUNGEON);

	private static final Map<String, List<LocationAction>> BY_CODE = Map.ofEntries(
			Map.entry(LocationCodes.CITY_SQUARE, CITY_SQUARE_ACTIONS),
			Map.entry(LocationCodes.TAVERN, TAVERN_ACTIONS),
			Map.entry(LocationCodes.MARKET, MARKET_ACTIONS),
			Map.entry(LocationCodes.OLD_TOWN, OLD_TOWN_ACTIONS),
			Map.entry(LocationCodes.FOREST, FOREST_ACTIONS),
			Map.entry(LocationCodes.NORTH_ROAD, NORTH_ROAD_ACTIONS),
			Map.entry(LocationCodes.ARENA, ARENA_ACTIONS),
			Map.entry(LocationCodes.SPARRING_YARD, SPARRING_YARD_ACTIONS),
			Map.entry(LocationCodes.CRAFTSMEN_WARD, CRAFTSMEN_WARD_ACTIONS),
			Map.entry(LocationCodes.HARBOUR, HARBOUR_ACTIONS),
			Map.entry(LocationCodes.SEWERS, SEWERS_ACTIONS),
			Map.entry(LocationCodes.OLD_MINE, OLD_MINE_ACTIONS),
			Map.entry(LocationCodes.BANDIT_CAMP, BANDIT_CAMP_ACTIONS),
			Map.entry(LocationCodes.ANCIENT_RUINS, ANCIENT_RUINS_ACTIONS));

	private LocationActions() {
	}

	public static List<LocationAction> forCode(String code) {
		List<LocationAction> actions = BY_CODE.get(code);
		if (actions == null) {
			throw new IllegalArgumentException("Unknown location code: " + code);
		}
		return actions;
	}
}
