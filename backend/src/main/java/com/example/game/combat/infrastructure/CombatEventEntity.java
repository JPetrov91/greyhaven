package com.example.game.combat.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.combat.domain.CombatEventType;

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
@Table(name = "combat_events")
public class CombatEventEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "session_id", nullable = false)
	private UUID sessionId;

	@Column(name = "round_number", nullable = false)
	private int roundNumber;

	@Column(name = "sequence_number", nullable = false)
	private int sequenceNumber;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, length = 64)
	private CombatEventType eventType;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String message;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Transient
	private boolean unsaved;

	protected CombatEventEntity() {
	}

	public CombatEventEntity(
			UUID id,
			UUID sessionId,
			int roundNumber,
			int sequenceNumber,
			CombatEventType eventType,
			String message,
			Instant createdAt) {
		this.id = id;
		this.sessionId = sessionId;
		this.roundNumber = roundNumber;
		this.sequenceNumber = sequenceNumber;
		this.eventType = eventType;
		this.message = message;
		this.createdAt = createdAt;
		this.unsaved = true;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public boolean isNew() {
		return unsaved;
	}

	@PostPersist
	@PostLoad
	void markStored() {
		this.unsaved = false;
	}

	public UUID getSessionId() {
		return sessionId;
	}

	public int getRoundNumber() {
		return roundNumber;
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public CombatEventType getEventType() {
		return eventType;
	}

	public String getMessage() {
		return message;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
