package com.example.game.pvp.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.game.combat.domain.ActionCombatBalance;
import com.example.game.combat.domain.CombatAction;
import com.example.game.combat.domain.CombatEvent;
import com.example.game.combat.domain.CombatEventType;
import com.example.game.combat.domain.CombatRuleViolation;
import com.example.game.combat.domain.CombatStrikeResolver;
import com.example.game.combat.domain.CombatV2Balance;
import com.example.game.combat.domain.CombatantSide;
import com.example.game.combat.domain.CombatantStats;
import com.example.game.combat.domain.StatusEffectEngine;
import com.example.game.combat.domain.StatusInstance;
import com.example.game.combat.domain.StatusType;
import com.example.game.mastery.domain.TechniqueEffectSpec;
import com.example.game.shared.domain.RandomProvider;

/**
 * Two player-shaped combatants. Round order matches Combat 2.0: DoTs, attacker, defender, expire, regen.
 */
public final class PvpCombatEngine {

	private PvpCombatEngine() {
	}

	public static ChosenPvpAction previewDefenderIntent(PvpCombatState state) {
		return state.defense().choose(defenderView(state), state.defender().staminaCostReduction(), state.defender().techniqueSpecs());
	}

	public static int previewAttackerHitChance(PvpCombatState state, CombatAction action, String techniqueCode) {
		boolean technique = action == CombatAction.USE_TECHNIQUE;
		TechniqueEffectSpec spec = technique
				? state.attacker().techniqueSpecs().get(techniqueCode)
				: CombatStrikeResolver.coreSpec(action);
		if (spec == null) {
			return CombatV2Balance.minHitChance();
		}
		return CombatStrikeResolver.previewHitChance(
				state.attacker().stats(),
				state.defender().stats(),
				state.attackerStatuses(),
				state.defenderStatuses(),
				spec,
				state.attacker().masteryPassive(),
				technique);
	}

	public static PvpRoundResult resolve(
			PvpCombatState state,
			CombatAction attackerAction,
			String attackerTechnique,
			CombatAction defenderAction,
			String defenderTechnique,
			RandomProvider random) {
		if (state.status() != PvpMatchStatus.ACTIVE) {
			throw new CombatRuleViolation(CombatRuleViolation.Reason.COMBAT_NOT_ACTIVE, "match is not active");
		}
		Working working = Working.from(state);
		int round = state.roundNumber() + 1;
		List<CombatEvent> events = new ArrayList<>();

		tickOpeningDots(working, events);
		if (working.attackerHealth <= 0) {
			events.add(new CombatEvent(CombatEventType.COMBAT_LOST, "You were defeated by " + state.defender().name() + "."));
			return working.toResult(round, PvpMatchStatus.DEFENDER_WON, events);
		}
		if (working.defenderHealth <= 0) {
			events.add(new CombatEvent(CombatEventType.COMBAT_WON, "You defeated " + state.defender().name() + "!"));
			return working.toResult(round, PvpMatchStatus.ATTACKER_WON, events);
		}

		boolean attackerStunned = StatusEffectEngine.has(working.attackerStatuses, StatusType.STUN);
		if (attackerStunned) {
			StatusEffectEngine.StunConsumeResult skipped = StatusEffectEngine.consumeStun(
					working.attackerStatuses, CombatantSide.PLAYER);
			working.attackerStatuses = new ArrayList<>(skipped.statuses());
			events.addAll(skipped.events());
		}
		else {
			validate(
					state.attacker(),
					working.attackerStamina,
					working.attackerPotionCharges,
					attackerAction,
					attackerTechnique);
			resolveSide(
					state.attacker(),
					state.defender(),
					working,
					true,
					attackerAction,
					attackerTechnique,
					state.lastDefenderMissed() || state.lastAttackerGuarded(),
					random,
					events);
		}
		if (working.defenderHealth <= 0) {
			events.add(new CombatEvent(CombatEventType.COMBAT_WON, "You defeated " + state.defender().name() + "!"));
			return working.toResult(round, PvpMatchStatus.ATTACKER_WON, events);
		}

		boolean defenderStunned = StatusEffectEngine.has(working.defenderStatuses, StatusType.STUN);
		if (defenderStunned) {
			StatusEffectEngine.StunConsumeResult skipped = StatusEffectEngine.consumeStun(
					working.defenderStatuses, CombatantSide.ENEMY);
			working.defenderStatuses = new ArrayList<>(skipped.statuses());
			events.addAll(skipped.events());
		}
		else {
			validate(
					state.defender(),
					working.defenderStamina,
					working.defenderPotionCharges,
					defenderAction,
					defenderTechnique);
			resolveSide(
					state.defender(),
					state.attacker(),
					working,
					false,
					defenderAction,
					defenderTechnique,
					false,
					random,
					events);
		}
		if (working.attackerHealth <= 0) {
			events.add(new CombatEvent(CombatEventType.COMBAT_LOST, "You were defeated by " + state.defender().name() + "."));
			return working.toResult(round, PvpMatchStatus.DEFENDER_WON, events);
		}

		StatusEffectEngine.ExpireResult attackerExpire = StatusEffectEngine.expire(
				working.attackerStatuses, CombatantSide.PLAYER);
		working.attackerStatuses = new ArrayList<>(attackerExpire.statuses());
		events.addAll(attackerExpire.events());
		StatusEffectEngine.ExpireResult defenderExpire = StatusEffectEngine.expire(
				working.defenderStatuses, CombatantSide.ENEMY);
		working.defenderStatuses = new ArrayList<>(defenderExpire.statuses());
		events.addAll(defenderExpire.events());

		working.attackerStamina = Math.min(
				state.attacker().maxStamina(),
				working.attackerStamina + CombatV2Balance.playerStaminaRegen(state.attacker().stats().agility()));
		working.defenderStamina = Math.min(
				state.defender().maxStamina(),
				working.defenderStamina + CombatV2Balance.playerStaminaRegen(state.defender().stats().agility()));

		return working.toResult(round, PvpMatchStatus.ACTIVE, events);
	}

