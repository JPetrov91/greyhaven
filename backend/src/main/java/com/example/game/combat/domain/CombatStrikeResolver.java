package com.example.game.combat.domain;

import java.util.ArrayList;
import java.util.List;

import com.example.game.mastery.domain.TechniqueEffectSpec;
import com.example.game.shared.domain.RandomProvider;

/**
 * Shared player-style strike math for PvE CombatEngine and PvP.
 */
public final class CombatStrikeResolver {

	private CombatStrikeResolver() {
	}

	public record Outcome(
			int defenderHealth,
			List<StatusInstance> defenderStatuses,
			List<CombatEvent> events,
			boolean hit
	) {
	}

	public static Outcome resolve(
			CombatantStats attacker,
			CombatantStats defender,
			List<StatusInstance> attackerStatuses,
			List<StatusInstance> defenderStatuses,
			int defenderHealth,
			int defenderMaxHealth,
			TechniqueEffectSpec spec,
			TechniqueEffectSpec masteryPassive,
			boolean technique,
			boolean counterWindow,
			String attackerLabel,
			String defenderLabel,
			boolean playerPerspective,
			CombatantSide defenderSide,
			RandomProvider random) {
		List<CombatEvent> events = new ArrayList<>();
		List<StatusInstance> defenderCurrent = new ArrayList<>(defenderStatuses);
		boolean followUp = hasTag(spec, "COUNTER") || hasTag(spec, "ADVANCED");
		int accuracy = accuracy(attacker, spec, masteryPassive, technique);
		if (StatusEffectEngine.has(attackerStatuses, StatusType.OFF_BALANCE) && !followUp) {
			accuracy -= CombatV2Balance.offBalanceAccuracyPenalty();
		}
		int dodge = defender.dodge();
		if (StatusEffectEngine.has(defenderCurrent, StatusType.OFF_BALANCE)) {
			dodge -= CombatV2Balance.offBalanceDodgePenalty();
		}
		int hitChance = CombatV2Balance.clampHitChance(accuracy - dodge);
		if (!random.chancePercent(hitChance)) {
			events.add(new CombatEvent(
					playerPerspective ? CombatEventType.PLAYER_MISS : CombatEventType.ENEMY_MISS,
					playerPerspective
							? "Your attack misses " + defenderLabel + "."
							: attackerLabel + " misses you."));
			return new Outcome(defenderHealth, List.copyOf(defenderCurrent), List.copyOf(events), false);
		}

		double raw = attacker.physicalDamage() * (1.0 + spec.damagePercentModifier() / 100.0);
		if (masteryPassive != null && masteryPassive.damagePercentModifier() != 0) {
			raw *= 1.0 + masteryPassive.damagePercentModifier() / 100.0;
		}
		if (hasTag(spec, "COUNTER") && counterWindow) {
			raw *= 1.0 + CombatV2Balance.counterDamagePercent() / 100.0;
		}
		if (hasTag(spec, "CLEAVE") && StatusEffectEngine.has(defenderCurrent, StatusType.GUARDED)) {
			raw *= 1.0 + CombatV2Balance.cleaveVsGuardedPercent() / 100.0;
		}
		int defenderHpPercent = defenderMaxHealth <= 0
				? 0
				: (int) Math.round(defenderHealth * 100.0 / defenderMaxHealth);
		if (hasTag(spec, "ADVANCED")
				&& (StatusEffectEngine.has(defenderCurrent, StatusType.OFF_BALANCE)
						|| defenderHpPercent <= CombatV2Balance.advancedHpThresholdPercent())) {
			raw *= 1.0 + CombatV2Balance.advancedDamagePercent() / 100.0;
		}
		int critBonus = technique ? 0 : ActionCombatBalance.critBonus(CombatAction.valueOf(spec.effectCode()));
		boolean critOverride = hasTag(spec, "CRIT_OVERRIDE");
		int critChance = critOverride
				? Math.min(100, attacker.criticalChance() + critBonus)
				: CombatV2Balance.clampCritChance(attacker.criticalChance() + critBonus);
		boolean crit = random.chancePercent(critChance);
		int beforeArmor = Math.max(1, (int) Math.round(raw));
		if (crit) {
			beforeArmor *= CombatV2Balance.criticalDamageMult();
		}
		boolean guarded = StatusEffectEngine.has(defenderCurrent, StatusType.GUARDED);
		int damage = ArmorMitigation.apply(
				beforeArmor,
				defender.armor(),
				StatusEffectEngine.stacks(defenderCurrent, StatusType.ARMOR_BREAK),
				guarded);
		if (guarded) {
			defenderCurrent = new ArrayList<>(StatusEffectEngine.consumeGuardedOnHit(defenderCurrent));
		}
		int remaining = Math.max(0, defenderHealth - damage);
		if (playerPerspective) {
			if (crit) {
				events.add(new CombatEvent(
						CombatEventType.PLAYER_CRIT,
						"Critical hit! You strike " + defenderLabel + " for " + damage + " damage."));
			}
			else {
				events.add(new CombatEvent(
						CombatEventType.PLAYER_ATTACK,
						"You strike " + defenderLabel + " for " + damage + " damage."));
			}
		}
		else if (crit) {
			events.add(new CombatEvent(
					CombatEventType.ENEMY_ATTACK,
					attackerLabel + " lands a critical hit for " + damage + " damage."));
		}
		else {
			events.add(new CombatEvent(
					CombatEventType.ENEMY_ATTACK,
					attackerLabel + " hits you for " + damage + " damage."));
		}
		if (spec.appliesStatus() != null && !spec.appliesStatus().isBlank()) {
			StatusType type = StatusType.valueOf(spec.appliesStatus());
			StatusEffectEngine.StatusApplyResult applied = StatusEffectEngine.apply(
					defenderCurrent,
					type,
					spec.statusStacks(),
					spec.statusDurationRounds(),
					defenderSide);
			defenderCurrent = new ArrayList<>(applied.statuses());
			events.addAll(applied.events());
		}
		return new Outcome(remaining, List.copyOf(defenderCurrent), List.copyOf(events), true);
	}

