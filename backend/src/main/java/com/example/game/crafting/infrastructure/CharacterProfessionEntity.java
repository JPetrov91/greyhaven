package com.example.game.crafting.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.crafting.domain.Profession;

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
@Table(name = "character_professions")
public class CharacterProfessionEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "character_id", nullable = false)
	private UUID characterId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private Profession profession;

	@Column(nullable = false)
	private int xp;

	@Column(nullable = false)
	private int rank;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Transient
	private boolean unsaved;

	protected CharacterProfessionEntity() {
	}

	public CharacterProfessionEntity(
			UUID id,
			UUID characterId,
			Profession profession,
			int xp,
			int rank,
			Instant updatedAt) {
		this.id = id;
		this.characterId = characterId;
		this.profession = profession;
		this.xp = xp;
		this.rank = rank;
		this.updatedAt = updatedAt;
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

	public Profession getProfession() {
		return profession;
	}

	public int getXp() {
		return xp;
	}

	public int getRank() {
		return rank;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void apply(int rank, int xp, Instant updatedAt) {
		this.rank = rank;
		this.xp = xp;
		this.updatedAt = updatedAt;
	}
}
