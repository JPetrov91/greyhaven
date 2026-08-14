package com.example.game.inventory.application;

import java.util.List;
import java.util.UUID;

import com.example.game.inventory.domain.EquipmentSlot;

public record ItemComparisonView(
		EquipmentSlot slot,
		UUID equippedItemId,
		ComparisonVerdict verdict,
		List<StatDeltaView> deltas
) {
}
