package com.example.game.combat.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.game.mastery.domain.TechniqueEffectSpec;
import com.example.game.shared.domain.ScriptedRandomProvider;

class CombatEngineV2Test {

	private static final CombatantStats PLAYER = new CombatantStats(20, 90, 8, 10, 10, 10);
	private static final MonsterCombatProfile THUG = new MonsterCombatProfile(
			"Street Thug", 1, 5, 8, 4, 72, 4, 5, 70, 40, EnemyAiArchetype.AGGRESSIVE, null, MonsterTier.NORMAL);

	@Test
	void previewHitChanceMatchesQuickAttackAccuracyMinusDodge() {
		Combat2State state = base().build();
		assertThat(CombatEngine.previewPlayerHitChance(state, CombatAction.QUICK_ATTACK, null))
				.isEqualTo(CombatV2Balance.clampHitChance(90 - 4));
		assertThat(CombatEngine.previewPlayerHitChance(state, CombatAction.HEAVY_ATTACK, null))
				.isEqualTo(CombatV2Balance.clampHitChance((int) Math.round(90 * 0.8) - 4));
	}

	@Test
	void previewEnemyIntentUsesAggressiveHeavyAttack() {
		assertThat(CombatEngine.previewEnemyIntent(base().build())).isEqualTo(EnemyActionKind.HEAVY_ATTACK);
	}

	@Test
	void hitAndMissRespectHitChanceBounds() {
		Combat2State state = base().build();
		CombatRoundResult hit = CombatEngine.resolve(
				state, CombatAction.QUICK_ATTACK, null, noPotion(), new ScriptedRandomProvider(1, 99, 1, 99, 5));
		assertThat(hit.events()).extracting(CombatEvent::type).contains(CombatEventType.PLAYER_ATTACK);

		CombatRoundResult miss = CombatEngine.resolve(
				state, CombatAction.QUICK_ATTACK, null, noPotion(), new ScriptedRandomProvider(99, 1, 99, 5));
		assertThat(miss.events()).extracting(CombatEvent::type).contains(CombatEventType.PLAYER_MISS);
	}

	@Test
	void dodgeCanCauseEnemyMiss() {
		CombatRoundResult result = CombatEngine.resolve(
				base().player(new CombatantStats(20, 90, 90, 10, 10, 10)).build(),
				CombatAction.DEFEND,
				null,
				noPotion(),
				new ScriptedRandomProvider(10));
		assertThat(result.events()).extracting(CombatEvent::type).contains(CombatEventType.ENEMY_MISS);
	}

	@Test
	void criticalHitDoublesDamage() {
		CombatRoundResult crit = CombatEngine.resolve(
				base().build(),
				CombatAction.QUICK_ATTACK,
				null,
				noPotion(),
				new ScriptedRandomProvider(1, 1, 99, 5));
		CombatRoundResult normal = CombatEngine.resolve(
				base().build(),
				CombatAction.QUICK_ATTACK,
				null,
				noPotion(),
				new ScriptedRandomProvider(1, 99, 99, 5));
		assertThat(crit.events()).extracting(CombatEvent::type).contains(CombatEventType.PLAYER_CRIT);
		assertThat(70 - crit.enemyHealth()).isGreaterThan(70 - normal.enemyHealth());
	}

	@Test
	void critCapsAtThirtyFivePercent() {
		Combat2State state = base().player(new CombatantStats(20, 90, 8, 80, 10, 10)).build();
		CombatRoundResult noCrit = CombatEngine.resolve(
				state, CombatAction.QUICK_ATTACK, null, noPotion(), new ScriptedRandomProvider(1, 40, 99, 5));
		assertThat(noCrit.events()).extracting(CombatEvent::type)
				.contains(CombatEventType.PLAYER_ATTACK)
				.doesNotContain(CombatEventType.PLAYER_CRIT);
	}

