package com.example.game.combat.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.game.shared.domain.ScriptedRandomProvider;

/**
 * CombatEngine evidence that accuracy, armor-break, and dodge change PvE outcomes.
 */
class CombatBuildMatchupEvaluationTest {

	@Test
	void perceptionAccuracyHitsTheAssassinOnTheSameRollThatStrengthMisses() {
		MonsterCombatProfile rat = new MonsterCombatProfile(
				"Plague Rat", 6, 6, 10, 3, 76, 16, 10, 80, 40, EnemyAiArchetype.ASSASSIN, StatusType.POISON,
				MonsterTier.NORMAL);
		CombatRoundResult perceptive = CombatEngine.resolve(
				state(new CombatantStats(12, 94, 6, 10, 8, 10), rat, 80),
				CombatAction.QUICK_ATTACK,
				null,
				noPotion(),
				new ScriptedRandomProvider(50, 90, 50, 90, 6));
		CombatRoundResult sluggish = CombatEngine.resolve(
				state(new CombatantStats(22, 62, 6, 8, 8, 8), rat, 80),
				CombatAction.QUICK_ATTACK,
				null,
				noPotion(),
				new ScriptedRandomProvider(50, 90, 50, 90, 6));
		assertThat(perceptive.events()).extracting(CombatEvent::type).contains(CombatEventType.PLAYER_ATTACK);
		assertThat(sluggish.events()).extracting(CombatEvent::type).contains(CombatEventType.PLAYER_MISS);
		assertThat(perceptive.enemyHealth()).isLessThan(sluggish.enemyHealth());
	}

	@Test
	void armorBreakRaisesDamageAgainstTheBruteOnTheSameHit() {
		MonsterCombatProfile brute = new MonsterCombatProfile(
				"Cave Brute", 11, 12, 16, 28, 70, 2, 4, 200, 50, EnemyAiArchetype.ARMORED, null, MonsterTier.NORMAL);
		Combat2State intact = state(new CombatantStats(18, 80, 4, 8, 10, 8), brute, 200);
		Combat2State shredded = new Combat2State(
				intact.roundNumber(),
				intact.playerHealth(),
				intact.playerMaxHealth(),
				intact.playerStamina(),
				intact.playerMaxStamina(),
				intact.enemyHealth(),
				intact.enemyMaxHealth(),
				intact.enemyStamina(),
				intact.enemyMaxStamina(),
				intact.status(),
				intact.playerStats(),
				intact.enemy(),
				intact.playerStatuses(),
				List.of(new StatusInstance(StatusType.ARMOR_BREAK, 3, 3)),
				intact.availableTechniqueCodes(),
				intact.techniqueSpecs(),
				intact.masteryPassive(),
				intact.staminaCostReduction(),
				intact.lastEnemyMissed(),
				intact.lastPlayerGuarded());
		CombatRoundResult raw = CombatEngine.resolve(
				intact, CombatAction.QUICK_ATTACK, null, noPotion(), new ScriptedRandomProvider(1, 90, 90, 90, 12));
		CombatRoundResult broken = CombatEngine.resolve(
				shredded, CombatAction.QUICK_ATTACK, null, noPotion(), new ScriptedRandomProvider(1, 90, 90, 90, 12));
		assertThat(broken.enemyHealth()).isLessThan(raw.enemyHealth());
	}

	@Test
	void dodgeTurnsTheMarksmanHitIntoAMissOnTheSameRoll() {
		MonsterCombatProfile smuggler = new MonsterCombatProfile(
				"Smuggler", 6, 9, 13, 4, 92, 8, 8, 95, 45, EnemyAiArchetype.MARKSMAN, StatusType.OFF_BALANCE,
				MonsterTier.NORMAL);
		CombatRoundResult dodgy = CombatEngine.resolve(
				state(new CombatantStats(12, 78, 22, 8, 6, 18), smuggler, 95),
				CombatAction.DEFEND,
				null,
				noPotion(),
				new ScriptedRandomProvider(70));
		CombatRoundResult sluggish = CombatEngine.resolve(
				state(new CombatantStats(18, 78, 4, 8, 12, 6), smuggler, 95),
				CombatAction.DEFEND,
				null,
				noPotion(),
				new ScriptedRandomProvider(70, 90, 9));
		assertThat(dodgy.events()).extracting(CombatEvent::type).contains(CombatEventType.ENEMY_MISS);
		assertThat(sluggish.events()).extracting(CombatEvent::type).contains(CombatEventType.ENEMY_ATTACK);
	}

	private static Combat2State state(CombatantStats player, MonsterCombatProfile enemy, int enemyHealth) {
		return new Combat2State(
				0,
				160,
				160,
				80,
				80,
				enemyHealth,
				enemyHealth,
				enemy.maxStamina(),
				enemy.maxStamina(),
				CombatSessionStatus.ACTIVE,
				player,
				enemy,
				List.of(),
				List.of(),
				List.of(),
				Map.of(),
				null,
				0,
				false,
				false);
	}

	private static CombatActionContext noPotion() {
		return new CombatActionContext(false, 0);
	}
}
