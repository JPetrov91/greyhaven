package com.example.game.item.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class ItemDisplayNames {

	private ItemDisplayNames() {
	}

	public static String compose(String definitionName, List<RolledAffix> affixes, AffixCatalog catalog) {
		if (affixes == null || affixes.isEmpty()) {
			return definitionName;
		}
		Map<AffixKind, List<RolledAffix>> byKind = affixes.stream()
				.sorted(Comparator.comparingInt(RolledAffix::ordinal))
				.collect(Collectors.groupingBy(RolledAffix::kind));
		StringBuilder name = new StringBuilder();
		List<RolledAffix> prefixes = byKind.getOrDefault(AffixKind.PREFIX, List.of());
		for (RolledAffix prefix : prefixes) {
			if (!name.isEmpty()) {
				name.append(' ');
			}
			name.append(catalog.require(prefix.affixCode()).displayName());
		}
		if (!name.isEmpty()) {
			name.append(' ');
		}
		name.append(definitionName);
		List<RolledAffix> suffixes = byKind.getOrDefault(AffixKind.SUFFIX, List.of());
		for (RolledAffix suffix : suffixes) {
			name.append(' ').append(catalog.require(suffix.affixCode()).displayName());
		}
		return name.toString();
	}
}
