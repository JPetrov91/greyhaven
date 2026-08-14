package com.example.game.mastery.domain;

import java.util.List;

import com.example.game.item.domain.WeaponFamily;

final class TestTechniqueCatalogs {

	private TestTechniqueCatalogs() {
	}

	static CombatTechniqueCatalog standard() {
		return new CombatTechniqueCatalog(List.of(
				active("SWORD_RIPOSTE", WeaponFamily.SWORD, 2),
				active("SWORD_DEEP_CUT", WeaponFamily.SWORD, 4),
				active("SWORD_GUARD_BREAK", WeaponFamily.SWORD, 6),
				active("SWORD_DUELISTS_TEMPO", WeaponFamily.SWORD, 8),
				passive("SWORD_MASTERY", WeaponFamily.SWORD),
				active("AXE_RENDING_CHOP", WeaponFamily.AXE, 2),
				active("BOW_AIMED_SHOT", WeaponFamily.BOW, 2)));
	}

	private static CombatTechniqueDefinition active(String code, WeaponFamily family, int level) {
		return new CombatTechniqueDefinition(
				code,
				code,
				code,
				family,
				level,
				TechniqueKind.ACTIVE,
				new TechniqueEffectSpec(code, 8, 0, 0, null, 0, 0, ""));
	}

	private static CombatTechniqueDefinition passive(String code, WeaponFamily family) {
		return new CombatTechniqueDefinition(
				code,
				code,
				code,
				family,
				10,
				TechniqueKind.PASSIVE,
				new TechniqueEffectSpec(code, 0, 0, 0, null, 0, 0, "MASTERY_PASSIVE"));
	}
}
