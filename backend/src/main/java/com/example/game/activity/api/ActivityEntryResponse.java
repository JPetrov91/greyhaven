package com.example.game.activity.api;

import java.time.Instant;
import java.util.UUID;

import com.example.game.activity.domain.ActivityType;

public record ActivityEntryResponse(
		UUID id,
		ActivityType type,
		String message,
		Instant createdAt,
		Instant readAt
) {
}
