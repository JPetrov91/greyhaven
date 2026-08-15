package com.example.game.character.api;

public record CharacterEquippedSlotResponse(
		String slot,
		String displayName,
		String rarity
) {
}
