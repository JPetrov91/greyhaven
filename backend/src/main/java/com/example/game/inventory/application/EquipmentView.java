package com.example.game.inventory.application;

import java.util.Map;
import java.util.UUID;

import com.example.game.inventory.domain.EquipmentSlot;

public record EquipmentView(Map<EquipmentSlot, UUID> slots) {

	public static EquipmentView from(Map<EquipmentSlot, UUID> equipped) {
		return new EquipmentView(Map.copyOf(equipped));
	}

	public UUID itemIn(EquipmentSlot slot) {
		return slots.get(slot);
	}
}
