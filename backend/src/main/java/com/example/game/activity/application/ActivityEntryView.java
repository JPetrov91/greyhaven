package com.example.game.activity.application;

import java.time.Instant;
import java.util.UUID;

import com.example.game.activity.domain.ActivityType;

public record ActivityEntryView(
		UUID id,
		ActivityType type,
		String message,
		Instant createdAt,
		Instant readAt
) {
}
