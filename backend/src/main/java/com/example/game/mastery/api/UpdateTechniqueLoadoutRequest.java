package com.example.game.mastery.api;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTechniqueLoadoutRequest(
		@NotNull @Size(min = 4, max = 4) List<String> slots
) {
}
