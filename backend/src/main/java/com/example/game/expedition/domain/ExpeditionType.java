package com.example.game.expedition.domain;

/**
 * MVP expedition catalog codes.
 */
public enum ExpeditionType {
	FOREST_PATROL("Forest Patrol");

	private final String displayName;

	ExpeditionType(String displayName) {
		this.displayName = displayName;
	}

	public String displayName() {
		return displayName;
	}
}