	@Test
	void missNeverCrits() {
		CombatRoundResult result = CombatEngine.resolve(
				base().build(),
				CombatAction.QUICK_ATTACK,
				null,
				noPotion(),
				new ScriptedRandomProvider(99, 1, 99, 5));
		assertThat(result.events()).extracting(CombatEvent::type).doesNotContain(CombatEventType.PLAYER_CRIT);
	}

	@Test
	void armorDiminishingReturnsNeverZeroWithoutGuard() {
		MonsterCombatProfile armored = new MonsterCombatProfile(
				"Veteran", 5, 15, 22, 150, 80, 4, 5, 220, 55, EnemyAiArchetype.ARMORED, null, MonsterTier.NORMAL);
		CombatRoundResult result = CombatEngine.resolve(
				base().enemy(armored).enemyHealth(220).enemyMaxHealth(220).build(),
				CombatAction.QUICK_ATTACK,
				null,
				noPotion(),
				new ScriptedRandomProvider(1, 99, 99, 5));
		assertThat(result.enemyHealth()).isLessThan(220);
		int dealt = 220 - result.enemyHealth();
		assertThat(dealt).isGreaterThanOrEqualTo(1);
	}

	@Test
	void armorBreakIncreasesDamage() {
		MonsterCombatProfile armored = new MonsterCombatProfile(
				"Veteran", 5, 15, 22, 50, 80, 4, 5, 220, 55, EnemyAiArchetype.ARMORED, null, MonsterTier.NORMAL);
		Combat2State broken = base()
				.enemy(armored)
				.enemyHealth(220)
				.enemyMaxHealth(220)
				.enemyStatuses(List.of(new StatusInstance(StatusType.ARMOR_BREAK, 3, 3)))
				.build();
		Combat2State plain = base().enemy(armored).enemyHealth(220).enemyMaxHealth(220).build();
		int brokenDealt = 220 - CombatEngine.resolve(
				broken, CombatAction.QUICK_ATTACK, null, noPotion(), new ScriptedRandomProvider(1, 99, 99, 5)).enemyHealth();
		int plainDealt = 220 - CombatEngine.resolve(
				plain, CombatAction.QUICK_ATTACK, null, noPotion(), new ScriptedRandomProvider(1, 99, 99, 5)).enemyHealth();
		assertThat(brokenDealt).isGreaterThan(plainDealt);
	}

	@Test
	void bleedTicksAndCanKill() {
		Combat2State state = base()
				.enemyHealth(3)
				.enemyStatuses(List.of(new StatusInstance(StatusType.BLEED, 1, 2)))
				.build();
		CombatRoundResult result = CombatEngine.resolve(
				state, CombatAction.DEFEND, null, noPotion(), new ScriptedRandomProvider(99, 5));
		assertThat(result.events()).extracting(CombatEvent::type).contains(CombatEventType.STATUS_TICK);
		assertThat(result.status()).isEqualTo(CombatSessionStatus.PLAYER_WON);
	}

	@Test
	void deepCutAppliesBleedOnHit() {
		TechniqueEffectSpec deepCut = new TechniqueEffectSpec("DEEP_CUT", 12, 0, 15, "BLEED", 1, 3, "");
		Combat2State state = base()
				.techniques(List.of("SWORD_DEEP_CUT"), Map.of("SWORD_DEEP_CUT", deepCut))
				.build();
		CombatRoundResult result = CombatEngine.resolve(
				state,
				CombatAction.USE_TECHNIQUE,
				"SWORD_DEEP_CUT",
				noPotion(),
				new ScriptedRandomProvider(1, 99, 99, 5));
		assertThat(StatusEffectEngine.has(result.enemyStatuses(), StatusType.BLEED)).isTrue();
		assertThat(result.events()).extracting(CombatEvent::type).contains(CombatEventType.PLAYER_TECHNIQUE);
	}

