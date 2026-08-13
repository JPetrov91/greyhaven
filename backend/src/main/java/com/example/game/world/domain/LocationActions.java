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
			LocationAction.VIEW_NEARBY);

	private static final List<LocationAction> TAVERN_ACTIONS = List.of(
			LocationAction.INSPECT,
			LocationAction.MOVE,
			LocationAction.VIEW_NEARBY,
			LocationAction.VIEW_CHAT,
			LocationAction.START_EXPEDITION,
			LocationAction.INSPECT_EXPEDITIONS);

	private static final List<LocationAction> MARKET_ACTIONS = List.of(
			LocationAction.INSPECT,
			LocationAction.MOVE,
			LocationAction.VIEW_NEARBY,
			LocationAction.BROWSE_MARKET,
			LocationAction.CREATE_LISTING,
			LocationAction.BUY_ITEM,
			LocationAction.CANCEL_LISTING);

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

	private static final Map<String, List<LocationAction>> BY_CODE = Map.of(
			LocationCodes.CITY_SQUARE, CITY_SQUARE_ACTIONS,
			LocationCodes.TAVERN, TAVERN_ACTIONS,
			LocationCodes.MARKET, MARKET_ACTIONS,
			LocationCodes.OLD_TOWN, OLD_TOWN_ACTIONS,
			LocationCodes.FOREST, FOREST_ACTIONS,
			LocationCodes.NORTH_ROAD, NORTH_ROAD_ACTIONS);

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
