package com.example.game.telemetry.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Builds JSON-safe telemetry maps. Rejects keys that could carry account PII or chat.
 */
public final class TelemetryPayload {

	private static final Set<String> FORBIDDEN_KEYS = Set.of(
			"email",
			"password",
			"passwordhash",
			"token",
			"session",
			"sessionid",
			"csrf",
			"chat",
			"message",
			"name",
			"charactername",
			"accountid",
			"account_id");

	private TelemetryPayload() {
	}

	public static Map<String, Object> of(Object... keysAndValues) {
		if (keysAndValues.length % 2 != 0) {
			throw new IllegalArgumentException("keys and values must be paired");
		}
		Map<String, Object> payload = new LinkedHashMap<>();
		for (int i = 0; i < keysAndValues.length; i += 2) {
			if (!(keysAndValues[i] instanceof String key)) {
				throw new IllegalArgumentException("payload keys must be strings");
			}
			put(payload, key, keysAndValues[i + 1]);
		}
		return Collections.unmodifiableMap(payload);
	}

	public static void put(Map<String, Object> payload, String key, Object value) {
		assertAllowed(key);
		if (value instanceof Map<?, ?> nested) {
			for (Object nestedKey : nested.keySet()) {
				if (nestedKey instanceof String nestedName) {
					assertAllowed(nestedName);
				}
			}
		}
		payload.put(key, value);
	}

	public static void assertAllowed(String key) {
		if (key == null || key.isBlank()) {
			throw new IllegalArgumentException("payload key is required");
		}
		String normalized = key.toLowerCase(Locale.ROOT).replace("-", "").replace("_", "");
		if (FORBIDDEN_KEYS.contains(normalized)) {
			throw new IllegalArgumentException("telemetry payload must not include '" + key + "'");
		}
	}
}
