package com.example.game.item.domain;

import java.util.ArrayList;
import java.util.List;

import com.example.game.inventory.domain.EquipmentSlot;
import com.example.game.shared.domain.RandomProvider;

public final class AffixGenerator {

	private AffixGenerator() {
	}

	public static List<RolledAffix> generate(
			ItemRarity rarity,
			ItemType type,
			EquipmentSlot slot,
			WeaponFamily family,
			ArmorCategory category,
			AffixCatalog catalog,
			RandomProvider random) {
		int count = ItemBalance.affixCount(rarity);
		if (count <= 0) {
			return List.of();
		}
		if (!type.isEquippable()) {
			throw new IllegalArgumentException("cannot generate affixes for " + type);
		}

		int prefixCount;
		int suffixCount;
		if (count == 1) {
			if (random.nextInt(0, 1) == 0) {
				prefixCount = 1;
				suffixCount = 0;
			}
			else {
				prefixCount = 0;
				suffixCount = 1;
			}
		}
		else if (count == 2) {
			prefixCount = 1;
			suffixCount = 1;
		}
		else {
			if (random.nextInt(0, 1) == 0) {
				prefixCount = 2;
				suffixCount = 1;
			}
			else {
				prefixCount = 1;
				suffixCount = 2;
			}
		}

		List<RolledAffix> rolled = new ArrayList<>();
		List<String> used = new ArrayList<>();
		addAffixes(rolled, used, AffixKind.PREFIX, prefixCount, type, slot, family, category, catalog, random);
		addAffixes(rolled, used, AffixKind.SUFFIX, suffixCount, type, slot, family, category, catalog, random);
		return List.copyOf(rolled);
	}

	private static void addAffixes(
			List<RolledAffix> rolled,
			List<String> used,
			AffixKind requestedKind,
			int count,
			ItemType type,
			EquipmentSlot slot,
			WeaponFamily family,
			ArmorCategory category,
			AffixCatalog catalog,
			RandomProvider random) {
		for (int i = 0; i < count; i++) {
			List<AffixDefinition> pool = catalog.compatible(requestedKind, type, slot, family, category).stream()
					.filter(affix -> !used.contains(affix.code()))
					.toList();
			if (pool.isEmpty()) {
				return;
			}
			AffixDefinition chosen = pool.get(random.nextInt(0, pool.size() - 1));
			used.add(chosen.code());
			int magnitude = random.nextInt(chosen.magnitudeMin(), chosen.magnitudeMax());
			int kindOrdinal = (int) rolled.stream().filter(affix -> affix.kind() == chosen.kind()).count();
			rolled.add(new RolledAffix(chosen.code(), chosen.kind(), kindOrdinal, magnitude));
		}
	}
}
