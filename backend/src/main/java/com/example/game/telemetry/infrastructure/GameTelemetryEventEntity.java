package com.example.game.telemetry.infrastructure;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

import com.example.game.telemetry.domain.TelemetryCategory;
import com.example.game.telemetry.domain.TelemetryEventType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "game_telemetry_events")
public class GameTelemetryEventEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "occurred_at", nullable = false)
	private Instant occurredAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private TelemetryCategory category;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, length = 64)
	private TelemetryEventType eventType;

	@Column(name = "character_id")
	private UUID characterId;

	@JdbcTypeCode(SqlTypes.JSON)
	@Column(nullable = false, columnDefinition = "jsonb")
	private Map<String, Object> payload;

	@Transient
	private boolean unsaved;

	protected GameTelemetryEventEntity() {
	}

	public GameTelemetryEventEntity(
			UUID id,
			Instant occurredAt,
			TelemetryCategory category,
			TelemetryEventType eventType,
			UUID characterId,
			Map<String, Object> payload) {
		this.id = id;
		this.occurredAt = occurredAt;
		this.category = category;
		this.eventType = eventType;
		this.characterId = characterId;
		this.payload = payload;
		this.unsaved = true;
	}

	@PostPersist
	@PostLoad
	void markStored() {
		this.unsaved = false;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public boolean isNew() {
		return unsaved;
	}

	public Instant getOccurredAt() {
		return occurredAt;
	}

	public TelemetryCategory getCategory() {
		return category;
	}

	public TelemetryEventType getEventType() {
		return eventType;
	}

	public UUID getCharacterId() {
		return characterId;
	}

	public Map<String, Object> getPayload() {
		return payload;
	}
}
