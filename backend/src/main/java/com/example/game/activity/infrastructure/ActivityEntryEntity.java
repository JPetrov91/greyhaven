package com.example.game.activity.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.activity.domain.ActivityType;

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
@Table(name = "activity_entries")
public class ActivityEntryEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "character_id", nullable = false)
	private UUID characterId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 64)
	private ActivityType type;

	@Column(nullable = false, length = 512)
	private String message;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "read_at")
	private Instant readAt;

	@Transient
	private boolean unsaved;

	protected ActivityEntryEntity() {
	}

	public ActivityEntryEntity(
			UUID id,
			UUID characterId,
			ActivityType type,
			String message,
			Instant createdAt) {
		this.id = id;
		this.characterId = characterId;
		this.type = type;
		this.message = message;
		this.createdAt = createdAt;
		this.readAt = null;
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

	public UUID getCharacterId() {
		return characterId;
	}

	public ActivityType getType() {
		return type;
	}

	public String getMessage() {
		return message;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getReadAt() {
		return readAt;
	}
}
