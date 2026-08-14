package com.example.game.item.domain;

import java.util.List;
import java.util.Set;

import com.example.game.inventory.domain.EquipmentSlot;

public final class TestAffixCatalogs {

	private TestAffixCatalogs() {
	}

	public static AffixCatalog standard() {
		return new AffixCatalog(List.of(
				prefix("SHARP", AffixStat.DAMAGE_PERCENT, 4, 8, Set.of(ItemType.WEAPON), Set.of(), Set.of(), Set.of()),
				prefix("BALANCED", AffixStat.ACCURACY, 3, 6, Set.of(ItemType.WEAPON), Set.of(), Set.of(), Set.of()),
				prefix("REINFORCED", AffixStat.ARMOR, 2, 5, Set.of(ItemType.ARMOR), Set.of(), Set.of(), Set.of()),
				suffix("OF_STRENGTH", AffixStat.STRENGTH, 1, 3, Set.of(ItemType.WEAPON, ItemType.ARMOR, ItemType.ACCESSORY)),
				suffix("OF_THE_FOX", AffixStat.AGILITY, 1, 3, Set.of(ItemType.WEAPON, ItemType.ARMOR, ItemType.ACCESSORY))));
	}

	private static AffixDefinition prefix(
			String code,
			AffixStat stat,
			int min,
			int max,
			Set<ItemType> types,
			Set<EquipmentSlot> slots,
			Set<WeaponFamily> families,
			Set<ArmorCategory> categories) {
		return new AffixDefinition(code, AffixKind.PREFIX, code, stat, min, max, types, slots, families, categories);
	}

	private static AffixDefinition suffix(String code, AffixStat stat, int min, int max, Set<ItemType> types) {
		return new AffixDefinition(
				code,
				AffixKind.SUFFIX,
				code,
				stat,
				min,
				max,
				types,
				Set.of(),
				Set.of(),
				Set.of());
	}
}
