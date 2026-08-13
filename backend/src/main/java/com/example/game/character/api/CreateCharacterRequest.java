package com.example.game.character.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateCharacterRequest(
		@NotBlank
		@Size(min = 3, max = 24)
		@Pattern(
				regexp = "^[A-Za-z][A-Za-z0-9_-]*$",
				message = "must start with a letter and contain only letters, digits, underscores, or hyphens")
		String name
) {
}
