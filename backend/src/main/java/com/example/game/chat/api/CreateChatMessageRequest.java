package com.example.game.chat.api;

import com.example.game.chat.domain.ChatRules;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateChatMessageRequest(
		@NotBlank @Size(max = ChatRules.MAX_BODY_LENGTH) String body
) {
}
