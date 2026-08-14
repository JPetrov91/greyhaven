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

		ItemRarity rarity = rollRarity(definition.catalogRarity(), random);
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

	private static ItemRarity rollRarity(ItemRarity floor, RandomProvider random) {
		ItemRarity minimum = floor == null ? ItemRarity.COMMON : floor;
		int total = 0;
		for (ItemRarity rarity : ItemRarity.values()) {
			if (rarity.ordinal() >= minimum.ordinal()) {
				total += ItemBalance.rarityWeight(rarity);
			}
		}
		int roll = random.nextInt(1, total);
		int cursor = 0;
		for (ItemRarity rarity : ItemRarity.values()) {
			if (rarity.ordinal() < minimum.ordinal()) {
				continue;
			}
			cursor += ItemBalance.rarityWeight(rarity);
			if (roll <= cursor) {
				return rarity;
			}
		}
		return minimum;
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
