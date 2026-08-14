package com.example.game.mastery.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.game.item.domain.WeaponFamily;

class CombatActiveTechniqueResolverTest {

	private final CombatTechniqueCatalog catalog = TestTechniqueCatalogs.standard();

	@Test
	void matchingFamilyReturnsActiveCodes() {
		List<String> resolved = CombatActiveTechniqueResolver.resolve(
				Arrays.asList("SWORD_RIPOSTE", null, "SWORD_DEEP_CUT", null),
				WeaponFamily.SWORD,
				catalog);

		assertThat(resolved).containsExactly("SWORD_RIPOSTE", "SWORD_DEEP_CUT");
	}

	@Test
	void mismatchedEquippedFamilyReturnsEmpty() {
		List<String> resolved = CombatActiveTechniqueResolver.resolve(
				Arrays.asList("SWORD_RIPOSTE", null, null, null),
				WeaponFamily.BOW,
				catalog);

		assertThat(resolved).isEmpty();
	}

	@Test
	void unarmedReturnsEmpty() {
		List<String> resolved = CombatActiveTechniqueResolver.resolve(
				Arrays.asList("SWORD_RIPOSTE", null, null, null),
				null,
				catalog);

		assertThat(resolved).isEmpty();
	}

	@Test
	void mixedPersistedFamiliesReturnEmpty() {
		List<String> resolved = CombatActiveTechniqueResolver.resolve(
				Arrays.asList("SWORD_RIPOSTE", "BOW_AIMED_SHOT", null, null),
				WeaponFamily.SWORD,
				catalog);

		assertThat(resolved).isEmpty();
	}

	@Test
	void passiveInLoadoutReturnsEmpty() {
		List<String> resolved = CombatActiveTechniqueResolver.resolve(
				Arrays.asList("SWORD_MASTERY", null, null, null),
				WeaponFamily.SWORD,
				catalog);

		assertThat(resolved).isEmpty();
	}

	@Test
	void unknownCodeReturnsEmpty() {
		List<String> resolved = CombatActiveTechniqueResolver.resolve(
				Arrays.asList("NOT_A_TECHNIQUE", null, null, null),
				WeaponFamily.SWORD,
				catalog);

		assertThat(resolved).isEmpty();
	}
}
