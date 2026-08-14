package com.example.game.combat.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.example.game.mastery.domain.TechniqueEffectSpec;
import com.example.game.shared.domain.RandomProvider;

/**
 * Combat 2.0 resolution. Persistence stays in the application layer.
 *
 * <p>Round order: start-of-round DoTs (both sides, then defeat checks), player action, enemy
 * action, expire, stamina regen. Random roll order for attacks: hit, then crit, then (enemy only)
 * raw damage {@code nextInt}. Status application does not roll. Enemy AI is deterministic.
 */
public final class CombatEngine {

	private CombatEngine() {
	}

	/**
	 * Server-side hit-chance preview using the same accuracy/dodge clamp as a strike.
	 * Does not roll and does not mutate state.
	 */
	public static int previewPlayerHitChance(
			Combat2State state,
			CombatAction action,
			TechniqueEffectSpec techniqueSpec) {
		boolean technique = action == CombatAction.USE_TECHNIQUE;
		TechniqueEffectSpec spec = technique ? techniqueSpec : CombatStrikeResolver.coreSpec(action);
		return CombatStrikeResolver.previewHitChance(
				state.playerStats(),
				new CombatantStats(
						0,
						0,
						state.enemy().dodge(),
						0,
						state.enemy().armor(),
						0),
				state.playerStatuses(),
				state.enemyStatuses(),
				spec,
				state.masteryPassive(),
				technique);
	}

	/**
	 * Deterministic enemy action the AI would choose on the current snapshot.
	 */
	public static EnemyActionKind previewEnemyIntent(Combat2State state) {
		return EnemyAi.choose(new EnemyAiView(
				state.enemy().archetype(),
				state.enemyHealth(),
				state.enemyMaxHealth(),
				state.enemyStamina(),
				state.enemyMaxStamina(),
				state.enemyStatuses(),
				state.playerHealth(),
				state.playerMaxHealth(),
				state.playerStatuses(),
				state.enemy().signatureStatus(),
				state.enemy().tier()));
	}

	public static CombatRoundResult resolve(
			Combat2State state,
			CombatAction action,
			String techniqueCode,
			CombatActionContext actionContext,
			RandomProvider random) {
		if (state.status() != CombatSessionStatus.ACTIVE) {
			throw new CombatRuleViolation(
					CombatRuleViolation.Reason.COMBAT_NOT_ACTIVE,
					"combat is not active");
		}

		Working working = Working.from(state);
		int round = state.roundNumber() + 1;
		List<CombatEvent> events = new ArrayList<>();

		tickOpeningDots(working, events);
		if (working.playerHealth <= 0) {
			events.add(new CombatEvent(
					CombatEventType.COMBAT_LOST,
					"You were defeated by the " + state.enemy().name() + "."));
			return working.toResult(round, CombatSessionStatus.PLAYER_LOST, events);
		}
		if (working.enemyHealth <= 0) {
			events.add(new CombatEvent(
					CombatEventType.COMBAT_WON,
					"You defeated the " + state.enemy().name() + "!"));
			return working.toResult(round, CombatSessionStatus.PLAYER_WON, events);
		}

		boolean playerStunned = StatusEffectEngine.has(working.playerStatuses, StatusType.STUN);
		if (playerStunned) {
			StatusEffectEngine.StunConsumeResult skipped = StatusEffectEngine.consumeStun(
					working.playerStatuses, CombatantSide.PLAYER);
			working.playerStatuses = new ArrayList<>(skipped.statuses());
			events.addAll(skipped.events());
		}
		else {
			validateAction(state, working, action, techniqueCode, actionContext);
			resolvePlayerAction(state, working, action, techniqueCode, actionContext, random, events);
		}

		if (working.enemyHealth <= 0) {
			events.add(new CombatEvent(
					CombatEventType.COMBAT_WON,
					"You defeated the " + state.enemy().name() + "!"));
			return working.toResult(round, CombatSessionStatus.PLAYER_WON, events);
		}

		if (working.escaped) {
			events.add(new CombatEvent(CombatEventType.COMBAT_ESCAPED, "You escaped combat."));
			return working.toResult(round, CombatSessionStatus.PLAYER_ESCAPED, events);
		}

		boolean enemyStunned = StatusEffectEngine.has(working.enemyStatuses, StatusType.STUN);
		if (enemyStunned) {
			StatusEffectEngine.StunConsumeResult skipped = StatusEffectEngine.consumeStun(
					working.enemyStatuses, CombatantSide.ENEMY);
			working.enemyStatuses = new ArrayList<>(skipped.statuses());
			events.addAll(skipped.events());
		}
		else {
			resolveEnemyAction(state, working, random, events);
		}

		if (working.playerHealth <= 0) {
			events.add(new CombatEvent(
					CombatEventType.COMBAT_LOST,
					"You were defeated by the " + state.enemy().name() + "."));
			return working.toResult(round, CombatSessionStatus.PLAYER_LOST, events);
		}

		StatusEffectEngine.ExpireResult playerExpire = StatusEffectEngine.expire(
				working.playerStatuses, CombatantSide.PLAYER);
		working.playerStatuses = new ArrayList<>(playerExpire.statuses());
		events.addAll(playerExpire.events());
		StatusEffectEngine.ExpireResult enemyExpire = StatusEffectEngine.expire(
				working.enemyStatuses, CombatantSide.ENEMY);
		working.enemyStatuses = new ArrayList<>(enemyExpire.statuses());
		events.addAll(enemyExpire.events());

		working.playerStamina = Math.min(
				state.playerMaxStamina(),
				working.playerStamina + CombatV2Balance.playerStaminaRegen(state.playerStats().agility()));
		working.enemyStamina = Math.min(
				state.enemyMaxStamina(),
				working.enemyStamina + CombatV2Balance.enemyStaminaRegen());

		return working.toResult(round, CombatSessionStatus.ACTIVE, events);
	}

