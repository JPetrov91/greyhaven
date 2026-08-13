package com.example.game.combat.domain;

public enum CombatSessionStatus {
	ACTIVE,
	PLAYER_WON,
	PLAYER_LOST,
	PLAYER_ESCAPED;

	public boolean terminal() {
		return this != ACTIVE;
	}
}
