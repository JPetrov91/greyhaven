package com.example.game.chat.api;

import java.time.Instant;
import java.util.UUID;

import com.example.game.chat.application.ChatMessageView;

public record ChatMessageResponse(
		UUID id,
		UUID characterId,
		String characterName,
		String body,
		Instant createdAt
) {

	static ChatMessageResponse from(ChatMessageView view) {
		return new ChatMessageResponse(
				view.id(),
				view.characterId(),
				view.characterName(),
				view.body(),
				view.createdAt());
	}
}
