package com.example.game.mastery.infrastructure;

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
@Table(name = "technique_loadout_slots")
public class TechniqueLoadoutSlotEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "character_id", nullable = false)
	private UUID characterId;

	@Column(name = "slot_index", nullable = false)
	private int slotIndex;

	@Column(name = "technique_code", length = 64)
	private String techniqueCode;

	@Transient
	private boolean unsaved;

	protected TechniqueLoadoutSlotEntity() {
	}

	public TechniqueLoadoutSlotEntity(UUID id, UUID characterId, int slotIndex, String techniqueCode) {
		this.id = id;
		this.characterId = characterId;
		this.slotIndex = slotIndex;
		this.techniqueCode = techniqueCode;
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

	public int getSlotIndex() {
		return slotIndex;
	}

	public String getTechniqueCode() {
		return techniqueCode;
	}

	public void assign(String techniqueCode) {
		this.techniqueCode = techniqueCode;
	}
}