	@Test
	void stunSkipsThenBlocksChain() {
		TechniqueEffectSpec stun = new TechniqueEffectSpec("CONCUSSIVE_STRIKE", 16, -2, 8, "STUN", 1, 1, "");
		Combat2State first = base()
				.techniques(List.of("MACE_CONCUSSIVE_STRIKE"), Map.of("MACE_CONCUSSIVE_STRIKE", stun))
				.build();
		CombatRoundResult stunned = CombatEngine.resolve(
				first,
				CombatAction.USE_TECHNIQUE,
				"MACE_CONCUSSIVE_STRIKE",
				noPotion(),
				new ScriptedRandomProvider(1, 99));
		assertThat(stunned.events()).extracting(CombatEvent::type).contains(CombatEventType.ACTION_SKIPPED_STUN);
		assertThat(StatusEffectEngine.has(stunned.enemyStatuses(), StatusType.STUN_IMMUNITY)).isTrue();

		Combat2State second = base()
				.round(stunned.roundNumber())
				.playerHealth(stunned.playerHealth())
				.playerStamina(stunned.playerStamina())
				.enemyHealth(stunned.enemyHealth())
				.enemyStamina(stunned.enemyStamina())
				.playerStatuses(stunned.playerStatuses())
				.enemyStatuses(stunned.enemyStatuses())
				.techniques(List.of("MACE_CONCUSSIVE_STRIKE"), Map.of("MACE_CONCUSSIVE_STRIKE", stun))
				.build();
		CombatRoundResult resisted = CombatEngine.resolve(
				second,
				CombatAction.USE_TECHNIQUE,
				"MACE_CONCUSSIVE_STRIKE",
				noPotion(),
				new ScriptedRandomProvider(1, 99, 1, 99, 5));
		assertThat(resisted.events()).extracting(CombatEvent::type).contains(CombatEventType.STATUS_RESISTED);
	}

	@Test
	void defendAppliesGuardedAndHalvesHit() {
		Combat2State state = base().player(new CombatantStats(20, 90, 0, 10, 0, 10)).build();
		CombatRoundResult guarded = CombatEngine.resolve(
				state, CombatAction.DEFEND, null, noPotion(), new ScriptedRandomProvider(1, 99, 8));
		CombatRoundResult unguarded = CombatEngine.resolve(
				state, CombatAction.RETREAT, null, noPotion(), new ScriptedRandomProvider(99, 1, 99, 8));
		assertThat(guarded.events()).extracting(CombatEvent::type).contains(CombatEventType.PLAYER_DEFEND);
		assertThat(guarded.playerHealth()).isGreaterThan(unguarded.playerHealth());
		assertThat(100 - guarded.playerHealth()).isEqualTo(
				(int) Math.round((100 - unguarded.playerHealth()) * CombatV2Balance.guardedDamageTakenMult()));
	}

	@Test
	void poisonAppliesAndTicksAfterTheHit() {
		TechniqueEffectSpec poison = new TechniqueEffectSpec("POISONED_STRIKE", 10, 2, 5, "POISON", 1, 4, "");
		CombatRoundResult result = CombatEngine.resolve(
				base().techniques(List.of("DAGGER_POISONED_STRIKE"), Map.of("DAGGER_POISONED_STRIKE", poison)).build(),
				CombatAction.USE_TECHNIQUE,
				"DAGGER_POISONED_STRIKE",
				noPotion(),
				new ScriptedRandomProvider(1, 99, 99, 5));
		assertThat(StatusEffectEngine.has(result.enemyStatuses(), StatusType.POISON)).isTrue();
		assertThat(result.events()).extracting(CombatEvent::type).doesNotContain(CombatEventType.STATUS_TICK);
	}

