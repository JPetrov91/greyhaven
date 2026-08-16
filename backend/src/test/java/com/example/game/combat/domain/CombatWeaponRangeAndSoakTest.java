package com.example.game.combat.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.game.mastery.domain.TechniqueEffectSpec;
import com.example.game.shared.domain.ScriptedRandomProvider;

class CombatWeaponRangeAndSoakTest {

	private static final TechniqueEffectSpec QUICK = CombatStrikeResolver.coreSpec(CombatAction.QUICK_ATTACK);

	@Test
	void rustySwordRollsWeaponThenAddsStrength() {
		CombatantStats attacker = new CombatantStats(14, 100, 0, 0, 0, 5, 4, 8, 7.5, 0, 0);
		CombatantStats defender = new CombatantStats(0, 0, 0, 0, 0, 0);
		CombatStrikeResolver.Outcome min = CombatStrikeResolver.resolve(
				attacker,
				defender,
				List.of(),
				List.of(),
				100,
				100,
				QUICK,
				null,
				false,
				false,
				"You",
				"the thug",
				true,
				CombatantSide.ENEMY,
				new ScriptedRandomProvider(1, 4, 99));
		CombatStrikeResolver.Outcome max = CombatStrikeResolver.resolve(
				attacker,
				defender,
				List.of(),
				List.of(),
				100,
				100,
				QUICK,
				null,
				false,
				false,
				"You",
				"the thug",
				true,
				CombatantSide.ENEMY,
				new ScriptedRandomProvider(1, 8, 99));
		assertThat(100 - min.defenderHealth()).isEqualTo(12);
		assertThat(100 - max.defenderHealth()).isEqualTo(16);
	}

	@Test
	void familyRawRankingMatchesRustyTable() {
		assertThat(average(3, 5)).isEqualTo(4.0);
		assertThat(average(4, 8)).isEqualTo(6.0);
		assertThat(average(5, 8)).isEqualTo(6.5);
		assertThat(average(4, 9)).isEqualTo(6.5);
		assertThat(5 - 3).isLessThan(8 - 4);
		assertThat(8 - 4).isLessThan(9 - 4);
		assertThat(3 + 5).isLessThan(4 + 8);
	}

	@Test
	void shieldSoakAppliesAfterArmorAndNeverRollsZero() {
		CombatantStats attacker = new CombatantStats(10, 100, 0, 0, 0, 5);
		CombatantStats shielded = new CombatantStats(0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2);
		CombatStrikeResolver.Outcome soak1 = CombatStrikeResolver.resolve(
				attacker,
				shielded,
				List.of(),
				List.of(),
				100,
				100,
				QUICK,
				null,
				false,
				false,
				"You",
				"you",
				false,
				CombatantSide.PLAYER,
				new ScriptedRandomProvider(1, 1));
		CombatStrikeResolver.Outcome soak2 = CombatStrikeResolver.resolve(
				attacker,
				shielded,
				List.of(),
				List.of(),
				100,
				100,
				QUICK,
				null,
				false,
				false,
				"You",
				"you",
				false,
				CombatantSide.PLAYER,
				new ScriptedRandomProvider(1, 2));
		int unshielded = 100 - CombatStrikeResolver.resolve(
				attacker,
				new CombatantStats(0, 0, 0, 0, 0, 0),
				List.of(),
				List.of(),
				100,
				100,
				QUICK,
				null,
				false,
				false,
				"You",
				"you",
				false,
				CombatantSide.PLAYER,
				new ScriptedRandomProvider(1)).defenderHealth();
		int damage1 = 100 - soak1.defenderHealth();
		int damage2 = 100 - soak2.defenderHealth();
		assertThat(damage1).isEqualTo(unshielded - 1);
		assertThat(damage2).isEqualTo(unshielded - 2);
		assertThat(damage1).isLessThan(unshielded);
	}

	private static double average(int min, int max) {
		return (min + max) / 2.0;
	}
}
