package com.example.game.chat.application;

import org.springframework.http.HttpStatus;

import com.example.game.shared.api.ApiException;

final class ChatErrors {

	private ChatErrors() {
	}

	static ApiException invalidMessage() {
		return new ApiException(
				"CHAT_MESSAGE_INVALID",
				"Chat messages must be plain text of at most 500 characters.",
				HttpStatus.BAD_REQUEST);
	}

	static ApiException rateLimited() {
		return new ApiException(
				"CHAT_RATE_LIMITED",
				"Wait a moment before sending another chat message.",
				HttpStatus.TOO_MANY_REQUESTS);
	}
}
