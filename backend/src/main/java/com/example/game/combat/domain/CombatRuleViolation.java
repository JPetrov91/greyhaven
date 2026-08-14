package com.example.game.combat.domain;

/**
 * Raised when a requested action breaks a combat rule. The reason is part of the contract so the
 * application layer can map failures onto API errors without inspecting exception text.
 */
public class CombatRuleViolation extends RuntimeException {

	public enum Reason {
		COMBAT_NOT_ACTIVE,
		INSUFFICIENT_STAMINA,
		NO_POTION,
		INVALID_TECHNIQUE
	}

	private final Reason reason;

	public CombatRuleViolation(Reason reason, String message) {
		super(message);
		this.reason = reason;
	}

	public Reason getReason() {
		return reason;
	}
}