	private static void tickOpeningDots(Working working, List<CombatEvent> events) {
		StatusEffectEngine.DotTickResult enemyDots = StatusEffectEngine.tickDots(
				working.enemyStatuses, CombatantSide.ENEMY);
		StatusEffectEngine.DotTickResult playerDots = StatusEffectEngine.tickDots(
				working.playerStatuses, CombatantSide.PLAYER);
		events.addAll(enemyDots.events());
		events.addAll(playerDots.events());
		working.enemyHealth = Math.max(0, working.enemyHealth - enemyDots.damage());
		working.playerHealth = Math.max(0, working.playerHealth - playerDots.damage());
	}

	private static void validateAction(
			Combat2State state,
			Working working,
			CombatAction action,
			String techniqueCode,
			CombatActionContext actionContext) {
		if (action == CombatAction.USE_TECHNIQUE) {
			if (techniqueCode == null || techniqueCode.isBlank()) {
				throw new CombatRuleViolation(
						CombatRuleViolation.Reason.INVALID_TECHNIQUE,
						"technique code is required");
			}
			if (!state.availableTechniqueCodes().contains(techniqueCode)) {
				throw new CombatRuleViolation(
						CombatRuleViolation.Reason.INVALID_TECHNIQUE,
						"technique is not in the combat loadout");
			}
			TechniqueEffectSpec spec = state.techniqueSpecs().get(techniqueCode);
			if (spec == null) {
				throw new CombatRuleViolation(
						CombatRuleViolation.Reason.INVALID_TECHNIQUE,
						"unknown technique");
			}
			int cost = CombatV2Balance.reducedStaminaCost(spec.staminaCost(), state.staminaCostReduction());
			if (working.playerStamina < cost) {
				throw new CombatRuleViolation(
						CombatRuleViolation.Reason.INSUFFICIENT_STAMINA,
						"insufficient stamina for " + techniqueCode);
			}
			return;
		}
		int cost = CombatV2Balance.reducedStaminaCost(
				ActionCombatBalance.staminaCost(action), state.staminaCostReduction());
		if (working.playerStamina < cost) {
			throw new CombatRuleViolation(
					CombatRuleViolation.Reason.INSUFFICIENT_STAMINA,
					"insufficient stamina for " + action);
		}
		if (action == CombatAction.USE_POTION
				&& (!actionContext.potionAvailable() || actionContext.potionHealAmount() < 1)) {
			throw new CombatRuleViolation(
					CombatRuleViolation.Reason.NO_POTION,
					"no potion available");
		}
	}

