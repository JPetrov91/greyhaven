package com.example.game.character.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AllocateAttributesRequest(
		@NotNull @Min(0) @Max(40) Integer strength,
		@NotNull @Min(0) @Max(40) Integer agility,
		@NotNull @Min(0) @Max(40) Integer endurance,
		@NotNull @Min(0) @Max(40) Integer perception
) {
}
