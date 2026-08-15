package com.example.game.character.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class CharacterNameRulesTest {

	private static final Pattern PATTERN = Pattern.compile(CharacterNameRules.PATTERN);

	@Test
	void acceptsLettersDigitsAndSingleSpaces() {
		assertThat(PATTERN.matcher("Ragnar Ironfist").matches()).isTrue();
		assertThat(PATTERN.matcher("Seren").matches()).isTrue();
		assertThat(PATTERN.matcher("Nyx2").matches()).isTrue();
		assertThat(PATTERN.matcher("Мара Тень").matches()).isTrue();
	}

	@Test
	void rejectsSpecialCharactersAndOddSpacing() {
		assertThat(PATTERN.matcher("Ragnar_Ironfist").matches()).isFalse();
		assertThat(PATTERN.matcher("Ragnar-Ironfist").matches()).isFalse();
		assertThat(PATTERN.matcher("Ragnar  Ironfist").matches()).isFalse();
		assertThat(PATTERN.matcher(" Ragnar").matches()).isFalse();
		assertThat(PATTERN.matcher("Ragnar!").matches()).isFalse();
	}
}
