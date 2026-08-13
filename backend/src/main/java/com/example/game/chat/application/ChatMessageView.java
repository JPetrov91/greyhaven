package com.example.game.chat.application;

import java.time.Instant;
import java.util.UUID;

public record ChatMessageView(
		UUID id,
		UUID characterId,
		String characterName,
		String body,
		Instant createdAt
) {
}