	@Test
	void poisonTicksAtTheStartOfTheFollowingRound() {
		TechniqueEffectSpec poison = new TechniqueEffectSpec("POISONED_STRIKE", 10, 2, 5, "POISON", 1, 4, "");
		CombatRoundResult applied = CombatEngine.resolve(
				base().techniques(List.of("DAGGER_POISONED_STRIKE"), Map.of("DAGGER_POISONED_STRIKE", poison)).build(),
				CombatAction.USE_TECHNIQUE,
				"DAGGER_POISONED_STRIKE",
				noPotion(),
				new ScriptedRandomProvider(1, 99, 99, 5));
		CombatRoundResult next = CombatEngine.resolve(
				base()
						.round(applied.roundNumber())
						.playerHealth(applied.playerHealth())
						.playerStamina(applied.playerStamina())
						.enemyHealth(applied.enemyHealth())
						.enemyStamina(applied.enemyStamina())
						.enemyStatuses(applied.enemyStatuses())
						.build(),
				CombatAction.DEFEND,
				null,
				noPotion(),
				new ScriptedRandomProvider(99, 5));
		assertThat(next.events()).extracting(CombatEvent::type).contains(CombatEventType.STATUS_TICK);
	}

	@Test
	void playerStunSkipsTheActionThenGrantsImmunity() {
		CombatRoundResult result = CombatEngine.resolve(
				base().playerStatuses(List.of(new StatusInstance(StatusType.STUN, 1, 1))).build(),
				CombatAction.QUICK_ATTACK,
				null,
				noPotion(),
				new ScriptedRandomProvider(99, 5));
		assertThat(result.events()).extracting(CombatEvent::type).contains(CombatEventType.ACTION_SKIPPED_STUN);
		assertThat(StatusEffectEngine.has(result.playerStatuses(), StatusType.STUN)).isFalse();
		assertThat(StatusEffectEngine.has(result.playerStatuses(), StatusType.STUN_IMMUNITY)).isTrue();
		assertThat(result.playerStamina()).isEqualTo(80);
	}

	@Test
	void offBalanceLowersHitChance() {
		Combat2State balanced = base().build();
		Combat2State offBalance = base()
				.playerStatuses(List.of(new StatusInstance(StatusType.OFF_BALANCE, 1, 2)))
				.build();
		CombatRoundResult hit = CombatEngine.resolve(
				balanced, CombatAction.QUICK_ATTACK, null, noPotion(), new ScriptedRandomProvider(80, 99, 99, 5));
		CombatRoundResult miss = CombatEngine.resolve(
				offBalance, CombatAction.QUICK_ATTACK, null, noPotion(), new ScriptedRandomProvider(80, 99, 5));
		assertThat(hit.events()).extracting(CombatEvent::type).contains(CombatEventType.PLAYER_ATTACK);
		assertThat(miss.events()).extracting(CombatEvent::type).contains(CombatEventType.PLAYER_MISS);
	}

	@Test
	void bleedExpiresAfterItsDuration() {
		Combat2State state = base()
				.enemyStatuses(List.of(new StatusInstance(StatusType.BLEED, 1, 1)))
				.build();
		CombatRoundResult result = CombatEngine.resolve(
				state, CombatAction.DEFEND, null, noPotion(), new ScriptedRandomProvider(99, 5));
		assertThat(result.events()).extracting(CombatEvent::type).contains(CombatEventType.STATUS_EXPIRED);
		assertThat(StatusEffectEngine.has(result.enemyStatuses(), StatusType.BLEED)).isFalse();
	}

	@Test
	void bleedStacksOnRepeatedHits() {
		TechniqueEffectSpec deepCut = new TechniqueEffectSpec("DEEP_CUT", 12, 0, 15, "BLEED", 1, 3, "");
		Combat2State first = base()
				.techniques(List.of("SWORD_DEEP_CUT"), Map.of("SWORD_DEEP_CUT", deepCut))
				.enemyStatuses(List.of(new StatusInstance(StatusType.BLEED, 1, 2)))
				.build();
		CombatRoundResult result = CombatEngine.resolve(
				first,
				CombatAction.USE_TECHNIQUE,
				"SWORD_DEEP_CUT",
				noPotion(),
				new ScriptedRandomProvider(1, 99, 99, 5));
		assertThat(StatusEffectEngine.stacks(result.enemyStatuses(), StatusType.BLEED)).isEqualTo(2);
	}

