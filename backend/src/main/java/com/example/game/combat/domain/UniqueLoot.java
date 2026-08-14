package com.example.game.combat.domain;

import java.util.List;
import java.util.Set;

/**
 * Once-per-character loot is rolled only if that character has never been granted the item.
 */
public final class UniqueLoot {

	private UniqueLoot() {
	}

	public static List<LootTableEntry> excludingGranted(List<LootTableEntry> table, Set<String> grantedItemCodes) {
		if (table == null || table.isEmpty()) {
			return List.of();
		}
		Set<String> granted = grantedItemCodes == null ? Set.of() : grantedItemCodes;
		return table.stream()
				.filter(entry -> !entry.oncePerCharacter() || !granted.contains(entry.itemCode()))
				.toList();
	}
}
