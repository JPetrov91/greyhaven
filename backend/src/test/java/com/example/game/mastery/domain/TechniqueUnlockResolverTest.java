package com.example.game.mastery.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.example.game.item.domain.WeaponFamily;

class TechniqueUnlockResolverTest {

	private final CombatTechniqueCatalog catalog = TestTechniqueCatalogs.standard();

	@Test
	void jumpingFromZeroToFourUnlocksTwoAndFour() {
		assertThat(TechniqueUnlockResolver.unlocksBetween(catalog, WeaponFamily.SWORD, 0, 4))
				.extracting(CombatTechniqueDefinition::code)
				.containsExactly("SWORD_RIPOSTE", "SWORD_DEEP_CUT");
	}

	@Test
	void oddLevelsDoNotUnlock() {
		assertThat(TechniqueUnlockResolver.unlocksBetween(catalog, WeaponFamily.SWORD, 0, 1)).isEmpty();
		assertThat(TechniqueUnlockResolver.unlocksBetween(catalog, WeaponFamily.SWORD, 2, 3)).isEmpty();
		assertThat(TechniqueUnlockResolver.unlocksBetween(catalog, WeaponFamily.SWORD, 4, 5)).isEmpty();
		assertThat(TechniqueUnlockResolver.unlocksBetween(catalog, WeaponFamily.SWORD, 8, 9)).isEmpty();
	}

	@Test
	void sameLevelYieldsNoUnlocks() {
		assertThat(TechniqueUnlockResolver.unlocksBetween(catalog, WeaponFamily.SWORD, 4, 4)).isEmpty();
	}

	@Test
	void swordXpDoesNotUnlockAxeTechniques() {
		assertThat(TechniqueUnlockResolver.unlocksBetween(catalog, WeaponFamily.SWORD, 0, 10))
				.extracting(CombatTechniqueDefinition::weaponFamily)
				.containsOnly(WeaponFamily.SWORD);
	}

	@Test
	void masteryTenUnlocksPassive() {
		assertThat(TechniqueUnlockResolver.unlocksBetween(catalog, WeaponFamily.SWORD, 8, 10))
				.extracting(CombatTechniqueDefinition::code)
				.containsExactly("SWORD_MASTERY");
	}
}
