package com.example.game.crafting.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.game.inventory.domain.EquipmentSlot;
import com.example.game.item.domain.GeneratedItem;
import com.example.game.item.domain.ItemDefinitionData;
import com.example.game.item.domain.ItemRarity;
import com.example.game.item.domain.ItemType;
import com.example.game.item.domain.TestAffixCatalogs;
import com.example.game.item.domain.WeaponFamily;
import com.example.game.shared.domain.ScriptedRandomProvider;

class CraftingDomainTest {

	@Test
	void professionXpRanksUpUsingTheCumulativeCurve() {
		ProfessionProgression.ProgressionResult result = ProfessionProgression.applyExperience(1, 0, 40);
		assertThat(result.rank()).isEqualTo(2);
		assertThat(result.ranksGained()).isEqualTo(1);
		assertThat(result.experience()).isEqualTo(40);
	}

	@Test
	void recipeValidatorRejectsRankLevelGoldAndMaterials() {
		RecipeValidator.RecipeRequirement recipe = new RecipeValidator.RecipeRequirement(
				Profession.BLACKSMITH,
				2,
				3,
				10,
				List.of(new RecipeValidator.RecipeInput("IRON_ORE", 3)));

		assertThat(RecipeValidator.validate(recipe, 1, 10, 100, Map.of("IRON_ORE", 3)))
				.isEqualTo(RecipeValidator.Failure.PROFESSION_RANK);
		assertThat(RecipeValidator.validate(recipe, 2, 2, 100, Map.of("IRON_ORE", 3)))
				.isEqualTo(RecipeValidator.Failure.CHARACTER_LEVEL);
		assertThat(RecipeValidator.validate(recipe, 2, 3, 9, Map.of("IRON_ORE", 3)))
				.isEqualTo(RecipeValidator.Failure.GOLD);
		assertThat(RecipeValidator.validate(recipe, 2, 3, 10, Map.of("IRON_ORE", 2)))
				.isEqualTo(RecipeValidator.Failure.MATERIALS);
		assertThat(RecipeValidator.validate(recipe, 2, 3, 10, Map.of("IRON_ORE", 3))).isNull();
	}

	@Test
	void craftingResolverPersistsTheSameRarityForTheSameScript() {
		ItemDefinitionData sword = new ItemDefinitionData(
				"IRON_SWORD",
				"Iron Sword",
				ItemType.WEAPON,
				ItemRarity.COMMON,
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
		GeneratedItem first = CraftingResolver.resolve(
				java.time.Instant.parse("2026-01-01T00:00:00Z"),
				60,
				sword,
				TestAffixCatalogs.standard(),
				new ScriptedRandomProvider(1, 100),
				ItemRarity.COMMON,
				ItemRarity.RARE,
				1,
				12).generated();
		GeneratedItem second = CraftingResolver.resolve(
				java.time.Instant.parse("2026-01-01T00:00:00Z"),
				60,
				sword,
				TestAffixCatalogs.standard(),
				new ScriptedRandomProvider(1, 100),
				ItemRarity.COMMON,
				ItemRarity.RARE,
				1,
				12).generated();
		assertThat(first).isEqualTo(second);
		assertThat(first.rarity()).isEqualTo(ItemRarity.COMMON);
	}

	@Test
	void salvageRejectsNonEquipmentAndScalesWithRarity() {
		assertThatThrownBy(() -> SalvageCalculator.calculate(
				ItemType.MATERIAL,
				ItemRarity.COMMON,
				List.of(new SalvageCalculator.CatalogLine("WEAPON_COMPONENTS", 1))))
				.isInstanceOf(IllegalArgumentException.class);

		assertThat(SalvageCalculator.calculate(
				ItemType.WEAPON,
				ItemRarity.RARE,
				List.of(new SalvageCalculator.CatalogLine("WEAPON_COMPONENTS", 1))))
				.containsExactly(new SalvageCalculator.SalvageOutput("WEAPON_COMPONENTS", 2));
	}

	@Test
	void highProfessionRankDoesNotMakeEpicMoreLikelyThanCommon() {
		int rank = CraftingBalance.MAX_RANK;
		int bonus = CraftingBalance.RANK_RARITY_BONUS_PER_RANK;
		int common = com.example.game.item.domain.ItemGenerator.craftedRarityWeight(
				ItemRarity.COMMON, ItemRarity.COMMON, rank, bonus);
		int epic = com.example.game.item.domain.ItemGenerator.craftedRarityWeight(
				ItemRarity.EPIC, ItemRarity.COMMON, rank, bonus);
		assertThat(common).isGreaterThan(epic);
		assertThat(common).isGreaterThanOrEqualTo(com.example.game.item.domain.ItemGenerator.craftedRarityWeight(
				ItemRarity.UNCOMMON, ItemRarity.COMMON, rank, bonus));
	}
}
