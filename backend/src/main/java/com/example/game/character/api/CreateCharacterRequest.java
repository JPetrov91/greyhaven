package com.example.game.character.api;

import com.example.game.character.domain.CharacterGender;
import com.example.game.character.domain.CharacterNameRules;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCharacterRequest(
		@NotBlank
		@Size(min = 3, max = 24)
		@Pattern(regexp = CharacterNameRules.PATTERN, message = CharacterNameRules.MESSAGE)
		String name,
		CharacterGender gender,
		@Size(max = 64)
		String avatarCode,
		@Min(0)
		@Max(2)
		Integer slotIndex
) {
}
