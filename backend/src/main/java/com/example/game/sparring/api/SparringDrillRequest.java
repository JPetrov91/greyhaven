package com.example.game.sparring.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SparringDrillRequest(
		@Min(1) @Max(10) int botLevel
) {
}
