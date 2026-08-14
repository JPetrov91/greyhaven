package com.example.game.character.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CharacterStatCalculatorTest {

	@Test
	void derivedStatsMatchPhase2FormulasWithStarterEquipment() {
		DerivedCombatStats stats = CharacterStatCalculator.calculate(5, 5, 5, 6, 3);

		assertThat(stats.physicalDamage()).isEqualTo(14); // 6 + 5 * 1.5
		assertThat(stats.accuracy()).isEqualTo(83); // round(75 + 5 * 1.5)
		assertThat(stats.dodge()).isEqualTo(6); // round(5 * 1.2)
		assertThat(stats.criticalChance()).isEqualTo(7); // round(5 + 5 * 0.35)
		assertThat(stats.armor()).isEqualTo(3);
	}

	@Test
	void equipmentBonusesApplyBeforeDerivedFormulas() {
		DerivedCombatStats stats = CharacterStatCalculator.calculate(
				5, 5, 5, 6, 3, 2, 2, 1, 1, 0, 0, 0);

		assertThat(stats.physicalDamage()).isEqualTo(15); // 6 + 6 * 1.5
		assertThat(stats.accuracy()).isEqualTo(85); // round(75 + 5 * 1.5 + 2)
		assertThat(stats.dodge()).isEqualTo(8); // round(5 * 1.2 + 2)
		assertThat(stats.criticalChance()).isEqualTo(8); // round(5 + 5 * 0.35 + 1)
		assertThat(stats.armor()).isEqualTo(3);
	}

	@Test
	void unarmedCharacterUsesAttributeOnlyDamage() {
		DerivedCombatStats stats = CharacterStatCalculator.calculate(5, 5, 5, 0, 0);

		assertThat(stats.physicalDamage()).isEqualTo(8); // 0 + 5 * 1.5
		assertThat(stats.armor()).isEqualTo(0);
	}

	@Test
	void criticalChanceIsCappedAtThirtyFive() {
		DerivedCombatStats stats = CharacterStatCalculator.calculate(
				5, 5, 40, 6, 3, 0, 0, 50, 0, 0, 0, 0);

		assertThat(stats.criticalChance()).isEqualTo(CombatBalance.CRIT_CHANCE_CAP);
	}
}
