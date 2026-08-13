package com.example.game.chat.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.regex.Pattern;

/**
 * Pure global-chat rules kept out of persistence and HTTP types.
 */
public final class ChatRules {

	public static final int MAX_BODY_LENGTH = 500;
	public static final int HISTORY_LIMIT = 100;
	public static final Duration MIN_INTERVAL_BETWEEN_MESSAGES = Duration.ofSeconds(2);

	private static final Pattern MARKUP = Pattern.compile("(?i)<\\s*[a-z!?/]");

	private ChatRules() {
	}

	public static String normalizeBody(String raw) {
		if (raw == null) {
			return "";
		}
		return raw.replace("\r\n", "\n").replace('\r', '\n').trim();
	}

	public static boolean isAcceptableBody(String normalizedBody) {
		return normalizedBody != null
				&& !normalizedBody.isEmpty()
				&& normalizedBody.length() <= MAX_BODY_LENGTH
				&& isPlainText(normalizedBody);
	}

	public static boolean isPlainText(String body) {
		if (body == null) {
			return false;
		}
		for (int index = 0; index < body.length(); index++) {
			char character = body.charAt(index);
			if (Character.isISOControl(character) && character != '\n' && character != '\t') {
				return false;
			}
		}
		return !MARKUP.matcher(body).find();
	}

	public static boolean isRateLimited(Instant lastSentAt, Instant now) {
		if (now == null) {
			throw new IllegalArgumentException("now is required");
		}
		if (lastSentAt == null) {
			return false;
		}
		return now.isBefore(lastSentAt.plus(MIN_INTERVAL_BETWEEN_MESSAGES));
	}
}
