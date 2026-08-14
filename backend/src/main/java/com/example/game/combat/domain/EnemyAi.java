package com.example.game.combat.domain;

/**
 * Deterministic archetype tables. Falls back when stamina cannot pay for the preferred action.
 */
public final class EnemyAi {

	private EnemyAi() {
	}

	public static EnemyActionKind choose(EnemyAiView view) {
		EnemyActionKind preferred = preferred(view);
		return affordable(view, preferred);
	}

	private static EnemyActionKind preferred(EnemyAiView view) {
		return switch (view.archetype()) {
			case DEFENSIVE -> {
				if (view.ownHealthPercent() < 40 || view.ownStaminaPercent() < 25) {
					yield EnemyActionKind.DEFEND;
				}
				yield EnemyActionKind.BASIC_ATTACK;
			}
			case AGGRESSIVE -> EnemyActionKind.HEAVY_ATTACK;
			case ASSASSIN -> canStatusAttack(view) ? EnemyActionKind.STATUS_ATTACK : EnemyActionKind.BASIC_ATTACK;
			case ARMORED -> EnemyActionKind.HEAVY_ATTACK;
			case BERSERKER -> {
				if (canStatusAttack(view) && StatusEffectEngine.stacks(view.playerStatuses(), StatusType.BLEED) < 3) {
					yield EnemyActionKind.STATUS_ATTACK;
				}
				if (view.ownHealthPercent() < 50) {
					yield EnemyActionKind.HEAVY_ATTACK;
				}
				yield EnemyActionKind.BASIC_ATTACK;
			}
			case CONTROL -> {
				if (canStatusAttack(view)
						&& !StatusEffectEngine.has(view.playerStatuses(), StatusType.STUN)
						&& !StatusEffectEngine.has(view.playerStatuses(), StatusType.STUN_IMMUNITY)) {
					yield EnemyActionKind.STATUS_ATTACK;
				}
				yield EnemyActionKind.BASIC_ATTACK;
			}
		};
	}

	private static boolean canStatusAttack(EnemyAiView view) {
		return view.signatureStatus() != null
				&& view.ownStamina() >= CombatV2Balance.enemyStatusAttackStaminaCost();
	}

	private static EnemyActionKind affordable(EnemyAiView view, EnemyActionKind preferred) {
		if (canPay(view, preferred)) {
			return preferred;
		}
		if (canPay(view, EnemyActionKind.BASIC_ATTACK)) {
			return EnemyActionKind.BASIC_ATTACK;
		}
		return EnemyActionKind.DEFEND;
	}

	private static boolean canPay(EnemyAiView view, EnemyActionKind action) {
		int cost = switch (action) {
			case BASIC_ATTACK -> CombatV2Balance.enemyBasicStaminaCost();
			case HEAVY_ATTACK -> CombatV2Balance.enemyHeavyStaminaCost();
			case STATUS_ATTACK -> CombatV2Balance.enemyStatusAttackStaminaCost();
			case DEFEND -> 0;
		};
		return view.ownStamina() >= cost;
	}
}
