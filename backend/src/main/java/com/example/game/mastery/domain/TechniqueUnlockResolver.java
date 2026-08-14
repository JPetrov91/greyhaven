package com.example.game.mastery.domain;

import java.util.List;

import com.example.game.item.domain.WeaponFamily;

/**
 * Techniques whose unlock threshold was crossed by a mastery level increase. Empty when the
 * previous and new levels are equal, so re-applying the same grant cannot duplicate unlocks.
 */
public final class TechniqueUnlockResolver {

	private TechniqueUnlockResolver() {
	}

	public static List<CombatTechniqueDefinition> unlocksBetween(
			CombatTechniqueCatalog catalog,
			WeaponFamily weaponFamily,
			int previousLevel,
			int newLevel) {
		if (weaponFamily == null) {
			throw new IllegalArgumentException("weaponFamily is required");
		}
		if (newLevel <= previousLevel) {
			return List.of();
		}
		return catalog.forFamily(weaponFamily).stream()
				.filter(technique -> technique.unlockMasteryLevel() > previousLevel
						&& technique.unlockMasteryLevel() <= newLevel)
				.toList();
	}
}
