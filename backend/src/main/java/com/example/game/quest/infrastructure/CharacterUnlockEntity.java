package com.example.game.quest.infrastructure;

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
@Table(name = "character_unlocks")
public class CharacterUnlockEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "character_id", nullable = false)
	private UUID characterId;

	@Column(name = "unlock_code", nullable = false, length = 64)
	private String unlockCode;

	@Column(name = "granted_at", nullable = false)
	private Instant grantedAt;

	@Transient
	private boolean unsaved;

	protected CharacterUnlockEntity() {
	}

	public CharacterUnlockEntity(UUID id, UUID characterId, String unlockCode, Instant grantedAt) {
		this.id = id;
		this.characterId = characterId;
		this.unlockCode = unlockCode;
		this.grantedAt = grantedAt;
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

	public String getUnlockCode() {
		return unlockCode;
	}
}
