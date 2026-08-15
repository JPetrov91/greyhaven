package com.example.game.character.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CharacterAppearanceTest {

	@Test
	void defaultsMaleWhenGenderOmitted() {
		assertThat(CharacterAppearance.resolveGender(null)).isEqualTo(CharacterGender.MALE);
		assertThat(CharacterAppearance.defaultAvatar(null)).isEqualTo("male_unyielding");
	}

	@Test
	void acceptsMatchingCatalogCodesOnly() {
		assertThat(CharacterAppearance.isAllowed(CharacterGender.MALE, "male_unyielding")).isTrue();
		assertThat(CharacterAppearance.isAllowed(CharacterGender.FEMALE, "female_veiled")).isTrue();
		assertThat(CharacterAppearance.isAllowed(CharacterGender.MALE, "female_veiled")).isFalse();
		assertThat(CharacterAppearance.isAllowed(CharacterGender.FEMALE, "male_unyielding")).isFalse();
		assertThat(CharacterAppearance.isAllowed(CharacterGender.MALE, "unknown")).isFalse();
		assertThat(CharacterAppearance.allCodes()).hasSize(10);
	}
}
