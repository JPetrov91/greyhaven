package com.example.game.combat.infrastructure;

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
@Table(name = "character_unique_drops")
public class CharacterUniqueDropEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "character_id", nullable = false)
	private UUID characterId;

	@Column(name = "item_code", nullable = false, length = 64)
	private String itemCode;

	@Column(name = "granted_at", nullable = false)
	private Instant grantedAt;

	@Transient
	private boolean unsaved;

	protected CharacterUniqueDropEntity() {
	}

	public CharacterUniqueDropEntity(UUID id, UUID characterId, String itemCode, Instant grantedAt) {
		this.id = id;
		this.characterId = characterId;
		this.itemCode = itemCode;
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

	public UUID getCharacterId() {
		return characterId;
	}

	public String getItemCode() {
		return itemCode;
	}

	public Instant getGrantedAt() {
		return grantedAt;
	}
}
