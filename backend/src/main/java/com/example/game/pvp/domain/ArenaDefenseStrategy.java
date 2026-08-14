package com.example.game.pvp.domain;

import java.util.List;
import java.util.Set;

import com.example.game.combat.domain.CombatAction;
import com.example.game.combat.domain.CombatV2Balance;
import com.example.game.combat.domain.ActionCombatBalance;
import com.example.game.mastery.domain.TechniqueEffectSpec;

/**
 * Structured Arena defense rules. Not a scripting language.
 */
public record ArenaDefenseStrategy(
		CombatAction preferredAction,
		String preferredTechniqueCode,
		int healWhenHpPercentBelow,
		int defendWhenStaminaPercentBelow,
		int finisherWhenEnemyHpPercentBelow,
		String finisherTechniqueCode
) {

	private static final Set<CombatAction> PREFERRED_ACTIONS = Set.of(
			CombatAction.QUICK_ATTACK,
			CombatAction.HEAVY_ATTACK,
			CombatAction.PRECISE_ATTACK,
			CombatAction.USE_TECHNIQUE);

	public ArenaDefenseStrategy {
		if (preferredAction == null || !PREFERRED_ACTIONS.contains(preferredAction)) {
			throw new IllegalArgumentException("preferredAction must be an attack or technique");
		}
		if (preferredAction == CombatAction.USE_TECHNIQUE
				&& (preferredTechniqueCode == null || preferredTechniqueCode.isBlank())) {
			throw new IllegalArgumentException("preferredTechniqueCode is required");
		}
		healWhenHpPercentBelow = clampPercent(healWhenHpPercentBelow);
		defendWhenStaminaPercentBelow = clampPercent(defendWhenStaminaPercentBelow);
		finisherWhenEnemyHpPercentBelow = clampPercent(finisherWhenEnemyHpPercentBelow);
		if (preferredTechniqueCode != null && preferredTechniqueCode.isBlank()) {
			preferredTechniqueCode = null;
		}
		if (finisherTechniqueCode != null && finisherTechniqueCode.isBlank()) {
			finisherTechniqueCode = null;
		}
	}

	public static ArenaDefenseStrategy defaults() {
		return new ArenaDefenseStrategy(
				CombatAction.QUICK_ATTACK,
				null,
				PvPBalance.HEAL_HP_DEFAULT,
				PvPBalance.DEFEND_STAMINA_DEFAULT,
				PvPBalance.FINISHER_HP_DEFAULT,
				null);
	}

	public ChosenPvpAction choose(
			ArenaDefenseView view,
			int staminaCostReduction,
			java.util.Map<String, TechniqueEffectSpec> techniqueSpecs) {
		if (view.ownHealthPercent() < healWhenHpPercentBelow && view.potionCharges() > 0) {
			return new ChosenPvpAction(CombatAction.USE_POTION, null);
		}
		if (view.ownStaminaPercent() < defendWhenStaminaPercentBelow) {
			return new ChosenPvpAction(CombatAction.DEFEND, null);
		}
		if (view.opponentHealthPercent() < finisherWhenEnemyHpPercentBelow
				&& finisherTechniqueCode != null
				&& view.availableTechniqueCodes().contains(finisherTechniqueCode)
				&& canAffordTechnique(view, finisherTechniqueCode, staminaCostReduction, techniqueSpecs)) {
			return new ChosenPvpAction(CombatAction.USE_TECHNIQUE, finisherTechniqueCode);
		}
		ChosenPvpAction preferred = preferredChoice(view);
		if (canAfford(view, preferred, staminaCostReduction, techniqueSpecs)) {
			return preferred;
		}
		if (canAffordCore(view, CombatAction.QUICK_ATTACK, staminaCostReduction)) {
			return new ChosenPvpAction(CombatAction.QUICK_ATTACK, null);
		}
		return new ChosenPvpAction(CombatAction.DEFEND, null);
	}

	private ChosenPvpAction preferredChoice(ArenaDefenseView view) {
		if (preferredAction == CombatAction.USE_TECHNIQUE
				&& preferredTechniqueCode != null
				&& view.availableTechniqueCodes().contains(preferredTechniqueCode)) {
			return new ChosenPvpAction(CombatAction.USE_TECHNIQUE, preferredTechniqueCode);
		}
		if (preferredAction == CombatAction.USE_TECHNIQUE) {
			return new ChosenPvpAction(CombatAction.QUICK_ATTACK, null);
		}
		return new ChosenPvpAction(preferredAction, null);
	}

	private static boolean canAfford(
			ArenaDefenseView view,
			ChosenPvpAction choice,
			int staminaCostReduction,
			java.util.Map<String, TechniqueEffectSpec> techniqueSpecs) {
		if (choice.action() == CombatAction.USE_TECHNIQUE) {
			return canAffordTechnique(view, choice.techniqueCode(), staminaCostReduction, techniqueSpecs);
		}
		return canAffordCore(view, choice.action(), staminaCostReduction);
	}

	private static boolean canAffordTechnique(
			ArenaDefenseView view,
			String code,
			int staminaCostReduction,
			java.util.Map<String, TechniqueEffectSpec> techniqueSpecs) {
		TechniqueEffectSpec spec = techniqueSpecs.get(code);
		if (spec == null) {
			return false;
		}
		return view.ownStamina() >= CombatV2Balance.reducedStaminaCost(spec.staminaCost(), staminaCostReduction);
	}

	private static boolean canAffordCore(ArenaDefenseView view, CombatAction action, int staminaCostReduction) {
		return view.ownStamina() >= CombatV2Balance.reducedStaminaCost(
				ActionCombatBalance.staminaCost(action), staminaCostReduction);
	}

	private static int clampPercent(int value) {
		return Math.max(0, Math.min(100, value));
	}

	public void validateAgainstLoadout(List<String> techniqueCodes) {
		if (preferredAction == CombatAction.USE_TECHNIQUE
				&& (preferredTechniqueCode == null || !techniqueCodes.contains(preferredTechniqueCode))) {
			throw new IllegalArgumentException("preferred technique is not in the loadout");
		}
		if (finisherTechniqueCode != null && !techniqueCodes.contains(finisherTechniqueCode)) {
			throw new IllegalArgumentException("finisher technique is not in the loadout");
		}
	}
}
