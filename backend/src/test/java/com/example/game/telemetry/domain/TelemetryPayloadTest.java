package com.example.game.telemetry.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.Test;

class TelemetryPayloadTest {

	@Test
	void acceptsCodedGameplayFields() {
		Map<String, Object> payload = TelemetryPayload.of("amount", 12, "source", "PVE_COMBAT");
		assertThat(payload).containsEntry("amount", 12).containsEntry("source", "PVE_COMBAT");
	}

	@Test
	void rejectsSensitiveKeys() {
		assertThatThrownBy(() -> TelemetryPayload.of("email", "a@b.c"))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("email");
		assertThatThrownBy(() -> TelemetryPayload.of("password", "x"))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> TelemetryPayload.of("session", "abc"))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> TelemetryPayload.of("chat", "hi"))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> TelemetryPayload.of("message", "secret"))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> TelemetryPayload.of("name", "Hero"))
				.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> TelemetryPayload.of("accountId", "id"))
				.isInstanceOf(IllegalArgumentException.class);
	}
}
