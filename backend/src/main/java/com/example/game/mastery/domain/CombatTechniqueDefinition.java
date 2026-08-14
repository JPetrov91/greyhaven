package com.example.game.mastery.domain;

import com.example.game.item.domain.WeaponFamily;

public record CombatTechniqueDefinition(
		String code,
		String displayName,
		String description,
		WeaponFamily weaponFamily,
		int unlockMasteryLevel,
		TechniqueKind kind,
		TechniqueEffectSpec effect
) {
}