	private static ArenaDefenseView defenderView(PvpCombatState state) {
		return new ArenaDefenseView(
				state.defenderHealth(),
				state.defender().maxHealth(),
				state.defenderStamina(),
				state.defender().maxStamina(),
				state.attackerHealth(),
				state.attacker().maxHealth(),
				state.defenderPotionCharges(),
				state.defenderStatuses(),
				state.defender().techniqueCodes());
	}

	private static void tickOpeningDots(Working working, List<CombatEvent> events) {
		StatusEffectEngine.DotTickResult defenderDots = StatusEffectEngine.tickDots(
				working.defenderStatuses, CombatantSide.ENEMY);
		StatusEffectEngine.DotTickResult attackerDots = StatusEffectEngine.tickDots(
				working.attackerStatuses, CombatantSide.PLAYER);
		events.addAll(defenderDots.events());
		events.addAll(attackerDots.events());
		working.defenderHealth = Math.max(0, working.defenderHealth - defenderDots.damage());
		working.attackerHealth = Math.max(0, working.attackerHealth - attackerDots.damage());
	}

	private static void validate(
			PvpCombatantSnapshot actor,
			int stamina,
			int potionCharges,
			CombatAction action,
			String techniqueCode) {
		if (action == CombatAction.RETREAT) {
			throw new CombatRuleViolation(CombatRuleViolation.Reason.INVALID_TECHNIQUE, "retreat is not allowed");
		}
		if (action == CombatAction.USE_TECHNIQUE) {
			if (techniqueCode == null || techniqueCode.isBlank() || !actor.techniqueCodes().contains(techniqueCode)) {
				throw new CombatRuleViolation(CombatRuleViolation.Reason.INVALID_TECHNIQUE, "technique is not in the snapshot loadout");
			}
			TechniqueEffectSpec spec = actor.techniqueSpecs().get(techniqueCode);
			if (spec == null) {
				throw new CombatRuleViolation(CombatRuleViolation.Reason.INVALID_TECHNIQUE, "unknown technique");
			}
			int cost = CombatV2Balance.reducedStaminaCost(spec.staminaCost(), actor.staminaCostReduction());
			if (stamina < cost) {
				throw new CombatRuleViolation(CombatRuleViolation.Reason.INSUFFICIENT_STAMINA, "insufficient stamina");
			}
			return;
		}
		int cost = CombatV2Balance.reducedStaminaCost(
				ActionCombatBalance.staminaCost(action), actor.staminaCostReduction());
		if (stamina < cost) {
			throw new CombatRuleViolation(CombatRuleViolation.Reason.INSUFFICIENT_STAMINA, "insufficient stamina");
		}
		if (action == CombatAction.USE_POTION && (potionCharges < 1 || actor.potionHealAmount() < 1)) {
			throw new CombatRuleViolation(CombatRuleViolation.Reason.NO_POTION, "no potion available");
		}
	}

