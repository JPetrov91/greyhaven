package com.example.game.telemetry.application;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.telemetry.domain.TelemetryCategory;
import com.example.game.telemetry.domain.TelemetryEventType;
import com.example.game.telemetry.domain.TelemetryPayload;
import com.example.game.telemetry.infrastructure.GameTelemetryEventEntity;
import com.example.game.telemetry.infrastructure.GameTelemetryEventRepository;

@Service
public class GameTelemetryRecorder {

	private final GameTelemetryEventRepository gameTelemetryEventRepository;
	private final Clock clock;

	public GameTelemetryRecorder(GameTelemetryEventRepository gameTelemetryEventRepository, Clock clock) {
		this.gameTelemetryEventRepository = gameTelemetryEventRepository;
		this.clock = clock;
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void record(
			TelemetryCategory category,
			TelemetryEventType eventType,
			UUID characterId,
			Map<String, Object> payload) {
		if (category == null) {
			throw new IllegalArgumentException("category is required");
		}
		if (eventType == null) {
			throw new IllegalArgumentException("eventType is required");
		}
		if (payload == null) {
			throw new IllegalArgumentException("payload is required");
		}
		for (String key : payload.keySet()) {
			TelemetryPayload.assertAllowed(key);
		}
		gameTelemetryEventRepository.saveAndFlush(new GameTelemetryEventEntity(
				UUID.randomUUID(),
				Instant.now(clock),
				category,
				eventType,
				characterId,
				Map.copyOf(payload)));
	}
}
