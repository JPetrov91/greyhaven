package com.example.game.world.domain;

/**
 * Declared actions available at a location. Chat is not yet wired to gameplay endpoints
 * and is returned for UI display only.
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
