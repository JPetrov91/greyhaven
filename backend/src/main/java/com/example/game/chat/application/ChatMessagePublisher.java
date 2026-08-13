package com.example.game.chat.application;

/**
 * Delivers a persisted chat message to live listeners after the write commits.
 */
public interface ChatMessagePublisher {

	void publish(ChatMessageView message);
}
