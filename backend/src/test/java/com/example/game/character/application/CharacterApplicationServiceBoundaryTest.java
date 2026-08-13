package com.example.game.character.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Guards the modular boundary: character application returns views, not API DTOs.
 */
class CharacterApplicationServiceBoundaryTest {

	@Test
	void applicationMethodsReturnCharacterViewNotApiDto() throws NoSuchMethodException {
		assertThat(CharacterApplicationService.class
				.getMethod("create", UUID.class, String.class)
				.getReturnType())
				.isEqualTo(CharacterView.class);
		assertThat(CharacterApplicationService.class
				.getMethod("current", UUID.class)
				.getReturnType())
				.isEqualTo(CharacterView.class);
	}
}
