package com.example.game.character.application;

import java.util.List;
import java.util.UUID;

public record CharacterRosterSlotView(
		int slotIndex,
		boolean empty,
		UUID characterId,
		String name,
		String gender,
		String avatarCode,
		int level,
		int gold,
		UUID currentLocationId,
		String locationName,
		int strength,
		int agility,
		int endurance,
		int perception,
		int currentHealth,
		int maxHealth,
		int currentStamina,
		int maxStamina,
		int physicalDamage,
		int accuracy,
		int dodge,
		int criticalChance,
		int armor,
		int healingPotions,
		List<CharacterRosterEquippedView> equipped
) {
	public static CharacterRosterSlotView empty(int slotIndex) {
		return new CharacterRosterSlotView(
				slotIndex,
				true,
				null,
				null,
				null,
				null,
				0,
				0,
				null,
				null,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				List.of());
	}
}
