package com.example.game.world.api;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record MoveRequest(
		@NotNull UUID destinationLocationId
) {
}
