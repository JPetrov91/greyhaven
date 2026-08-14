package com.example.game.pvp.infrastructure;

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
@Table(name = "pvp_match_events")
public class PvpMatchEventEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "match_id", nullable = false)
	private UUID matchId;

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

	protected PvpMatchEventEntity() {
	}

	public PvpMatchEventEntity(
			UUID id,
			UUID matchId,
			int roundNumber,
			int sequenceNumber,
			CombatEventType eventType,
			String message,
			Instant createdAt) {
		this.id = id;
		this.matchId = matchId;
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
}