	@Test
	void playerDefeatEndsTheFight() {
		CombatRoundResult result = CombatEngine.resolve(
				base().player(new CombatantStats(20, 90, 0, 10, 0, 10)).playerHealth(1).build(),
				CombatAction.DEFEND,
				null,
				noPotion(),
				new ScriptedRandomProvider(1, 99, 8));
		assertThat(result.status()).isEqualTo(CombatSessionStatus.PLAYER_LOST);
		assertThat(result.playerHealth()).isZero();
		assertThat(result.events()).extracting(CombatEvent::type).contains(CombatEventType.COMBAT_LOST);
	}

	@Test
	void masteryPassiveIncreasesDamage() {
		TechniqueEffectSpec passive = new TechniqueEffectSpec("SWORD_MASTERY", 0, 4, 50, null, 0, 0, "MASTERY_PASSIVE");
		int boosted = 70 - CombatEngine.resolve(
				base().masteryPassive(passive).build(),
				CombatAction.QUICK_ATTACK,
				null,
				noPotion(),
				new ScriptedRandomProvider(1, 99, 99, 5)).enemyHealth();
		int plain = 70 - CombatEngine.resolve(
				base().build(),
				CombatAction.QUICK_ATTACK,
				null,
				noPotion(),
				new ScriptedRandomProvider(1, 99, 99, 5)).enemyHealth();
		assertThat(boosted).isGreaterThan(plain);
	}

	@Test
	void staminaExhaustionRejectsExpensiveTechnique() {
		TechniqueEffectSpec heavy = new TechniqueEffectSpec("EXECUTIONER", 18, 0, 28, null, 0, 0, "ADVANCED");
		Combat2State state = base()
				.playerStamina(5)
				.techniques(List.of("AXE_EXECUTIONER"), Map.of("AXE_EXECUTIONER", heavy))
				.build();
		assertThatThrownBy(() -> CombatEngine.resolve(
				state,
				CombatAction.USE_TECHNIQUE,
				"AXE_EXECUTIONER",
				noPotion(),
				new ScriptedRandomProvider()))
				.isInstanceOfSatisfying(CombatRuleViolation.class, violation -> assertThat(violation.getReason())
						.isEqualTo(CombatRuleViolation.Reason.INSUFFICIENT_STAMINA));
	}

	@Test
	void staminaCostReductionLowersTechniqueCost() {
		TechniqueEffectSpec chop = new TechniqueEffectSpec("RENDING_CHOP", 10, -2, 18, "BLEED", 1, 2, "");
		Combat2State state = base()
				.playerStamina(8)
				.staminaCostReduction(3)
				.techniques(List.of("AXE_RENDING_CHOP"), Map.of("AXE_RENDING_CHOP", chop))
				.build();
		CombatRoundResult result = CombatEngine.resolve(
				state,
				CombatAction.USE_TECHNIQUE,
				"AXE_RENDING_CHOP",
				noPotion(),
				new ScriptedRandomProvider(1, 99, 99, 5));
		assertThat(result.playerStamina()).isGreaterThanOrEqualTo(8 - 7 + CombatV2Balance.playerStaminaRegen(10));
	}

