package com.example.game.telemetry.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.game.telemetry.domain.TelemetryEventType;

public interface GameTelemetryEventRepository extends JpaRepository<GameTelemetryEventEntity, UUID> {

	List<GameTelemetryEventEntity> findByCharacterIdOrderByOccurredAtAsc(UUID characterId);

	long countByEventType(TelemetryEventType eventType);
}
