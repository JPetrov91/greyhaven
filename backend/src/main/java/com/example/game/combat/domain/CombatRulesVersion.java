package com.example.game.combat.domain;

/**
 * Persisted on {@code combat_sessions.rules_version}. In-flight Phase 1 fights finish on legacy
 * rules; new fights use Combat 2.0.
 */
public final class CombatRulesVersion {

	public static final int PHASE_1 = 1;
	public static final int COMBAT_2 = 2;

	private CombatRulesVersion() {
	}
}
