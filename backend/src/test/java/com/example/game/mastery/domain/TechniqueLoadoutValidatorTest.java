package com.example.game.mastery.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class TechniqueLoadoutValidatorTest {

	private final CombatTechniqueCatalog catalog = TestTechniqueCatalogs.standard();
	private final Set<String> unlocked = Set.of(
			"SWORD_RIPOSTE",
			"SWORD_DEEP_CUT",
			"SWORD_MASTERY",
			"AXE_RENDING_CHOP",
			"BOW_AIMED_SHOT");

	@Test
	void matchingFamilyLoadoutIsValid() {
		TechniqueLoadoutValidator.Result result = TechniqueLoadoutValidator.validate(
				Arrays.asList("SWORD_RIPOSTE", "SWORD_DEEP_CUT", null, null),
				unlocked,
				catalog);

		assertThat(result.valid()).isTrue();
		assertThat(result.loadoutFamily().name()).isEqualTo("SWORD");
	}

	@Test
	void rejectsWrongSize() {
		TechniqueLoadoutValidator.Result result = TechniqueLoadoutValidator.validate(
				List.of("SWORD_RIPOSTE"),
				unlocked,
				catalog);

		assertThat(result.valid()).isFalse();
		assertThat(result.reason()).contains("exactly 4");
	}

	@Test
	void rejectsDuplicates() {
		TechniqueLoadoutValidator.Result result = TechniqueLoadoutValidator.validate(
				Arrays.asList("SWORD_RIPOSTE", "SWORD_RIPOSTE", null, null),
				unlocked,
				catalog);

		assertThat(result.valid()).isFalse();
		assertThat(result.reason()).contains("same technique");
	}

	@Test
	void rejectsLockedTechnique() {
		TechniqueLoadoutValidator.Result result = TechniqueLoadoutValidator.validate(
				Arrays.asList("SWORD_GUARD_BREAK", null, null, null),
				unlocked,
				catalog);

		assertThat(result.valid()).isFalse();
		assertThat(result.reason()).contains("not unlocked");
	}

	@Test
	void rejectsPassiveInSlot() {
		TechniqueLoadoutValidator.Result result = TechniqueLoadoutValidator.validate(
				Arrays.asList("SWORD_MASTERY", null, null, null),
				unlocked,
				catalog);

		assertThat(result.valid()).isFalse();
		assertThat(result.reason()).contains("Passive");
	}

	@Test
	void rejectsMixedFamilies() {
		TechniqueLoadoutValidator.Result result = TechniqueLoadoutValidator.validate(
				Arrays.asList("SWORD_RIPOSTE", "BOW_AIMED_SHOT", null, null),
				unlocked,
				catalog);

		assertThat(result.valid()).isFalse();
		assertThat(result.reason()).contains("same weapon family");
	}

	@Test
	void emptyLoadoutIsValid() {
		TechniqueLoadoutValidator.Result result = TechniqueLoadoutValidator.validate(
				Arrays.asList(null, null, null, null),
				unlocked,
				catalog);

		assertThat(result.valid()).isTrue();
		assertThat(result.loadoutFamily()).isNull();
	}
}
