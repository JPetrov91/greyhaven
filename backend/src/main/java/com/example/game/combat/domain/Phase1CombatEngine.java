package com.example.game.combat.domain;

import java.util.ArrayList;
import java.util.List;

import com.example.game.shared.domain.RandomProvider;

/**
 * Frozen Phase 1 combat resolution. Used only for {@link CombatRulesVersion#PHASE_1} sessions so
 * in-flight fights keep linear armor, uncapped crit, and the original six actions.
 */
public final class Phase1CombatEngine {

	private Phase1CombatEngine() {
	}

	public static CombatRoundResult resolve(
			CombatSessionState state,
			CombatAction action,
			CombatActionContext actionContext,
			RandomProvider random) {
		if (state.status() != CombatSessionStatus.ACTIVE) {
			throw new CombatRuleViolation(
					CombatRuleViolation.Reason.COMBAT_NOT_ACTIVE,
					"combat is not active");
		}
		if (action == CombatAction.USE_TECHNIQUE) {
			throw new CombatRuleViolation(
					CombatRuleViolation.Reason.INVALID_TECHNIQUE,
					"techniques are not available in legacy combat");
		}

		validateAction(state, action, actionContext);

		int round = state.roundNumber() + 1;
		int playerHealth = state.playerHealth();
		int playerStamina = state.playerStamina();
		int enemyHealth = state.enemyHealth();
		boolean defending = false;
		List<CombatEvent> events = new ArrayList<>();

		switch (action) {
			case QUICK_ATTACK, HEAVY_ATTACK, PRECISE_ATTACK -> {
				int cost = ActionCombatBalance.staminaCost(action);
				playerStamina -= cost;
				AttackOutcome attack = resolvePlayerAttack(state, action, random);
				events.addAll(attack.events());
				enemyHealth = Math.max(0, enemyHealth - attack.damageDealt());
			}
			case DEFEND -> {
				defending = true;
				playerStamina = Math.min(
						state.playerMaxStamina(),
						playerStamina + ActionCombatBalance.DEFEND_STAMINA_RESTORE);
				events.add(new CombatEvent(
						CombatEventType.PLAYER_DEFEND,
						"You take a defensive stance and catch your breath."));
			}
			case USE_POTION -> {
				int healed = Math.min(
						state.playerMaxHealth() - playerHealth,
						actionContext.potionHealAmount());
				playerHealth += healed;
				events.add(new CombatEvent(
						CombatEventType.PLAYER_POTION,
						"You drink a healing potion and recover " + healed + " health."));
			}
			case RETREAT -> {
				int chance = ActionCombatBalance.retreatChance(state.playerStats().agility());
				if (random.chancePercent(chance)) {
					events.add(new CombatEvent(
							CombatEventType.PLAYER_RETREAT_SUCCESS,
							"You slip away from the " + state.monster().name() + "."));
					events.add(new CombatEvent(
							CombatEventType.COMBAT_ESCAPED,
							"You escaped combat."));
					return CombatRoundResult.phase1(
							round,
							playerHealth,
							playerStamina,
							enemyHealth,
							CombatSessionStatus.PLAYER_ESCAPED,
							List.copyOf(events));
				}
				events.add(new CombatEvent(
						CombatEventType.PLAYER_RETREAT_FAIL,
						"You fail to escape the " + state.monster().name() + "."));
			}
			case USE_TECHNIQUE -> throw new CombatRuleViolation(
					CombatRuleViolation.Reason.INVALID_TECHNIQUE,
					"techniques are not available in legacy combat");
		}

		if (enemyHealth <= 0) {
			events.add(new CombatEvent(
					CombatEventType.COMBAT_WON,
					"You defeated the " + state.monster().name() + "!"));
			return CombatRoundResult.phase1(
					round,
					playerHealth,
					playerStamina,
					0,
					CombatSessionStatus.PLAYER_WON,
					List.copyOf(events));
		}

		EnemyAttackOutcome enemyAttack = resolveEnemyAttack(state, defending, random);
		events.addAll(enemyAttack.events());
		playerHealth = Math.max(0, playerHealth - enemyAttack.damageDealt());

		if (playerHealth <= 0) {
			events.add(new CombatEvent(
					CombatEventType.COMBAT_LOST,
					"You were defeated by the " + state.monster().name() + "."));
			return CombatRoundResult.phase1(
					round,
					0,
					playerStamina,
					enemyHealth,
					CombatSessionStatus.PLAYER_LOST,
					List.copyOf(events));
		}

		return CombatRoundResult.phase1(
				round,
				playerHealth,
				playerStamina,
				enemyHealth,
				CombatSessionStatus.ACTIVE,
				List.copyOf(events));
	}

