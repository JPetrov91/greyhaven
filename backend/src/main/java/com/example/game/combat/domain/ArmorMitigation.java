package com.example.game.combat.domain;

/**
 * Diminishing-return physical armor. Avoids linear subtraction that can zero out damage.
 */
public final class ArmorMitigation {

	private ArmorMitigation() {
	}

	public static int apply(int rawDamage, int armor, int armorBreakStacks, boolean guarded) {
		double breakFactor = Math.min(0.95, CombatV2Balance.armorBreakPerStack() * Math.max(0, armorBreakStacks));
		double effectiveArmor = Math.max(0, armor * (1.0 - breakFactor));
		double mitigation = effectiveArmor / (effectiveArmor + CombatV2Balance.armorK());
		int afterArmor = Math.max(
				CombatV2Balance.minDamageAfterArmor(),
				(int) Math.round(rawDamage * (1.0 - mitigation)));
		if (!guarded) {
			return afterArmor;
		}
		return Math.max(0, (int) Math.round(afterArmor * CombatV2Balance.guardedDamageTakenMult()));
	}
}
