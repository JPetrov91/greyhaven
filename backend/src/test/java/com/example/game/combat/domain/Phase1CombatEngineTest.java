package com.example.game.combat.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.example.game.shared.domain.ScriptedRandomProvider;

class Phase1CombatEngineTest {

	private static final CombatantStats PLAYER = new CombatantStats(14, 80, 8, 8, 3, 5);
	private static final MonsterCombatStats RAT = new MonsterCombatStats("Giant Rat", 1, 3, 6);

	@Test
	void quickAttackHitAndEnemyHit() {
		CombatSessionState state = active(100, 80, 50);
		// player hit roll 10 (< hit chance), no crit 90 (>= 8), enemy hit 10, enemy dmg 4
		ScriptedRandomProvider random = new ScriptedRandomProvider(10, 90, 10, 4);

		CombatRoundResult result = Phase1CombatEngine.resolve(
				state,
				CombatAction.QUICK_ATTACK,
				noPotion(),
				random);

		assertThat(result.status()).isEqualTo(CombatSessionStatus.ACTIVE);
		assertThat(result.roundNumber()).isEqualTo(1);
		assertThat(result.enemyHealth()).isEqualTo(36); // 50 - 14
		assertThat(result.playerStamina()).isEqualTo(72); // 80 - 8
		assertThat(result.playerHealth()).isEqualTo(99); // 100 - max(1, 4-3) = 99
		assertThat(result.events()).extracting(CombatEvent::type)
				.contains(CombatEventType.PLAYER_ATTACK, CombatEventType.ENEMY_ATTACK);
	}

	@Test
	void heavyAttackAppliesDamageMultiplierAndStaminaCost() {
		CombatSessionState state = active(100, 80, 50);
		// hit, no crit
		ScriptedRandomProvider random = new ScriptedRandomProvider(5, 90, 90, 3);

		CombatRoundResult result = Phase1CombatEngine.resolve(
				state,
				CombatAction.HEAVY_ATTACK,
				noPotion(),
				random);

		int expectedDamage = (int) Math.round(14 * 1.4);
		assertThat(result.enemyHealth()).isEqualTo(50 - expectedDamage);
		assertThat(result.playerStamina()).isEqualTo(62);
	}

	@Test
	void preciseAttackCanCrit() {
		CombatSessionState state = active(100, 80, 50);
		// hit, crit (crit chance 8+15=23, roll 10), enemy miss
		ScriptedRandomProvider random = new ScriptedRandomProvider(5, 10, 90);

		CombatRoundResult result = Phase1CombatEngine.resolve(
				state,
				CombatAction.PRECISE_ATTACK,
				noPotion(),
				random);

		int base = (int) Math.round(14 * 0.8);
		assertThat(result.enemyHealth()).isEqualTo(50 - (base * 2));
		assertThat(result.events()).extracting(CombatEvent::type)
				.contains(CombatEventType.PLAYER_CRIT, CombatEventType.ENEMY_MISS);
	}

	@Test
	void playerMissDoesNotDamageEnemy() {
		CombatSessionState state = active(100, 80, 50);
		// miss (roll 95), enemy miss
		ScriptedRandomProvider random = new ScriptedRandomProvider(95, 90);

		CombatRoundResult result = Phase1CombatEngine.resolve(
				state,
				CombatAction.QUICK_ATTACK,
				noPotion(),
				random);

		assertThat(result.enemyHealth()).isEqualTo(50);
		assertThat(result.events()).extracting(CombatEvent::type)
				.contains(CombatEventType.PLAYER_MISS);
	}

	@Test
	void defendReducesIncomingDamageAndRestoresStamina() {
		CombatSessionState state = active(100, 40, 50);
		// enemy hit, dmg 6 -> after armor 3 -> defended 2 (round 1.5? 3*0.5=1.5 -> 2)
		ScriptedRandomProvider random = new ScriptedRandomProvider(5, 6);

		CombatRoundResult result = Phase1CombatEngine.resolve(
				state,
				CombatAction.DEFEND,
				noPotion(),
				random);

		assertThat(result.playerStamina()).isEqualTo(48);
		assertThat(result.playerHealth()).isEqualTo(100 - (int) Math.round(3 * 0.5));
		assertThat(result.events()).extracting(CombatEvent::type)
				.contains(CombatEventType.PLAYER_DEFEND, CombatEventType.ENEMY_ATTACK);
	}

	@Test
	void usePotionHealsWithoutSpendingStamina() {
		CombatSessionState state = active(50, 80, 50);
		ScriptedRandomProvider random = new ScriptedRandomProvider(90);

		CombatRoundResult result = Phase1CombatEngine.resolve(
				state,
				CombatAction.USE_POTION,
				new CombatActionContext(true, 40),
				random);

		assertThat(result.playerHealth()).isEqualTo(90);
		assertThat(result.playerStamina()).isEqualTo(80);
		assertThat(result.events()).extracting(CombatEvent::type)
				.contains(CombatEventType.PLAYER_POTION);
	}