	private static void resolveSide(
			PvpCombatantSnapshot actor,
			PvpCombatantSnapshot opponent,
			Working working,
			boolean attackerActing,
			CombatAction action,
			String techniqueCode,
			boolean counterWindow,
			RandomProvider random,
			List<CombatEvent> events) {
		boolean playerPerspective = attackerActing;
		switch (action) {
			case QUICK_ATTACK, HEAVY_ATTACK, PRECISE_ATTACK -> {
				subtractStamina(working, attackerActing, CombatV2Balance.reducedStaminaCost(
						ActionCombatBalance.staminaCost(action), actor.staminaCostReduction()));
				strike(actor, opponent, working, attackerActing, CombatStrikeResolver.coreSpec(action), false, counterWindow, random, events);
			}
			case USE_TECHNIQUE -> {
				TechniqueEffectSpec spec = actor.techniqueSpecs().get(techniqueCode);
				subtractStamina(working, attackerActing, CombatV2Balance.reducedStaminaCost(
						spec.staminaCost(), actor.staminaCostReduction()));
				events.add(new CombatEvent(
						playerPerspective ? CombatEventType.PLAYER_TECHNIQUE : CombatEventType.ENEMY_TECHNIQUE,
						(playerPerspective ? "You use " : actor.name() + " uses ")
								+ techniqueCode.replace('_', ' ').toLowerCase(Locale.ROOT) + "."));
				strike(actor, opponent, working, attackerActing, spec, true, counterWindow, random, events);
			}
			case DEFEND -> {
				if (attackerActing) {
					working.attackerStamina = Math.min(
							actor.maxStamina(),
							working.attackerStamina + CombatV2Balance.defendStaminaRestore());
					working.lastAttackerGuarded = true;
					events.add(new CombatEvent(CombatEventType.PLAYER_DEFEND, "You take a defensive stance and catch your breath."));
					StatusEffectEngine.StatusApplyResult guarded = StatusEffectEngine.apply(
							working.attackerStatuses, StatusType.GUARDED, 1, 1, CombatantSide.PLAYER);
					working.attackerStatuses = new ArrayList<>(guarded.statuses());
					events.addAll(guarded.events());
				}
				else {
					working.defenderStamina = Math.min(
							actor.maxStamina(),
							working.defenderStamina + CombatV2Balance.defendStaminaRestore());
					events.add(new CombatEvent(CombatEventType.ENEMY_TECHNIQUE, actor.name() + " takes a defensive stance."));
					StatusEffectEngine.StatusApplyResult guarded = StatusEffectEngine.apply(
							working.defenderStatuses, StatusType.GUARDED, 1, 1, CombatantSide.ENEMY);
					working.defenderStatuses = new ArrayList<>(guarded.statuses());
					events.addAll(guarded.events());
				}
			}
			case USE_POTION -> {
				int heal = actor.potionHealAmount();
				if (attackerActing) {
					int recovered = Math.min(actor.maxHealth() - working.attackerHealth, heal);
					working.attackerHealth += recovered;
					working.attackerPotionCharges = Math.max(0, working.attackerPotionCharges - 1);
					events.add(new CombatEvent(
							CombatEventType.PLAYER_POTION,
							"You drink a healing potion and recover " + recovered + " health."));
				}
				else {
					int recovered = Math.min(actor.maxHealth() - working.defenderHealth, heal);
					working.defenderHealth += recovered;
					working.defenderPotionCharges = Math.max(0, working.defenderPotionCharges - 1);
					events.add(new CombatEvent(
							CombatEventType.ENEMY_TECHNIQUE,
							actor.name() + " drinks a healing potion and recovers " + recovered + " health."));
				}
			}
			case RETREAT -> throw new CombatRuleViolation(
					CombatRuleViolation.Reason.INVALID_TECHNIQUE, "retreat is not allowed");
		}
	}

