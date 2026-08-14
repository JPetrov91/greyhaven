package com.example.game.item.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class ItemStatCalculatorTest {

	@Test
	void lightArmorDoesNotAddCategoryDodgeOnTheItem() {
		ItemStats stats = ItemStatCalculator.calculate(null, 3, List.of());

		assertThat(stats.armor()).isEqualTo(3);
		assertThat(stats.dodge()).isZero();
	}

	@Test
	void heavyArmorDoesNotAddCategoryDodgeOnTheItem() {
		ItemStats stats = ItemStatCalculator.calculate(null, 12, List.of());

		assertThat(stats.armor()).isEqualTo(12);
		assertThat(stats.dodge()).isZero();
	}

	@Test
	void twoLightPiecesDoNotStackCategoryDodge() {
		ItemStats helm = ItemStatCalculator.calculate(null, 1, List.of());
		ItemStats chest = ItemStatCalculator.calculate(null, 3, List.of());

		assertThat(helm.plus(chest).dodge()).isZero();
		assertThat(ItemBalance.armorDodge(ArmorCategory.heaviest(ArmorCategory.LIGHT, ArmorCategory.LIGHT)))
				.isEqualTo(2);
		assertThat(ItemBalance.armorDodge(ArmorCategory.heaviest(ArmorCategory.LIGHT, ArmorCategory.HEAVY)))
				.isEqualTo(-3);
	}

	@Test
	void catalogModifiersApplyWithoutAffixes() {
		ItemStats stats = ItemStatCalculator.calculate(
				8,
				null,
				List.of(
						new ItemStatCalculator.AppliedAffix(AffixStat.ACCURACY, 4),
						new ItemStatCalculator.AppliedAffix(AffixStat.CRIT_CHANCE, 1)),
				List.of());

		assertThat(stats.weaponDamage()).isEqualTo(8);
		assertThat(stats.accuracy()).isEqualTo(4);
		assertThat(stats.criticalChance()).isEqualTo(1);
	}

	@Test
	void bootsCatalogDodgeStacksWithRolledArmor() {
		ItemStats stats = ItemStatCalculator.calculate(
				null,
				1,
				List.of(new ItemStatCalculator.AppliedAffix(AffixStat.DODGE, 1)),
				List.of());

		assertThat(stats.armor()).isEqualTo(1);
		assertThat(stats.dodge()).isEqualTo(1);
	}

	@Test
	void damagePercentAppliesToRolledWeaponBase() {
		ItemStats stats = ItemStatCalculator.calculate(
				10,
				null,
				List.of(new ItemStatCalculator.AppliedAffix(AffixStat.DAMAGE_PERCENT, 10)));

		assertThat(stats.weaponDamage()).isEqualTo(11);
	}

	@Test
	void attributeAffixesStackOnCatalogModifiers() {
		ItemStats stats = ItemStatCalculator.calculate(
				6,
				null,
				List.of(new ItemStatCalculator.AppliedAffix(AffixStat.ACCURACY, 4)),
				List.of(
						new ItemStatCalculator.AppliedAffix(AffixStat.STRENGTH, 2),
						new ItemStatCalculator.AppliedAffix(AffixStat.ACCURACY, 3)));

		assertThat(stats.strength()).isEqualTo(2);
		assertThat(stats.accuracy()).isEqualTo(7);
		assertThat(stats.weaponDamage()).isEqualTo(6);
	}
}