	@Test
	void retreatSuccessEndsCombatWithoutEnemyTurn() {
		CombatSessionState state = active(100, 80, 50);
		// retreat chance = 25 + 5*3 = 40; roll 10 succeeds
		ScriptedRandomProvider random = new ScriptedRandomProvider(10);

		CombatRoundResult result = Phase1CombatEngine.resolve(
				state,
				CombatAction.RETREAT,
				noPotion(),
				random);

		assertThat(result.status()).isEqualTo(CombatSessionStatus.PLAYER_ESCAPED);
		assertThat(result.enemyHealth()).isEqualTo(50);
		assertThat(result.events()).extracting(CombatEvent::type)
				.contains(CombatEventType.PLAYER_RETREAT_SUCCESS, CombatEventType.COMBAT_ESCAPED);
	}

	@Test
	void retreatFailureAllowsEnemyAttack() {
		CombatSessionState state = active(100, 80, 50);
		// fail retreat (roll 80 >= 40), enemy miss
		ScriptedRandomProvider random = new ScriptedRandomProvider(80, 90);

		CombatRoundResult result = Phase1CombatEngine.resolve(
				state,
				CombatAction.RETREAT,
				noPotion(),
				random);

		assertThat(result.status()).isEqualTo(CombatSessionStatus.ACTIVE);
		assertThat(result.events()).extracting(CombatEvent::type)
				.contains(CombatEventType.PLAYER_RETREAT_FAIL, CombatEventType.ENEMY_MISS);
	}

	@Test
	void killingBlowSkipsEnemyTurn() {
		CombatSessionState state = active(100, 80, 10);
		ScriptedRandomProvider random = new ScriptedRandomProvider(5, 90);

		CombatRoundResult result = Phase1CombatEngine.resolve(
				state,
				CombatAction.QUICK_ATTACK,
				noPotion(),
				random);

		assertThat(result.status()).isEqualTo(CombatSessionStatus.PLAYER_WON);
		assertThat(result.enemyHealth()).isZero();
		assertThat(result.events()).extracting(CombatEvent::type)
				.contains(CombatEventType.COMBAT_WON)
				.doesNotContain(CombatEventType.ENEMY_ATTACK);
	}

	@Test
	void lethalEnemyAttackEndsInLoss() {
		CombatSessionState state = active(2, 80, 50);
		ScriptedRandomProvider random = new ScriptedRandomProvider(5, 90, 5, 6);

		CombatRoundResult result = Phase1CombatEngine.resolve(
				state,
				CombatAction.QUICK_ATTACK,
				noPotion(),
				random);

		assertThat(result.status()).isEqualTo(CombatSessionStatus.PLAYER_LOST);
		assertThat(result.playerHealth()).isZero();
		assertThat(result.events()).extracting(CombatEvent::type)
				.contains(CombatEventType.COMBAT_LOST);
	}

	@Test
	void insufficientStaminaIsRejected() {
		CombatSessionState state = active(100, 5, 50);

		assertThatThrownBy(() -> Phase1CombatEngine.resolve(
				state,
				CombatAction.HEAVY_ATTACK,
				noPotion(),
				new ScriptedRandomProvider()))
				.isInstanceOfSatisfying(CombatRuleViolation.class, violation -> assertThat(violation.getReason())
						.isEqualTo(CombatRuleViolation.Reason.INSUFFICIENT_STAMINA));
	}

	@Test
	void potionRequiredForUsePotion() {
		CombatSessionState state = active(50, 80, 50);

		assertThatThrownBy(() -> Phase1CombatEngine.resolve(
				state,
				CombatAction.USE_POTION,
				new CombatActionContext(false, 0),
				new ScriptedRandomProvider()))
				.isInstanceOfSatisfying(CombatRuleViolation.class, violation -> assertThat(violation.getReason())
						.isEqualTo(CombatRuleViolation.Reason.NO_POTION));
	}

	@Test
	void inactiveCombatCannotResolve() {
		CombatSessionState state = new CombatSessionState(
				3, 100, 160, 80, 80, 0, CombatSessionStatus.PLAYER_WON, PLAYER, RAT);

		assertThatThrownBy(() -> Phase1CombatEngine.resolve(
				state,
				CombatAction.QUICK_ATTACK,
				noPotion(),
				new ScriptedRandomProvider()))
				.isInstanceOfSatisfying(CombatRuleViolation.class, violation -> assertThat(violation.getReason())
						.isEqualTo(CombatRuleViolation.Reason.COMBAT_NOT_ACTIVE));
	}

	private static CombatSessionState active(int playerHealth, int stamina, int enemyHealth) {
		return new CombatSessionState(
				0,
				playerHealth,
				160,
				stamina,
				80,
				enemyHealth,
				CombatSessionStatus.ACTIVE,
				PLAYER,
				RAT);
	}

	private static CombatActionContext noPotion() {
		return new CombatActionContext(false, 0);
	}
}
