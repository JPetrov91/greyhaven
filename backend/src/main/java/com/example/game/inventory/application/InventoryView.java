package com.example.game.inventory.application;

import java.util.List;

import com.example.game.character.domain.DerivedCombatStats;

public record InventoryView(
		int capacity,
		int usedSlots,
		List<InventoryItemView> items,
		EquipmentView equipment,
		DerivedCombatStats derivedStats
) {
}
