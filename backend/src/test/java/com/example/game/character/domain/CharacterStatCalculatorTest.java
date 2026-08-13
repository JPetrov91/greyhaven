package com.example.game.character.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CharacterStatCalculatorTest {

	@Test
	void derivedStatsMatchMvpFormulasWithStarterEquipment() {
		DerivedCombatStats stats = CharacterStatCalculator.calculate(5, 5, 5, 6, 3);

		assertThat(stats.physicalDamage()).isEqualTo(14); // 6 + 5 * 1.5
		assertThat(stats.accuracy()).isEqualTo(80); // 70 + 5 * 2
		assertThat(stats.dodge()).isEqualTo(8); // 5 * 1.5
		assertThat(stats.criticalChance()).isEqualTo(8); // 5 + 5 * 0.5
		assertThat(stats.armor()).isEqualTo(3);
	}

	@Test
	void unarmedCharacterUsesAttributeOnlyDamage() {
		DerivedCombatStats stats = CharacterStatCalculator.calculate(5, 5, 5, 0, 0);

		assertThat(stats.physicalDamage()).isEqualTo(8); // 0 + 5 * 1.5
		assertThat(stats.armor()).isEqualTo(0);
	}
}
