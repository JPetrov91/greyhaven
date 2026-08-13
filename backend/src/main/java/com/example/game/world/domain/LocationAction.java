package com.example.game.world.domain;

/**
 * Declared actions available at a location. Some actions are not yet wired to gameplay
 * endpoints (combat, market, expeditions, chat) and are returned for UI display only.
 */
public enum LocationAction {
	INSPECT,
	MOVE,
	VIEW_NEARBY,
	VIEW_CHAT,
	START_EXPEDITION,
	INSPECT_EXPEDITIONS,
	BROWSE_MARKET,
	CREATE_LISTING,
	BUY_ITEM,
	CANCEL_LISTING,
	SEARCH_ENCOUNTER
}
