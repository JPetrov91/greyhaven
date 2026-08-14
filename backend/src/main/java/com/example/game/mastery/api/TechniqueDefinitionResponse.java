package com.example.game.mastery.api;

public record TechniqueDefinitionResponse(
		String code,
		String displayName,
		String description,
		String weaponFamily,
		int unlockMasteryLevel,
		String kind,
		boolean unlocked,
		int staminaCost,
		int accuracyModifier,
		int damagePercentModifier,
		String appliesStatus,
		String tags
) {
}
