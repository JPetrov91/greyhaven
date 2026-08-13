package com.example.game.shared.api;

import java.time.Instant;

public record ApiError(
		String code,
		String message,
		Instant timestamp
) {
}