	private static void strike(
			PvpCombatantSnapshot actor,
			PvpCombatantSnapshot opponent,
			Working working,
			boolean attackerActing,
			TechniqueEffectSpec spec,
			boolean technique,
			boolean counterWindow,
			RandomProvider random,
			List<CombatEvent> events) {
		CombatantStats attackerStats = actor.stats();
		CombatantStats defenderStats = opponent.stats();
		CombatStrikeResolver.Outcome outcome = CombatStrikeResolver.resolve(
				attackerStats,
				defenderStats,
				attackerActing ? working.attackerStatuses : working.defenderStatuses,
				attackerActing ? working.defenderStatuses : working.attackerStatuses,
				attackerActing ? working.defenderHealth : working.attackerHealth,
				opponent.maxHealth(),
				spec,
				actor.masteryPassive(),
				technique,
				counterWindow,
				actor.name(),
				opponent.name(),
				attackerActing,
				attackerActing ? CombatantSide.ENEMY : CombatantSide.PLAYER,
				random);
		if (attackerActing) {
			working.defenderHealth = outcome.defenderHealth();
			working.defenderStatuses = new ArrayList<>(outcome.defenderStatuses());
			if (!outcome.hit()) {
				working.lastDefenderMissed = false;
			}
		}
		else {
			working.attackerHealth = outcome.defenderHealth();
			working.attackerStatuses = new ArrayList<>(outcome.defenderStatuses());
			working.lastDefenderMissed = !outcome.hit();
		}
		events.addAll(outcome.events());
	}

	private static void subtractStamina(Working working, boolean attackerActing, int cost) {
		if (attackerActing) {
			working.attackerStamina -= cost;
		}
		else {
			working.defenderStamina -= cost;
		}
	}

	private static final class Working {
		int attackerHealth;
		int attackerStamina;
		int defenderHealth;
		int defenderStamina;
		int attackerPotionCharges;
		int defenderPotionCharges;
		List<StatusInstance> attackerStatuses;
		List<StatusInstance> defenderStatuses;
		boolean lastDefenderMissed;
		boolean lastAttackerGuarded;

		static Working from(PvpCombatState state) {
			Working working = new Working();
			working.attackerHealth = state.attackerHealth();
			working.attackerStamina = state.attackerStamina();
			working.defenderHealth = state.defenderHealth();
			working.defenderStamina = state.defenderStamina();
			working.attackerPotionCharges = state.attackerPotionCharges();
			working.defenderPotionCharges = state.defenderPotionCharges();
			working.attackerStatuses = new ArrayList<>(state.attackerStatuses());
			working.defenderStatuses = new ArrayList<>(state.defenderStatuses());
			working.lastDefenderMissed = false;
			working.lastAttackerGuarded = false;
			return working;
		}

		PvpRoundResult toResult(int round, PvpMatchStatus status, List<CombatEvent> events) {
			int attackerHealth = status == PvpMatchStatus.DEFENDER_WON ? 0 : this.attackerHealth;
			int defenderHealth = status == PvpMatchStatus.ATTACKER_WON ? 0 : this.defenderHealth;
			return new PvpRoundResult(
					round,
					status,
					attackerHealth,
					attackerStamina,
					defenderHealth,
					defenderStamina,
					attackerPotionCharges,
					defenderPotionCharges,
					List.copyOf(attackerStatuses),
					List.copyOf(defenderStatuses),
					lastDefenderMissed,
					lastAttackerGuarded,
					List.copyOf(events));
		}
	}
}
