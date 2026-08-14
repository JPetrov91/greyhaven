package com.example.game.combat.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class EnemyAiTest {

	@Test
	void defensiveDefendsWhenHealthIsLow() {
		EnemyActionKind action = EnemyAi.choose(view(EnemyAiArchetype.DEFENSIVE, 20, 130, 50, 50, null));
		assertThat(action).isEqualTo(EnemyActionKind.DEFEND);
	}

	@Test
	void aggressivePrefersHeavyWhenAffordable() {
		EnemyActionKind action = EnemyAi.choose(view(EnemyAiArchetype.AGGRESSIVE, 70, 70, 40, 40, null));
		assertThat(action).isEqualTo(EnemyActionKind.HEAVY_ATTACK);
	}

	@Test
	void assassinUsesSignatureWhenAvailable() {
		assertThat(EnemyAi.choose(view(EnemyAiArchetype.ASSASSIN, 70, 70, 40, 40, StatusType.POISON)))
				.isEqualTo(EnemyActionKind.STATUS_ATTACK);
		assertThat(EnemyAi.choose(view(EnemyAiArchetype.ASSASSIN, 70, 70, 40, 40, null)))
				.isEqualTo(EnemyActionKind.BASIC_ATTACK);
	}

	@Test
	void controlUsesStunWhenTargetIsVulnerable() {
		assertThat(EnemyAi.choose(view(EnemyAiArchetype.CONTROL, 80, 80, 40, 40, StatusType.STUN)))
				.isEqualTo(EnemyActionKind.STATUS_ATTACK);
	}

	@Test
	void armoredAppliesArmorBreakUntilCapped() {
		assertThat(EnemyAi.choose(view(EnemyAiArchetype.ARMORED, 200, 220, 55, 55, StatusType.ARMOR_BREAK)))
				.isEqualTo(EnemyActionKind.STATUS_ATTACK);
		EnemyAiView capped = new EnemyAiView(
				EnemyAiArchetype.ARMORED,
				200,
				220,
				55,
				55,
				List.of(),
				100,
				160,
				List.of(new StatusInstance(StatusType.ARMOR_BREAK, 3, 3)),
				StatusType.ARMOR_BREAK,
				MonsterTier.NORMAL);
		assertThat(EnemyAi.choose(capped)).isEqualTo(EnemyActionKind.HEAVY_ATTACK);
	}

	@Test
	void berserkerAppliesBleedUntilCapped() {
		EnemyAiView stacking = view(EnemyAiArchetype.BERSERKER, 80, 90, 45, 45, StatusType.BLEED);
		assertThat(EnemyAi.choose(stacking)).isEqualTo(EnemyActionKind.STATUS_ATTACK);
		EnemyAiView capped = new EnemyAiView(
				EnemyAiArchetype.BERSERKER,
				80,
				90,
				45,
				45,
				List.of(),
				100,
				160,
				List.of(new StatusInstance(StatusType.BLEED, 3, 2)),
				StatusType.BLEED,
				MonsterTier.NORMAL);
		assertThat(EnemyAi.choose(capped)).isEqualTo(EnemyActionKind.BASIC_ATTACK);
	}

	@Test
	void shieldedDefendsUntilGuarded() {
		assertThat(EnemyAi.choose(view(EnemyAiArchetype.SHIELDED, 150, 150, 50, 50, null)))
				.isEqualTo(EnemyActionKind.DEFEND);
		EnemyAiView guarded = new EnemyAiView(
				EnemyAiArchetype.SHIELDED,
				150,
				150,
				50,
				50,
				List.of(new StatusInstance(StatusType.GUARDED, 1, 1)),
				100,
				160,
				List.of(),
				null,
				MonsterTier.NORMAL);
		assertThat(EnemyAi.choose(guarded)).isEqualTo(EnemyActionKind.BASIC_ATTACK);
		EnemyAiView bleedAfterGuard = new EnemyAiView(
				EnemyAiArchetype.SHIELDED,
				210,
				210,
				58,
				58,
				List.of(new StatusInstance(StatusType.GUARDED, 1, 1)),
				100,
				160,
				List.of(),
				StatusType.BLEED,
				MonsterTier.ELITE);
		assertThat(EnemyAi.choose(bleedAfterGuard)).isEqualTo(EnemyActionKind.STATUS_ATTACK);
	}

	@Test
	void marksmanPressesWithBasicAttacksUntilThePlayerIsLow() {
		assertThat(EnemyAi.choose(view(EnemyAiArchetype.MARKSMAN, 95, 95, 45, 45, null)))
				.isEqualTo(EnemyActionKind.BASIC_ATTACK);
		EnemyAiView execute = new EnemyAiView(
				EnemyAiArchetype.MARKSMAN,
				95,
				95,
				45,
				45,
				List.of(),
				20,
				160,
				List.of(),
				null,
				MonsterTier.NORMAL);
		assertThat(EnemyAi.choose(execute)).isEqualTo(EnemyActionKind.HEAVY_ATTACK);
	}

	@Test
	void miniBossControlEnragesIntoHeavyWhenStunIsUnavailable() {
		EnemyAiView enraged = new EnemyAiView(
				EnemyAiArchetype.CONTROL,
				40,
				240,
				60,
				60,
				List.of(),
				100,
				160,
				List.of(new StatusInstance(StatusType.STUN_IMMUNITY, 1, 1)),
				StatusType.STUN,
				MonsterTier.MINI_BOSS);
		assertThat(EnemyAi.choose(enraged)).isEqualTo(EnemyActionKind.HEAVY_ATTACK);
	}

	@Test
	void bossMarksmanEnragesIntoHeavyWhenOffBalanceIsUp() {
		EnemyAiView enraged = new EnemyAiView(
				EnemyAiArchetype.MARKSMAN,
				160,
				360,
				70,
				70,
				List.of(),
				100,
				160,
				List.of(new StatusInstance(StatusType.OFF_BALANCE, 1, 2)),
				StatusType.OFF_BALANCE,
				MonsterTier.BOSS);
		assertThat(EnemyAi.choose(enraged)).isEqualTo(EnemyActionKind.HEAVY_ATTACK);
	}
	@Test
	void heavyFallsBackToBasicWhenExhausted() {
		EnemyActionKind action = EnemyAi.choose(view(EnemyAiArchetype.AGGRESSIVE, 70, 70, 10, 40, null));
		assertThat(action).isEqualTo(EnemyActionKind.BASIC_ATTACK);
	}

	private static EnemyAiView view(
			EnemyAiArchetype archetype,
			int health,
			int maxHealth,
			int stamina,
			int maxStamina,
			StatusType signature) {
		return new EnemyAiView(
				archetype,
				health,
				maxHealth,
				stamina,
				maxStamina,
				List.of(),
				100,
				160,
				List.of(),
				signature,
				MonsterTier.NORMAL);
	}
}
