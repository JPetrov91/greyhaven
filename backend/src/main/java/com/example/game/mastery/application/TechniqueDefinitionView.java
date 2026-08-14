package com.example.game.mastery.application;

import com.example.game.item.domain.WeaponFamily;
import com.example.game.mastery.domain.TechniqueKind;

public record TechniqueDefinitionView(
		String code,
		String displayName,
		String description,
		WeaponFamily weaponFamily,
		int unlockMasteryLevel,
		TechniqueKind kind,
		boolean unlocked,
		int staminaCost,
		int accuracyModifier,
		int damagePercentModifier,
		String appliesStatus,
		String tags
) {
}
