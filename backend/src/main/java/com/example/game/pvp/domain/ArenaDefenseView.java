package com.example.game.pvp.domain;

import java.util.List;

import com.example.game.combat.domain.StatusInstance;

public record ArenaDefenseView(
		int ownHealth,
		int ownMaxHealth,
		int ownStamina,
		int ownMaxStamina,
		int opponentHealth,
		int opponentMaxHealth,
		int potionCharges,
		List<StatusInstance> ownStatuses,
		List<String> availableTechniqueCodes
) {

	public int ownHealthPercent() {
		return percent(ownHealth, ownMaxHealth);
	}

	public int ownStaminaPercent() {
		return percent(ownStamina, ownMaxStamina);
	}

	public int opponentHealthPercent() {
		return percent(opponentHealth, opponentMaxHealth);
	}

	private static int percent(int current, int max) {
		if (max <= 0) {
			return 0;
		}
		return (int) Math.round(current * 100.0 / max);
	}
}
