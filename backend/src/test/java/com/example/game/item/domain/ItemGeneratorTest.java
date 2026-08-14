package com.example.game.item.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.example.game.inventory.domain.EquipmentSlot;
import com.example.game.shared.domain.ScriptedRandomProvider;

class ItemGeneratorTest {

	@Test
	void catalogLegacyFlagStillRollsNewInstances() {
		GeneratedItem generated = ItemGenerator.generate(
				sword(true),
				TestAffixCatalogs.standard(),
				new ScriptedRandomProvider(1, 100));

		assertThat(generated.rarity()).isEqualTo(ItemRarity.COMMON);
		assertThat(generated.rolledWeaponDamage()).isEqualTo(6);
		assertThat(generated.affixes()).isEmpty();
	}

	@Test
	void commonGenerationUsesBaseVarianceAndNoAffixes() {
		GeneratedItem generated = ItemGenerator.generate(
				sword(false),
				TestAffixCatalogs.standard(),
				new ScriptedRandomProvider(1, 100));

		assertThat(generated.rarity()).isEqualTo(ItemRarity.COMMON);
		assertThat(generated.rolledWeaponDamage()).isEqualTo(6);
		assertThat(generated.affixes()).isEmpty();
	}

	@Test
	void catalogRarityIsAFloor() {
		ItemDefinitionData rareSword = new ItemDefinitionData(
				"STEEL_LONGSWORD",
				"Steel Longsword",
				ItemType.WEAPON,
				ItemRarity.RARE,
				false,
				EquipmentSlot.MAIN_HAND,
				false,
				WeaponFamily.SWORD,
				null,
				10,
				null,
				1,
				0,
				0,
				0,
				0);

		GeneratedItem generated = ItemGenerator.generate(
				rareSword,
				TestAffixCatalogs.standard(),
				new ScriptedRandomProvider(1, 100, 0, 4, 0, 1));

		assertThat(generated.rarity()).isEqualTo(ItemRarity.RARE);
		assertThat(generated.affixes()).hasSize(2);
	}

	@Test
	void sameScriptProducesIdenticalItems() {
		ItemDefinitionData definition = sword(false);
		AffixCatalog catalog = TestAffixCatalogs.standard();
		GeneratedItem first = ItemGenerator.generate(definition, catalog, new ScriptedRandomProvider(61, 105, 0, 0, 4));
		GeneratedItem second = ItemGenerator.generate(definition, catalog, new ScriptedRandomProvider(61, 105, 0, 0, 4));

		assertThat(first).isEqualTo(second);
		assertThat(first.rarity()).isEqualTo(ItemRarity.UNCOMMON);
		assertThat(first.rolledWeaponDamage()).isEqualTo(6);
		assertThat(first.affixes()).hasSize(1);
	}

	@Test
	void consumableDefinitionsDoNotRollAffixes() {
		ItemDefinitionData potion = new ItemDefinitionData(
				"HEALING_POTION",
				"Healing Potion",
				ItemType.CONSUMABLE,
				ItemRarity.COMMON,
				false,
				null,
				false,
				null,
				null,
				null,
				null,
				1,
				0,
				0,
				0,
				0);

		GeneratedItem generated = ItemGenerator.generate(
				potion,
				TestAffixCatalogs.standard(),
				new ScriptedRandomProvider());

		assertThat(generated.affixes()).isEmpty();
		assertThat(generated.rarity()).isEqualTo(ItemRarity.COMMON);
	}

	@Test
	void scriptedValuesOutsideRangeFailFast() {
		assertThatThrownBy(() -> ItemGenerator.generate(
				sword(false),
				TestAffixCatalogs.standard(),
				new ScriptedRandomProvider(200)))
				.isInstanceOf(IllegalStateException.class);
	}

	private static ItemDefinitionData sword(boolean legacy) {
		return new ItemDefinitionData(
				"RUSTY_SWORD",
				"Rusty Sword",
				ItemType.WEAPON,
				ItemRarity.COMMON,
				legacy,
				EquipmentSlot.MAIN_HAND,
				false,
				WeaponFamily.SWORD,
				null,
				6,
				null,
				1,
				0,
				0,
				0,
				0);
	}
}
