package com.example.game.item.domain;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.game.inventory.domain.EquipmentSlot;

public record AffixDefinition(
		String code,
		AffixKind kind,
		String displayName,
		AffixStat stat,
		int magnitudeMin,
		int magnitudeMax,
		Set<ItemType> allowedItemTypes,
		Set<EquipmentSlot> allowedEquipmentSlots,
		Set<WeaponFamily> allowedWeaponFamilies,
		Set<ArmorCategory> allowedArmorCategories
) {

	public boolean compatibleWith(ItemType type, EquipmentSlot slot, WeaponFamily family, ArmorCategory category) {
		if (!allowedItemTypes.isEmpty() && !allowedItemTypes.contains(type)) {
			return false;
		}
		if (!allowedEquipmentSlots.isEmpty() && (slot == null || !allowedEquipmentSlots.contains(slot))) {
			return false;
		}
		if (!allowedWeaponFamilies.isEmpty() && (family == null || !allowedWeaponFamilies.contains(family))) {
			return false;
		}
		if (!allowedArmorCategories.isEmpty() && (category == null || !allowedArmorCategories.contains(category))) {
			return false;
		}
		return true;
	}

	public static Set<ItemType> parseItemTypes(String raw) {
		return parse(raw, ItemType.class);
	}

	public static Set<EquipmentSlot> parseSlots(String raw) {
		return parse(raw, EquipmentSlot.class);
	}

	public static Set<WeaponFamily> parseFamilies(String raw) {
		return parse(raw, WeaponFamily.class);
	}

	public static Set<ArmorCategory> parseCategories(String raw) {
		return parse(raw, ArmorCategory.class);
	}

	private static <E extends Enum<E>> Set<E> parse(String raw, Class<E> type) {
		if (raw == null || raw.isBlank()) {
			return Set.of();
		}
		return Arrays.stream(raw.split(","))
				.map(String::trim)
				.filter(part -> !part.isEmpty())
				.map(part -> Enum.valueOf(type, part.toUpperCase(Locale.ROOT)))
				.collect(Collectors.toUnmodifiableSet());
	}

	public static List<String> csv(Set<? extends Enum<?>> values) {
		return values.stream().map(Enum::name).sorted().toList();
	}
}
