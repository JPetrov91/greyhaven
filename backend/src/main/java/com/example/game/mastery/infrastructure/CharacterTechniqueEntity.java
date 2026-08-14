package com.example.game.mastery.infrastructure;

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
@Table(name = "character_techniques")
public class CharacterTechniqueEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "character_id", nullable = false)
	private UUID characterId;

	@Column(name = "technique_code", nullable = false, length = 64)
	private String techniqueCode;

	@Column(name = "unlocked_at", nullable = false)
	private Instant unlockedAt;

	@Transient
	private boolean unsaved;

	protected CharacterTechniqueEntity() {
	}

	public CharacterTechniqueEntity(UUID id, UUID characterId, String techniqueCode, Instant unlockedAt) {
		this.id = id;
		this.characterId = characterId;
		this.techniqueCode = techniqueCode;
		this.unlockedAt = unlockedAt;
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

	public UUID getCharacterId() {
		return characterId;
	}

	public String getTechniqueCode() {
		return techniqueCode;
	}

	public Instant getUnlockedAt() {
		return unlockedAt;
	}
}
