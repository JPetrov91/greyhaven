package com.example.game.combat.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class StatusEffectEngineTest {

	@Test
	void bleedStacksToThreeAndRefreshesDuration() {
		List<StatusInstance> first = StatusEffectEngine.apply(
				List.of(), StatusType.BLEED, 1, 3, CombatantSide.ENEMY).statuses();
		List<StatusInstance> second = StatusEffectEngine.apply(
				first, StatusType.BLEED, 1, 2, CombatantSide.ENEMY).statuses();
		List<StatusInstance> third = StatusEffectEngine.apply(
				second, StatusType.BLEED, 2, 3, CombatantSide.ENEMY).statuses();

		assertThat(StatusEffectEngine.stacks(third, StatusType.BLEED)).isEqualTo(3);
		assertThat(StatusEffectEngine.find(third, StatusType.BLEED).orElseThrow().remainingRounds()).isEqualTo(3);
	}

	@Test
	void poisonTicksLowerThanBleedAndExpires() {
		List<StatusInstance> statuses = List.of(new StatusInstance(StatusType.POISON, 1, 4));
		StatusEffectEngine.DotTickResult tick = StatusEffectEngine.tickDots(statuses, CombatantSide.PLAYER);
		assertThat(tick.damage()).isEqualTo(CombatV2Balance.poisonDamagePerStack());
		assertThat(tick.damage()).isLessThan(CombatV2Balance.bleedDamagePerStack());

		List<StatusInstance> remaining = statuses;
		for (int i = 0; i < 4; i++) {
			remaining = StatusEffectEngine.expire(remaining, CombatantSide.PLAYER).statuses();
		}
		assertThat(StatusEffectEngine.has(remaining, StatusType.POISON)).isFalse();
	}

	@Test
	void stunIsResistedWhileImmune() {
		List<StatusInstance> immune = StatusEffectEngine.apply(
				List.of(), StatusType.STUN_IMMUNITY, 1, 2, CombatantSide.ENEMY).statuses();
		StatusEffectEngine.StatusApplyResult resisted = StatusEffectEngine.apply(
				immune, StatusType.STUN, 1, 1, CombatantSide.ENEMY);
		assertThat(resisted.applied()).isFalse();
		assertThat(resisted.events()).extracting(CombatEvent::type).contains(CombatEventType.STATUS_RESISTED);
		assertThat(StatusEffectEngine.has(resisted.statuses(), StatusType.STUN)).isFalse();
	}

	@Test
	void stunIsNotExpiredBeforeItIsConsumed() {
		List<StatusInstance> stunned = List.of(new StatusInstance(StatusType.STUN, 1, 1));
		StatusEffectEngine.ExpireResult expired = StatusEffectEngine.expire(stunned, CombatantSide.PLAYER);
		assertThat(StatusEffectEngine.has(expired.statuses(), StatusType.STUN)).isTrue();
	}

	@Test
	void offBalanceLastsIntoTheFollowingRound() {
		List<StatusInstance> applied = StatusEffectEngine.apply(
				List.of(), StatusType.OFF_BALANCE, 1, 1, CombatantSide.ENEMY).statuses();
		assertThat(StatusEffectEngine.find(applied, StatusType.OFF_BALANCE).orElseThrow().remainingRounds())
				.isGreaterThanOrEqualTo(2);
		List<StatusInstance> afterRound = StatusEffectEngine.expire(applied, CombatantSide.ENEMY).statuses();
		assertThat(StatusEffectEngine.has(afterRound, StatusType.OFF_BALANCE)).isTrue();
	}

	@Test
	void consumingStunAppliesImmunity() {
		List<StatusInstance> stunned = List.of(new StatusInstance(StatusType.STUN, 1, 1));
		StatusEffectEngine.StunConsumeResult result = StatusEffectEngine.consumeStun(stunned, CombatantSide.PLAYER);
		assertThat(result.skipped()).isTrue();
		assertThat(StatusEffectEngine.has(result.statuses(), StatusType.STUN)).isFalse();
		assertThat(StatusEffectEngine.has(result.statuses(), StatusType.STUN_IMMUNITY)).isTrue();
	}

	@Test
	void guardedSurvivesTheApplyRoundThenExpires() {
		List<StatusInstance> guarded = StatusEffectEngine.apply(
				List.of(), StatusType.GUARDED, 1, 1, CombatantSide.PLAYER).statuses();
		assertThat(StatusEffectEngine.consumeGuardedOnHit(guarded)).isEmpty();
		List<StatusInstance> afterApplyRound = StatusEffectEngine.expire(guarded, CombatantSide.PLAYER).statuses();
		assertThat(StatusEffectEngine.has(afterApplyRound, StatusType.GUARDED)).isTrue();
		List<StatusInstance> afterNextRound = StatusEffectEngine.expire(afterApplyRound, CombatantSide.PLAYER).statuses();
		assertThat(StatusEffectEngine.has(afterNextRound, StatusType.GUARDED)).isFalse();
	}

	@Test
	void armorBreakCapsAtThree() {
		List<StatusInstance> statuses = List.of();
		for (int i = 0; i < 5; i++) {
			statuses = StatusEffectEngine.apply(statuses, StatusType.ARMOR_BREAK, 1, 3, CombatantSide.ENEMY).statuses();
		}
		assertThat(StatusEffectEngine.stacks(statuses, StatusType.ARMOR_BREAK)).isEqualTo(3);
	}
}