	private static void validateAction(
			CombatSessionState state,
			CombatAction action,
			CombatActionContext actionContext) {
		int cost = ActionCombatBalance.staminaCost(action);
		if (state.playerStamina() < cost) {
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

	private record AttackOutcome(int damageDealt, List<CombatEvent> events) {
	}

	private static AttackOutcome resolvePlayerAttack(
			CombatSessionState state,
			CombatAction action,
			RandomProvider random) {
		CombatantStats stats = state.playerStats();
		int hitChance = ActionCombatBalance.clampHitChance(
				(int) Math.round(stats.accuracy() * ActionCombatBalance.accuracyMultiplier(action))
						- ActionCombatBalance.enemyDodge(state.monster().level()));
		List<CombatEvent> events = new ArrayList<>();

		if (!random.chancePercent(hitChance)) {
			events.add(new CombatEvent(
					CombatEventType.PLAYER_MISS,
					"Your attack misses the " + state.monster().name() + "."));
			return new AttackOutcome(0, events);
		}

		int baseDamage = (int) Math.round(stats.physicalDamage() * ActionCombatBalance.damageMultiplier(action));
		int critChance = Math.min(100, stats.criticalChance() + ActionCombatBalance.critBonus(action));
		boolean crit = random.chancePercent(critChance);
		int damage = crit ? baseDamage * ActionCombatBalance.CRITICAL_DAMAGE_MULT : baseDamage;
		damage = Math.max(1, damage);

		if (crit) {
			events.add(new CombatEvent(
					CombatEventType.PLAYER_CRIT,
					"Critical hit! You strike the " + state.monster().name() + " for " + damage + " damage."));
		}
		else {
			events.add(new CombatEvent(
					CombatEventType.PLAYER_ATTACK,
					"You strike the " + state.monster().name() + " for " + damage + " damage."));
		}
		return new AttackOutcome(damage, events);
	}

	private record EnemyAttackOutcome(int damageDealt, List<CombatEvent> events) {
	}

	private static EnemyAttackOutcome resolveEnemyAttack(
			CombatSessionState state,
			boolean defending,
			RandomProvider random) {
		MonsterCombatStats monster = state.monster();
		CombatantStats player = state.playerStats();
		int hitChance = ActionCombatBalance.clampHitChance(
				ActionCombatBalance.enemyAccuracy(monster.level()) - player.dodge());
		List<CombatEvent> events = new ArrayList<>();

		if (!random.chancePercent(hitChance)) {
			events.add(new CombatEvent(
					CombatEventType.ENEMY_MISS,
					"The " + monster.name() + " misses you."));
			return new EnemyAttackOutcome(0, events);
		}

		int raw = random.nextInt(monster.damageMin(), monster.damageMax());
		int afterArmor = Math.max(ActionCombatBalance.MIN_DAMAGE_AFTER_ARMOR, raw - player.armor());
		int damage = defending
				? Math.max(0, (int) Math.round(afterArmor * ActionCombatBalance.DEFEND_DAMAGE_TAKEN_MULT))
				: afterArmor;

		String message = defending
				? "The " + monster.name() + " hits you for " + damage + " damage (defended)."
				: "The " + monster.name() + " hits you for " + damage + " damage.";
		events.add(new CombatEvent(CombatEventType.ENEMY_ATTACK, message));
		return new EnemyAttackOutcome(damage, events);
	}
}
