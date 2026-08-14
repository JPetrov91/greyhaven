package com.example.game.item.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.example.game.inventory.domain.EquipmentSlot;
import com.example.game.shared.domain.ScriptedRandomProvider;

class AffixGeneratorTest {

	@Test
	void commonHasNoAffixes() {
		List<RolledAffix> affixes = AffixGenerator.generate(
				ItemRarity.COMMON,
				ItemType.WEAPON,
				EquipmentSlot.MAIN_HAND,
				WeaponFamily.SWORD,
				null,
				TestAffixCatalogs.standard(),
				new ScriptedRandomProvider());

		assertThat(affixes).isEmpty();
	}

	@Test
	void uncommonRollsOneCompatiblePrefix() {
		List<RolledAffix> affixes = AffixGenerator.generate(
				ItemRarity.UNCOMMON,
				ItemType.WEAPON,
				EquipmentSlot.MAIN_HAND,
				WeaponFamily.SWORD,
				null,
				TestAffixCatalogs.standard(),
				new ScriptedRandomProvider(0, 0, 4));

		assertThat(affixes).hasSize(1);
		assertThat(affixes.get(0).affixCode()).isEqualTo("SHARP");
		assertThat(affixes.get(0).kind()).isEqualTo(AffixKind.PREFIX);
		assertThat(affixes.get(0).magnitude()).isEqualTo(4);
	}

	@Test
	void rareRollsPrefixAndSuffix() {
		List<RolledAffix> affixes = AffixGenerator.generate(
				ItemRarity.RARE,
				ItemType.WEAPON,
				EquipmentSlot.MAIN_HAND,
				WeaponFamily.SWORD,
				null,
				TestAffixCatalogs.standard(),
				new ScriptedRandomProvider(0, 4, 0, 1));

		assertThat(affixes).hasSize(2);
		assertThat(affixes.get(0).kind()).isEqualTo(AffixKind.PREFIX);
		assertThat(affixes.get(1).kind()).isEqualTo(AffixKind.SUFFIX);
		assertThat(affixes.get(0).affixCode()).isEqualTo("SHARP");
		assertThat(affixes.get(1).affixCode()).isEqualTo("OF_STRENGTH");
	}

	@Test
	void armorPrefixesAreNotChosenForWeapons() {
		List<RolledAffix> affixes = AffixGenerator.generate(
				ItemRarity.UNCOMMON,
				ItemType.WEAPON,
				EquipmentSlot.MAIN_HAND,
				WeaponFamily.SWORD,
				null,
				TestAffixCatalogs.standard(),
				new ScriptedRandomProvider(0, 1, 5));

		assertThat(affixes).hasSize(1);
		assertThat(affixes.get(0).affixCode()).isEqualTo("BALANCED");
	}

	@Test
	void epicRollsThreeAffixesWithoutRepeatingCodes() {
		List<RolledAffix> affixes = AffixGenerator.generate(
				ItemRarity.EPIC,
				ItemType.WEAPON,
				EquipmentSlot.MAIN_HAND,
				WeaponFamily.SWORD,
				null,
				TestAffixCatalogs.standard(),
				new ScriptedRandomProvider(0, 0, 4, 0, 3, 0, 1));

		assertThat(affixes).hasSize(3);
		assertThat(affixes).extracting(RolledAffix::affixCode)
				.containsExactly("SHARP", "BALANCED", "OF_STRENGTH");
		assertThat(affixes).extracting(RolledAffix::kind)
				.containsExactly(AffixKind.PREFIX, AffixKind.PREFIX, AffixKind.SUFFIX);
	}

	@Test
	void lightOnlyPrefixesAreIncompatibleWithHeavyArmor() {
		AffixDefinition nimble = new AffixDefinition(
				"NIMBLE",
				AffixKind.PREFIX,
				"Nimble",
				AffixStat.DODGE,
				2,
				4,
				Set.of(ItemType.ARMOR),
				Set.of(),
				Set.of(),
				Set.of(ArmorCategory.LIGHT));

		assertThat(nimble.compatibleWith(ItemType.ARMOR, EquipmentSlot.CHEST, null, ArmorCategory.LIGHT)).isTrue();
		assertThat(nimble.compatibleWith(ItemType.ARMOR, EquipmentSlot.CHEST, null, ArmorCategory.HEAVY)).isFalse();
		assertThat(nimble.compatibleWith(ItemType.WEAPON, EquipmentSlot.MAIN_HAND, WeaponFamily.SWORD, null)).isFalse();
	}

	@Test
	void doesNotCrossKindWhenTheRequestedPoolIsEmpty() {
		AffixCatalog prefixesOnly = new AffixCatalog(List.of(
				new AffixDefinition(
						"SHARP",
						AffixKind.PREFIX,
						"Sharp",
						AffixStat.DAMAGE_PERCENT,
						4,
						8,
						Set.of(ItemType.WEAPON),
						Set.of(),
						Set.of(),
						Set.of()),
				new AffixDefinition(
						"BALANCED",
						AffixKind.PREFIX,
						"Balanced",
						AffixStat.ACCURACY,
						3,
						6,
						Set.of(ItemType.WEAPON),
						Set.of(),
						Set.of(),
						Set.of())));

		List<RolledAffix> affixes = AffixGenerator.generate(
				ItemRarity.EPIC,
				ItemType.WEAPON,
				EquipmentSlot.MAIN_HAND,
				WeaponFamily.SWORD,
				null,
				prefixesOnly,
				new ScriptedRandomProvider(0, 0, 4, 0, 3));

		assertThat(affixes).hasSize(2);
		assertThat(affixes).extracting(RolledAffix::kind).containsOnly(AffixKind.PREFIX);
	}

	@Test
	void consumablesCannotReceiveAffixes() {
		assertThatThrownBy(() -> AffixGenerator.generate(
				ItemRarity.UNCOMMON,
				ItemType.CONSUMABLE,
				null,
				null,
				null,
				TestAffixCatalogs.standard(),
				new ScriptedRandomProvider(0, 0, 1)))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("CONSUMABLE");
	}
}