	private static void resolvePlayerAction(
			Combat2State state,
			Working working,
			CombatAction action,
			String techniqueCode,
			CombatActionContext actionContext,
			RandomProvider random,
			List<CombatEvent> events) {
		switch (action) {
			case QUICK_ATTACK, HEAVY_ATTACK, PRECISE_ATTACK -> {
				working.playerStamina -= CombatV2Balance.reducedStaminaCost(
						ActionCombatBalance.staminaCost(action), state.staminaCostReduction());
				applyPlayerStrike(
						state,
						working,
						CombatStrikeResolver.coreSpec(action),
						false,
						random,
						events);
			}
			case USE_TECHNIQUE -> {
				TechniqueEffectSpec spec = state.techniqueSpecs().get(techniqueCode);
				working.playerStamina -= CombatV2Balance.reducedStaminaCost(
						spec.staminaCost(), state.staminaCostReduction());
				events.add(new CombatEvent(
						CombatEventType.PLAYER_TECHNIQUE,
						"You use " + techniqueCode.replace('_', ' ').toLowerCase(Locale.ROOT) + "."));
				applyPlayerStrike(state, working, spec, true, random, events);
			}
			case DEFEND -> {
				working.playerStamina = Math.min(
						state.playerMaxStamina(),
						working.playerStamina + CombatV2Balance.defendStaminaRestore());
				working.lastPlayerGuarded = true;
				events.add(new CombatEvent(
						CombatEventType.PLAYER_DEFEND,
						"You take a defensive stance and catch your breath."));
				StatusEffectEngine.StatusApplyResult guarded = StatusEffectEngine.apply(
						working.playerStatuses, StatusType.GUARDED, 1, 1, CombatantSide.PLAYER);
				working.playerStatuses = new ArrayList<>(guarded.statuses());
				events.addAll(guarded.events());
			}
			case USE_POTION -> {
				int healed = Math.min(
						state.playerMaxHealth() - working.playerHealth,
						actionContext.potionHealAmount());
				working.playerHealth += healed;
				events.add(new CombatEvent(
						CombatEventType.PLAYER_POTION,
						"You drink a healing potion and recover " + healed + " health."));
			}
			case RETREAT -> {
				int chance = ActionCombatBalance.retreatChance(state.playerStats().agility());
				if (random.chancePercent(chance)) {
					events.add(new CombatEvent(
							CombatEventType.PLAYER_RETREAT_SUCCESS,
							"You slip away from the " + state.enemy().name() + "."));
					working.escaped = true;
				}
				else {
					events.add(new CombatEvent(
							CombatEventType.PLAYER_RETREAT_FAIL,
							"You fail to escape the " + state.enemy().name() + "."));
				}
			}
		}
	}

	private static void applyPlayerStrike(
			Combat2State state,
			Working working,
			TechniqueEffectSpec spec,
			boolean technique,
			RandomProvider random,
			List<CombatEvent> events) {
		CombatStrikeResolver.Outcome outcome = CombatStrikeResolver.resolve(
				state.playerStats(),
				new CombatantStats(
						0,
						0,
						state.enemy().dodge(),
						0,
						state.enemy().armor(),
						0),
				working.playerStatuses,
				working.enemyStatuses,
				working.enemyHealth,
				state.enemyMaxHealth(),
				spec,
				state.masteryPassive(),
				technique,
				state.lastEnemyMissed() || state.lastPlayerGuarded(),
				"You",
				"the " + state.enemy().name(),
				true,
				CombatantSide.ENEMY,
				random);
		working.enemyHealth = outcome.defenderHealth();
		working.enemyStatuses = new ArrayList<>(outcome.defenderStatuses());
		events.addAll(outcome.events());
	}

