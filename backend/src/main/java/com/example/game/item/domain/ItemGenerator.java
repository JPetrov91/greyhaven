package com.example.game.item.domain;

import java.util.List;

import com.example.game.shared.domain.RandomProvider;

public final class ItemGenerator {

	private ItemGenerator() {
	}

	public static GeneratedItem generate(
			ItemDefinitionData definition,
			AffixCatalog catalog,
			RandomProvider random) {
		if (!definition.type().isEquippable()) {
			return new GeneratedItem(
					definition.catalogRarity(),
					definition.weaponDamage(),
					definition.armorValue(),
					List.of());
		}

		ItemRarity rarity = rollRarity(definition.catalogRarity(), ItemRarity.EPIC, 1, 0, random);
		return generateRolled(definition, catalog, random, rarity);
	}

	/**
	 * Crafted equipment rolls rarity inside {@code [minRarity, maxRarity]}, then uses the same base
	 * and affix rolls as loot generation. Rank shifts weight toward the ceiling.
	 */
	public static GeneratedItem generateCrafted(
			ItemDefinitionData definition,
			AffixCatalog catalog,
			RandomProvider random,
			ItemRarity minRarity,
			ItemRarity maxRarity,
			int professionRank,
			int rankRarityBonusPerRank) {
		if (!definition.type().isEquippable()) {
			return generate(definition, catalog, random);
		}
		ItemRarity floor = minRarity == null ? ItemRarity.COMMON : minRarity;
		ItemRarity ceiling = maxRarity == null ? ItemRarity.EPIC : maxRarity;
		if (definition.catalogRarity() != null && definition.catalogRarity().ordinal() > floor.ordinal()) {
			floor = definition.catalogRarity();
		}
		if (floor.ordinal() > ceiling.ordinal()) {
			floor = ceiling;
		}
		ItemRarity rarity = rollRarity(floor, ceiling, professionRank, rankRarityBonusPerRank, random);
		return generateRolled(definition, catalog, random, rarity);
	}

	private static GeneratedItem generateRolled(
			ItemDefinitionData definition,
			AffixCatalog catalog,
			RandomProvider random,
			ItemRarity rarity) {
		Integer rolledWeapon = rollBase(definition.weaponDamage(), random);
		Integer rolledArmor = rollBase(definition.armorValue(), random);
		List<RolledAffix> affixes = AffixGenerator.generate(
				rarity,
				definition.type(),
				definition.equipmentSlot(),
				definition.weaponFamily(),
				definition.armorCategory(),
				catalog,
				random);
		return new GeneratedItem(rarity, rolledWeapon, rolledArmor, affixes);
	}

	private static ItemRarity rollRarity(
			ItemRarity floor,
			ItemRarity ceiling,
			int professionRank,
			int rankRarityBonusPerRank,
			RandomProvider random) {
		ItemRarity minimum = floor == null ? ItemRarity.COMMON : floor;
		ItemRarity maximum = ceiling == null ? ItemRarity.EPIC : ceiling;
		int rank = Math.max(1, professionRank);
		int total = 0;
		for (ItemRarity rarity : ItemRarity.values()) {
			if (rarity.ordinal() >= minimum.ordinal() && rarity.ordinal() <= maximum.ordinal()) {
				total += craftingWeight(rarity, minimum, rank, rankRarityBonusPerRank);
			}
		}
		int roll = random.nextInt(1, total);
		int cursor = 0;
		for (ItemRarity rarity : ItemRarity.values()) {
			if (rarity.ordinal() < minimum.ordinal() || rarity.ordinal() > maximum.ordinal()) {
				continue;
			}
			cursor += craftingWeight(rarity, minimum, rank, rankRarityBonusPerRank);
			if (roll <= cursor) {
				return rarity;
			}
		}
		return minimum;
	}

	public static int craftedRarityWeight(
			ItemRarity rarity,
			ItemRarity minimum,
			int professionRank,
			int rankRarityBonusPerRank) {
		return craftingWeight(rarity, minimum, professionRank, rankRarityBonusPerRank);
	}

	private static int craftingWeight(
			ItemRarity rarity,
			ItemRarity minimum,
			int professionRank,
			int rankRarityBonusPerRank) {
		int bonus = (professionRank - 1)
				* Math.max(0, rankRarityBonusPerRank)
				* (rarity.ordinal() - minimum.ordinal());
		return ItemBalance.rarityWeight(rarity) + Math.max(0, bonus);
	}

	private static Integer rollBase(Integer definitionBase, RandomProvider random) {
		if (definitionBase == null) {
			return null;
		}
		int percent = random.nextInt(ItemBalance.BASE_ROLL_PERCENT_MIN, ItemBalance.BASE_ROLL_PERCENT_MAX);
		int rolled = (int) Math.round(definitionBase * (percent / 100.0));
		if (definitionBase >= 1) {
			return Math.max(1, rolled);
		}
		return Math.max(0, rolled);
	}
}