	@Test
	void riposteGainsCounterWhenLastEnemyMissed() {
		TechniqueEffectSpec riposte = new TechniqueEffectSpec("RIPOSTE", 8, 8, 0, null, 0, 0, "COUNTER");
		Combat2State withCounter = base()
				.lastEnemyMissed(true)
				.techniques(List.of("SWORD_RIPOSTE"), Map.of("SWORD_RIPOSTE", riposte))
				.build();
		Combat2State without = base()
				.techniques(List.of("SWORD_RIPOSTE"), Map.of("SWORD_RIPOSTE", riposte))
				.build();
		int withDamage = 70 - CombatEngine.resolve(
				withCounter,
				CombatAction.USE_TECHNIQUE,
				"SWORD_RIPOSTE",
				noPotion(),
				new ScriptedRandomProvider(1, 99, 99, 5)).enemyHealth();
		int withoutDamage = 70 - CombatEngine.resolve(
				without,
				CombatAction.USE_TECHNIQUE,
				"SWORD_RIPOSTE",
				noPotion(),
				new ScriptedRandomProvider(1, 99, 99, 5)).enemyHealth();
		assertThat(withDamage).isGreaterThan(withoutDamage);
	}

	@Test
	void advancedBonusWhenTargetOffBalance() {
		TechniqueEffectSpec finisher = new TechniqueEffectSpec("FINISHER", 14, 4, 30, null, 0, 0, "ADVANCED");
		Combat2State offBalance = base()
				.enemyStatuses(List.of(new StatusInstance(StatusType.OFF_BALANCE, 1, 1)))
				.techniques(List.of("DAGGER_FINISHER"), Map.of("DAGGER_FINISHER", finisher))
				.build();
		Combat2State plain = base()
				.techniques(List.of("DAGGER_FINISHER"), Map.of("DAGGER_FINISHER", finisher))
				.build();
		int bonus = 70 - CombatEngine.resolve(
				offBalance,
				CombatAction.USE_TECHNIQUE,
				"DAGGER_FINISHER",
				noPotion(),
				new ScriptedRandomProvider(1, 99, 99, 5)).enemyHealth();
		int baseDamage = 70 - CombatEngine.resolve(
				plain,
				CombatAction.USE_TECHNIQUE,
				"DAGGER_FINISHER",
				noPotion(),
				new ScriptedRandomProvider(1, 99, 99, 5)).enemyHealth();
		assertThat(bonus).isGreaterThan(baseDamage);
	}

	@Test
	void killingBlowSkipsEnemyTurn() {
		CombatRoundResult result = CombatEngine.resolve(
				base().enemyHealth(1).build(),
				CombatAction.QUICK_ATTACK,
				null,
				noPotion(),
				new ScriptedRandomProvider(1, 99));
		assertThat(result.status()).isEqualTo(CombatSessionStatus.PLAYER_WON);
		assertThat(result.events()).extracting(CombatEvent::type).doesNotContain(CombatEventType.ENEMY_ATTACK);
	}

	@Test
	void retreatSuccessSkipsEnemy() {
		CombatRoundResult result = CombatEngine.resolve(
				base().build(),
				CombatAction.RETREAT,
				null,
				noPotion(),
				new ScriptedRandomProvider(1));
		assertThat(result.status()).isEqualTo(CombatSessionStatus.PLAYER_ESCAPED);
		assertThat(result.events()).extracting(CombatEvent::type)
				.contains(CombatEventType.PLAYER_RETREAT_SUCCESS, CombatEventType.COMBAT_ESCAPED);
	}

	@Test
	void defensiveAiDefendsAtLowHealth() {
		MonsterCombatProfile bandit = new MonsterCombatProfile(
				"Bandit", 3, 10, 15, 8, 76, 6, 5, 130, 50, EnemyAiArchetype.DEFENSIVE, null, MonsterTier.NORMAL);
		CombatRoundResult result = CombatEngine.resolve(
				base().enemy(bandit).enemyHealth(20).enemyMaxHealth(130).enemyStamina(50).build(),
				CombatAction.QUICK_ATTACK,
				null,
				noPotion(),
				new ScriptedRandomProvider(1, 99));
		assertThat(result.events()).extracting(CombatEvent::type).contains(CombatEventType.ENEMY_TECHNIQUE);
		assertThat(result.events()).extracting(CombatEvent::message)
				.anyMatch(message -> message.contains("GUARDED"));
		assertThat(StatusEffectEngine.has(result.enemyStatuses(), StatusType.GUARDED)).isTrue();
	}

