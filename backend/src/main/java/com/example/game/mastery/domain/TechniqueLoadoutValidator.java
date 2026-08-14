package com.example.game.mastery.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.game.item.domain.WeaponFamily;

public final class TechniqueLoadoutValidator {

	public static final int SLOT_COUNT = 4;

	private TechniqueLoadoutValidator() {
	}

	public record Result(boolean valid, String reason, WeaponFamily loadoutFamily) {
		public static Result ok(WeaponFamily family) {
			return new Result(true, null, family);
		}

		public static Result invalid(String reason) {
			return new Result(false, reason, null);
		}
	}

	public static Result validate(
			List<String> slots,
			Set<String> unlockedCodes,
			CombatTechniqueCatalog catalog) {
		if (slots == null || slots.size() != SLOT_COUNT) {
			return Result.invalid("Loadout must contain exactly " + SLOT_COUNT + " slots.");
		}
		Set<String> seen = new HashSet<>();
		WeaponFamily family = null;
		for (String code : slots) {
			if (code == null || code.isBlank()) {
				continue;
			}
			String trimmed = code.trim();
			if (!seen.add(trimmed)) {
				return Result.invalid("The same technique cannot occupy more than one loadout slot.");
			}
			if (!unlockedCodes.contains(trimmed)) {
				return Result.invalid("You have not unlocked that technique.");
			}
			CombatTechniqueDefinition definition;
			try {
				definition = catalog.require(trimmed);
			}
			catch (IllegalArgumentException exception) {
				return Result.invalid("Unknown technique.");
			}
			if (definition.kind() != TechniqueKind.ACTIVE) {
				return Result.invalid("Passive techniques cannot be placed in the loadout.");
			}
			if (family == null) {
				family = definition.weaponFamily();
			}
			else if (definition.weaponFamily() != family) {
				return Result.invalid("All loadout techniques must belong to the same weapon family.");
			}
		}
		return Result.ok(family);
	}
}