	public static int previewHitChance(
			CombatantStats attacker,
			CombatantStats defender,
			List<StatusInstance> attackerStatuses,
			List<StatusInstance> defenderStatuses,
			TechniqueEffectSpec spec,
			TechniqueEffectSpec masteryPassive,
			boolean technique) {
		boolean followUp = hasTag(spec, "COUNTER") || hasTag(spec, "ADVANCED");
		int accuracy = accuracy(attacker, spec, masteryPassive, technique);
		if (StatusEffectEngine.has(attackerStatuses, StatusType.OFF_BALANCE) && !followUp) {
			accuracy -= CombatV2Balance.offBalanceAccuracyPenalty();
		}
		int dodge = defender.dodge();
		if (StatusEffectEngine.has(defenderStatuses, StatusType.OFF_BALANCE)) {
			dodge -= CombatV2Balance.offBalanceDodgePenalty();
		}
		return CombatV2Balance.clampHitChance(accuracy - dodge);
	}

	public static TechniqueEffectSpec coreSpec(CombatAction action) {
		return new TechniqueEffectSpec(
				action.name(),
				ActionCombatBalance.staminaCost(action),
				0,
				(int) Math.round((ActionCombatBalance.damageMultiplier(action) - 1.0) * 100),
				null,
				0,
				0,
				"");
	}

	static boolean hasTag(TechniqueEffectSpec spec, String tag) {
		if (spec.tags() == null || spec.tags().isBlank()) {
			return false;
		}
		for (String part : spec.tags().split("[,\\s]+")) {
			if (tag.equalsIgnoreCase(part)) {
				return true;
			}
		}
		return false;
	}

	private static int accuracy(
			CombatantStats attacker,
			TechniqueEffectSpec spec,
			TechniqueEffectSpec masteryPassive,
			boolean technique) {
		int value;
		if (technique) {
			value = attacker.accuracy() + spec.accuracyModifier();
		}
		else {
			value = (int) Math.round(attacker.accuracy() * ActionCombatBalance.accuracyMultiplier(
					CombatAction.valueOf(spec.effectCode())));
		}
		if (masteryPassive != null) {
			value += masteryPassive.accuracyModifier();
		}
		return value;
	}
}