	@Test
	void openingDotsApplyToBothSidesBeforeWinCheck() {
		Combat2State state = base()
				.playerHealth(5)
				.enemyHealth(4)
				.playerStatuses(List.of(new StatusInstance(StatusType.POISON, 1, 2)))
				.enemyStatuses(List.of(new StatusInstance(StatusType.BLEED, 1, 2)))
				.build();
		CombatRoundResult result = CombatEngine.resolve(
				state, CombatAction.DEFEND, null, noPotion(), new ScriptedRandomProvider());
		assertThat(result.status()).isEqualTo(CombatSessionStatus.PLAYER_WON);
		assertThat(result.playerHealth()).isEqualTo(2);
		assertThat(result.events()).extracting(CombatEvent::type)
				.contains(CombatEventType.STATUS_TICK, CombatEventType.COMBAT_WON)
				.doesNotContain(CombatEventType.PLAYER_DEFEND);
	}

	@Test
	void enemyGuardCarriesIntoTheNextRound() {
		MonsterCombatProfile bandit = new MonsterCombatProfile(
				"Bandit", 3, 10, 15, 8, 76, 6, 5, 130, 50, EnemyAiArchetype.DEFENSIVE, null, MonsterTier.NORMAL);
		CombatRoundResult defended = CombatEngine.resolve(
				base().enemy(bandit).enemyHealth(45).enemyMaxHealth(130).enemyStamina(50).build(),
				CombatAction.QUICK_ATTACK,
				null,
				noPotion(),
				new ScriptedRandomProvider(1, 99));
		assertThat(StatusEffectEngine.has(defended.enemyStatuses(), StatusType.GUARDED)).isTrue();

		CombatRoundResult followUp = CombatEngine.resolve(
				base()
						.round(defended.roundNumber())
						.playerHealth(defended.playerHealth())
						.playerStamina(defended.playerStamina())
						.enemy(bandit)
						.enemyHealth(defended.enemyHealth())
						.enemyMaxHealth(130)
						.enemyStamina(defended.enemyStamina())
						.enemyStatuses(defended.enemyStatuses())
						.build(),
				CombatAction.QUICK_ATTACK,
				null,
				noPotion(),
				new ScriptedRandomProvider(1, 99, 99, 5));
		CombatRoundResult unguarded = CombatEngine.resolve(
				base()
						.enemy(bandit)
						.enemyHealth(defended.enemyHealth())
						.enemyMaxHealth(130)
						.enemyStamina(50)
						.build(),
				CombatAction.QUICK_ATTACK,
				null,
				noPotion(),
				new ScriptedRandomProvider(1, 99, 99, 5));
		int guardedDealt = defended.enemyHealth() - followUp.enemyHealth();
		int unguardedDealt = defended.enemyHealth() - unguarded.enemyHealth();
		assertThat(guardedDealt).isLessThan(unguardedDealt);
	}

	@Test
	void cleaveDealsMoreAgainstCarriedGuard() {
		TechniqueEffectSpec cleave = new TechniqueEffectSpec("CLEAVE", 14, -6, 22, null, 0, 0, "CLEAVE");
		TechniqueEffectSpec swing = new TechniqueEffectSpec("SWING", 14, -6, 22, null, 0, 0, "");
		List<StatusInstance> guarded = List.of(new StatusInstance(StatusType.GUARDED, 1, 1));
		int cleaveDamage = 70 - CombatEngine.resolve(
				base()
						.enemyStatuses(guarded)
						.techniques(List.of("AXE_CLEAVE"), Map.of("AXE_CLEAVE", cleave))
						.build(),
				CombatAction.USE_TECHNIQUE,
				"AXE_CLEAVE",
				noPotion(),
				new ScriptedRandomProvider(1, 99, 99, 5)).enemyHealth();
		int swingDamage = 70 - CombatEngine.resolve(
				base()
						.enemyStatuses(guarded)
						.techniques(List.of("AXE_SWING"), Map.of("AXE_SWING", swing))
						.build(),
				CombatAction.USE_TECHNIQUE,
				"AXE_SWING",
				noPotion(),
				new ScriptedRandomProvider(1, 99, 99, 5)).enemyHealth();
		assertThat(cleaveDamage).isGreaterThan(swingDamage);
	}

