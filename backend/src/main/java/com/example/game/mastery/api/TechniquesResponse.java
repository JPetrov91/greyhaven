package com.example.game.mastery.api;

import java.util.List;

public record TechniquesResponse(
		String equippedWeaponFamily,
		List<TechniqueDefinitionResponse> techniques,
		TechniqueLoadoutResponse loadout
) {
}