	private static void resolveEnemyAction(
			Combat2State state,
			Working working,
			RandomProvider random,
			List<CombatEvent> events) {
		EnemyAiView view = new EnemyAiView(
				state.enemy().archetype(),
				working.enemyHealth,
				state.enemyMaxHealth(),
				working.enemyStamina,
				state.enemyMaxStamina(),
				List.copyOf(working.enemyStatuses),
				working.playerHealth,
				state.playerMaxHealth(),
				List.copyOf(working.playerStatuses),
				state.enemy().signatureStatus(),
				state.enemy().tier());
		EnemyActionKind chosen = EnemyAi.choose(view);
		switch (chosen) {
			case DEFEND -> {
				working.enemyStamina = Math.min(
						state.enemyMaxStamina(),
						working.enemyStamina + CombatV2Balance.defendStaminaRestore());
				events.add(new CombatEvent(
						CombatEventType.ENEMY_TECHNIQUE,
						"The " + state.enemy().name() + " takes a defensive stance."));
				StatusEffectEngine.StatusApplyResult guarded = StatusEffectEngine.apply(
						working.enemyStatuses, StatusType.GUARDED, 1, 1, CombatantSide.ENEMY);
				working.enemyStatuses = new ArrayList<>(guarded.statuses());
				events.addAll(guarded.events());
			}
			case BASIC_ATTACK, HEAVY_ATTACK, STATUS_ATTACK -> {
				int cost = switch (chosen) {
					case HEAVY_ATTACK -> CombatV2Balance.enemyHeavyStaminaCost();
					case STATUS_ATTACK -> CombatV2Balance.enemyStatusAttackStaminaCost();
					default -> CombatV2Balance.enemyBasicStaminaCost();
				};
				working.enemyStamina -= cost;
				double damageMult = chosen == EnemyActionKind.HEAVY_ATTACK ? 1.4 : 1.0;
				double accuracyMult = chosen == EnemyActionKind.HEAVY_ATTACK ? 0.8 : 1.0;
				int accuracy = (int) Math.round(state.enemy().accuracy() * accuracyMult);
				if (StatusEffectEngine.has(working.enemyStatuses, StatusType.OFF_BALANCE)) {
					accuracy -= CombatV2Balance.offBalanceAccuracyPenalty();
				}
				int dodge = state.playerStats().dodge();
				if (StatusEffectEngine.has(working.playerStatuses, StatusType.OFF_BALANCE)) {
					dodge -= CombatV2Balance.offBalanceDodgePenalty();
				}
				int hitChance = CombatV2Balance.clampHitChance(accuracy - dodge);
				if (!random.chancePercent(hitChance)) {
					working.lastEnemyMissed = true;
					events.add(new CombatEvent(
							CombatEventType.ENEMY_MISS,
							"The " + state.enemy().name() + " misses you."));
					return;
				}
				int critChance = CombatV2Balance.clampCritChance(state.enemy().criticalChance());
				boolean crit = random.chancePercent(critChance);
				int raw = (int) Math.round(random.nextInt(state.enemy().damageMin(), state.enemy().damageMax()) * damageMult);
				if (crit) {
					raw *= CombatV2Balance.criticalDamageMult();
				}
				boolean guarded = StatusEffectEngine.has(working.playerStatuses, StatusType.GUARDED);
				int damage = ArmorMitigation.apply(
						Math.max(1, raw),
						state.playerStats().armor(),
						StatusEffectEngine.stacks(working.playerStatuses, StatusType.ARMOR_BREAK),
						guarded);
				if (guarded) {
					working.playerStatuses = new ArrayList<>(
							StatusEffectEngine.consumeGuardedOnHit(working.playerStatuses));
				}
				working.playerHealth = Math.max(0, working.playerHealth - damage);
				events.add(new CombatEvent(
						CombatEventType.ENEMY_ATTACK,
						crit
								? "The " + state.enemy().name() + " lands a critical hit for " + damage + " damage."
								: "The " + state.enemy().name() + " hits you for " + damage + " damage."));
				if (chosen == EnemyActionKind.STATUS_ATTACK && state.enemy().signatureStatus() != null) {
					StatusType status = state.enemy().signatureStatus();
					int duration = status == StatusType.STUN ? 1 : 2;
					StatusEffectEngine.StatusApplyResult applied = StatusEffectEngine.apply(
							working.playerStatuses, status, 1, duration, CombatantSide.PLAYER);
					working.playerStatuses = new ArrayList<>(applied.statuses());
					events.addAll(applied.events());
				}
			}
		}
	}

	private static final class Working {
		int playerHealth;
		int playerStamina;
		int enemyHealth;
		int enemyStamina;
		List<StatusInstance> playerStatuses;
		List<StatusInstance> enemyStatuses;
		boolean lastEnemyMissed;
		boolean lastPlayerGuarded;
		boolean escaped;

		static Working from(Combat2State state) {
			Working working = new Working();
			working.playerHealth = state.playerHealth();
			working.playerStamina = state.playerStamina();
			working.enemyHealth = state.enemyHealth();
			working.enemyStamina = state.enemyStamina();
			working.playerStatuses = new ArrayList<>(state.playerStatuses());
			working.enemyStatuses = new ArrayList<>(state.enemyStatuses());
			working.lastEnemyMissed = false;
			working.lastPlayerGuarded = false;
			return working;
		}

		CombatRoundResult toResult(int round, CombatSessionStatus status, List<CombatEvent> events) {
			int playerHealth = status == CombatSessionStatus.PLAYER_LOST ? 0 : this.playerHealth;
			int enemyHealth = status == CombatSessionStatus.PLAYER_WON ? 0 : this.enemyHealth;
			return new CombatRoundResult(
					round,
					playerHealth,
					playerStamina,
					enemyHealth,
					status,
					List.copyOf(events),
					enemyStamina,
					List.copyOf(playerStatuses),
					List.copyOf(enemyStatuses),
					lastEnemyMissed,
					lastPlayerGuarded);
		}
	}
}
