package com.example.game.mastery.application;

import java.util.List;

import com.example.game.item.domain.WeaponFamily;

public record TechniquesView(
		WeaponFamily equippedWeaponFamily,
		List<TechniqueDefinitionView> techniques,
		TechniqueLoadoutView loadout
) {
}
