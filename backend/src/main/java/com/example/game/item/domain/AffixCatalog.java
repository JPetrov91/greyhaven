package com.example.game.item.domain;

import java.util.List;

import com.example.game.inventory.domain.EquipmentSlot;

public final class AffixCatalog {

	private final List<AffixDefinition> affixes;

	public AffixCatalog(List<AffixDefinition> affixes) {
		this.affixes = List.copyOf(affixes);
	}

	public List<AffixDefinition> all() {
		return affixes;
	}

	public AffixDefinition require(String code) {
		return affixes.stream()
				.filter(affix -> affix.code().equals(code))
				.findFirst()
				.orElseThrow(() -> new IllegalArgumentException("Unknown affix: " + code));
	}

	public List<AffixDefinition> compatible(
			AffixKind kind,
			ItemType type,
			EquipmentSlot slot,
			WeaponFamily family,
			ArmorCategory category) {
		return affixes.stream()
				.filter(affix -> affix.kind() == kind)
				.filter(affix -> affix.compatibleWith(type, slot, family, category))
				.toList();
	}
}
