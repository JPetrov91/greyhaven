package com.example.game.mastery.domain;

import java.util.ArrayList;
import java.util.List;

import com.example.game.item.domain.WeaponFamily;

/**
 * Combat-facing active techniques. A persisted loadout is ignored unless every occupied slot
 * belongs to the equipped main-hand family.
 */
public final class CombatActiveTechniqueResolver {

	private CombatActiveTechniqueResolver() {
	}

	public static List<String> resolve(
			List<String> loadoutCodes,
			WeaponFamily equippedFamily,
			CombatTechniqueCatalog catalog) {
		if (equippedFamily == null || loadoutCodes == null || catalog == null) {
			return List.of();
		}
		List<String> resolved = new ArrayList<>();
		for (String code : loadoutCodes) {
			if (code == null || code.isBlank()) {
				continue;
			}
			CombatTechniqueDefinition definition;
			try {
				definition = catalog.require(code.trim());
			}
			catch (IllegalArgumentException exception) {
				return List.of();
			}
			if (definition.kind() != TechniqueKind.ACTIVE || definition.weaponFamily() != equippedFamily) {
				return List.of();
			}
			resolved.add(definition.code());
		}
		return List.copyOf(resolved);
	}
}
