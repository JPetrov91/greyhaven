package com.example.game.combat.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Generic status stacking, stun anti-chain, DoT ticks, and expiration.
 */
public final class StatusEffectEngine {

	private StatusEffectEngine() {
	}

	public static boolean has(List<StatusInstance> statuses, StatusType type) {
		return find(statuses, type).isPresent();
	}

	public static int stacks(List<StatusInstance> statuses, StatusType type) {
		return find(statuses, type).map(StatusInstance::stacks).orElse(0);
	}

	public static Optional<StatusInstance> find(List<StatusInstance> statuses, StatusType type) {
		return statuses.stream().filter(status -> status.type() == type).findFirst();
	}

	public static StatusApplyResult apply(
			List<StatusInstance> current,
			StatusType type,
			int incomingStacks,
			int durationRounds,
			CombatantSide target) {
		List<StatusInstance> next = replaceable(current);
		if (type == StatusType.STUN && has(next, StatusType.STUN_IMMUNITY)) {
			return new StatusApplyResult(
					List.copyOf(next),
					List.of(new CombatEvent(
							CombatEventType.STATUS_RESISTED,
							label(target) + " resists Stun (immune).")),
					false);
		}
		if (type == StatusType.STUN && has(next, StatusType.STUN)) {
			return new StatusApplyResult(List.copyOf(next), List.of(), false);
		}
		int stacks = Math.max(1, incomingStacks);
		int duration = Math.max(1, durationRounds);
		if (type == StatusType.OFF_BALANCE || type == StatusType.GUARDED) {
			// Duration 1 would expire at the end of the apply round, before the opponent's next hit.
			duration = Math.max(2, duration);
		}
		int cap = capFor(type);
		StatusInstance existing = find(next, type).orElse(null);
		if (existing == null) {
			next.add(new StatusInstance(type, Math.min(cap, stacks), duration));
		}
		else {
			int combined = type == StatusType.OFF_BALANCE
					|| type == StatusType.GUARDED
					|| type == StatusType.STUN
					|| type == StatusType.STUN_IMMUNITY
					? Math.min(cap, stacks)
					: Math.min(cap, existing.stacks() + stacks);
			int remaining = Math.max(existing.remainingRounds(), duration);
			replace(next, new StatusInstance(type, combined, remaining));
		}
		StatusInstance applied = find(next, type).orElseThrow();
		return new StatusApplyResult(
				List.copyOf(next),
				List.of(new CombatEvent(
						CombatEventType.STATUS_APPLIED,
						label(target) + " gains " + type.name()
								+ " (" + applied.stacks() + ", " + applied.remainingRounds() + "r).")),
				true);
	}

	public static StunConsumeResult consumeStun(List<StatusInstance> current, CombatantSide side) {
		List<StatusInstance> next = replaceable(current);
		if (!has(next, StatusType.STUN)) {
			return new StunConsumeResult(List.copyOf(next), List.of(), false);
		}
		next.removeIf(status -> status.type() == StatusType.STUN);
		int immunityDuration = CombatV2Balance.stunImmunityRounds() + 1;
		StatusApplyResult immunity = apply(next, StatusType.STUN_IMMUNITY, 1, immunityDuration, side);
		List<CombatEvent> events = new ArrayList<>();
		events.add(new CombatEvent(
				CombatEventType.ACTION_SKIPPED_STUN,
				label(side) + " is stunned and cannot act."));
		events.addAll(immunity.events());
		return new StunConsumeResult(immunity.statuses(), List.copyOf(events), true);
	}

	public static List<StatusInstance> consumeGuardedOnHit(List<StatusInstance> current) {
		if (!has(current, StatusType.GUARDED)) {
			return current;
		}
		List<StatusInstance> next = replaceable(current);
		next.removeIf(status -> status.type() == StatusType.GUARDED);
		return List.copyOf(next);
	}

	public static DotTickResult tickDots(List<StatusInstance> current, CombatantSide side) {
		int damage = 0;
		List<CombatEvent> events = new ArrayList<>();
		int bleed = stacks(current, StatusType.BLEED);
		if (bleed > 0) {
			int tick = CombatV2Balance.bleedDamagePerStack() * bleed;
			damage += tick;
			events.add(new CombatEvent(
					CombatEventType.STATUS_TICK,
					label(side) + " bleeds for " + tick + " damage."));
		}
		int poison = stacks(current, StatusType.POISON);
		if (poison > 0) {
			int tick = CombatV2Balance.poisonDamagePerStack() * poison;
			damage += tick;
			events.add(new CombatEvent(
					CombatEventType.STATUS_TICK,
					label(side) + " suffers " + tick + " poison damage."));
		}
		return new DotTickResult(damage, List.copyOf(events));
	}

	public static ExpireResult expire(List<StatusInstance> current, CombatantSide side) {
		List<StatusInstance> next = new ArrayList<>();
		List<CombatEvent> events = new ArrayList<>();
		for (StatusInstance status : current) {
			if (status.type() == StatusType.STUN) {
				next.add(status);
				continue;
			}
			int remaining = status.remainingRounds() - 1;
			if (remaining <= 0) {
				events.add(new CombatEvent(
						CombatEventType.STATUS_EXPIRED,
						label(side) + " " + status.type().name() + " expires."));
			}
			else {
				next.add(new StatusInstance(status.type(), status.stacks(), remaining));
			}
		}
		return new ExpireResult(List.copyOf(next), List.copyOf(events));
	}

	private static int capFor(StatusType type) {
		return switch (type) {
			case BLEED -> CombatV2Balance.bleedMaxStacks();
			case POISON -> CombatV2Balance.poisonMaxStacks();
			case ARMOR_BREAK -> CombatV2Balance.armorBreakMaxStacks();
			case STUN, STUN_IMMUNITY, OFF_BALANCE, GUARDED -> 1;
		};
	}

	private static List<StatusInstance> replaceable(List<StatusInstance> current) {
		return new ArrayList<>(current);
	}

	private static void replace(List<StatusInstance> statuses, StatusInstance updated) {
		statuses.removeIf(status -> status.type() == updated.type());
		statuses.add(updated);
	}

	private static String label(CombatantSide side) {
		return side == CombatantSide.PLAYER ? "You" : "The enemy";
	}

	public record StatusApplyResult(List<StatusInstance> statuses, List<CombatEvent> events, boolean applied) {
	}

	public record StunConsumeResult(List<StatusInstance> statuses, List<CombatEvent> events, boolean skipped) {
	}

	public record DotTickResult(int damage, List<CombatEvent> events) {
	}

	public record ExpireResult(List<StatusInstance> statuses, List<CombatEvent> events) {
	}
}
