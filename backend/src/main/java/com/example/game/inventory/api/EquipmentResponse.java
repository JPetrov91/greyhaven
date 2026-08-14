package com.example.game.inventory.api;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.example.game.inventory.application.EquipmentView;
import com.example.game.inventory.domain.EquipmentSlot;

public record EquipmentResponse(Map<String, UUID> slots) {

	public static EquipmentResponse from(EquipmentView equipment) {
		Map<String, UUID> slots = new LinkedHashMap<>();
		for (EquipmentSlot slot : EquipmentSlot.values()) {
			slots.put(slot.name(), equipment.itemIn(slot));
		}
		return new EquipmentResponse(slots);
	}
}