	@Test
	void unknownTechniqueIsRejected() {
		assertThatThrownBy(() -> CombatEngine.resolve(
				base().build(),
				CombatAction.USE_TECHNIQUE,
				"SWORD_DEEP_CUT",
				noPotion(),
				new ScriptedRandomProvider()))
				.isInstanceOfSatisfying(CombatRuleViolation.class, violation -> assertThat(violation.getReason())
						.isEqualTo(CombatRuleViolation.Reason.INVALID_TECHNIQUE));
	}

	private static CombatActionContext noPotion() {
		return new CombatActionContext(false, 0);
	}

	private static Builder base() {
		return new Builder();
	}

	private static final class Builder {
		private int round;
		private int playerHealth = 100;
		private int playerMaxHealth = 160;
		private int playerStamina = 80;
		private int playerMaxStamina = 80;
		private int enemyHealth = 70;
		private int enemyMaxHealth = 70;
		private int enemyStamina = 40;
		private int enemyMaxStamina = 40;
		private CombatantStats player = PLAYER;
		private MonsterCombatProfile enemy = THUG;
		private List<StatusInstance> playerStatuses = List.of();
		private List<StatusInstance> enemyStatuses = List.of();
		private List<String> codes = List.of();
		private Map<String, TechniqueEffectSpec> specs = Map.of();
		private TechniqueEffectSpec masteryPassive;
		private int staminaCostReduction;
		private boolean lastEnemyMissed;
		private boolean lastPlayerGuarded;

		Builder round(int value) {
			this.round = value;
			return this;
		}

		Builder playerHealth(int value) {
			this.playerHealth = value;
			return this;
		}

		Builder playerStamina(int value) {
			this.playerStamina = value;
			return this;
		}

		Builder enemyHealth(int value) {
			this.enemyHealth = value;
			return this;
		}

		Builder enemyMaxHealth(int value) {
			this.enemyMaxHealth = value;
			return this;
		}

		Builder enemyStamina(int value) {
			this.enemyStamina = value;
			return this;
		}

		Builder player(CombatantStats value) {
			this.player = value;
			return this;
		}

		Builder enemy(MonsterCombatProfile value) {
			this.enemy = value;
			return this;
		}

		Builder playerStatuses(List<StatusInstance> value) {
			this.playerStatuses = value;
			return this;
		}

		Builder enemyStatuses(List<StatusInstance> value) {
			this.enemyStatuses = value;
			return this;
		}

		Builder techniques(List<String> codes, Map<String, TechniqueEffectSpec> specs) {
			this.codes = codes;
			this.specs = specs;
			return this;
		}

		Builder staminaCostReduction(int value) {
			this.staminaCostReduction = value;
			return this;
		}

		Builder lastEnemyMissed(boolean value) {
			this.lastEnemyMissed = value;
			return this;
		}

		Builder masteryPassive(TechniqueEffectSpec value) {
			this.masteryPassive = value;
			return this;
		}

		Combat2State build() {
			return new Combat2State(
					round,
					playerHealth,
					playerMaxHealth,
					playerStamina,
					playerMaxStamina,
					enemyHealth,
					enemyMaxHealth,
					enemyStamina,
					enemyMaxStamina,
					CombatSessionStatus.ACTIVE,
					player,
					enemy,
					playerStatuses,
					enemyStatuses,
					codes,
					specs,
					masteryPassive,
					staminaCostReduction,
					lastEnemyMissed,
					lastPlayerGuarded);
		}
	}
}
