package com.example.game.chat.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "chat_messages")
public class ChatMessageEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "character_id", nullable = false)
	private UUID characterId;

	@Column(nullable = false, length = 500)
	private String body;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Transient
	private boolean unsaved;

	protected ChatMessageEntity() {
	}

	public ChatMessageEntity(UUID id, UUID characterId, String body, Instant createdAt) {
		this.id = id;
		this.characterId = characterId;
		this.body = body;
		this.createdAt = createdAt;
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

	public String getBody() {
		return body;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
