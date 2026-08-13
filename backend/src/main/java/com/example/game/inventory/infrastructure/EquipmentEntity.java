package com.example.game.inventory.infrastructure;

import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.inventory.domain.EquipmentSlot;

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
@Table(name = "equipment")
public class EquipmentEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "character_id", nullable = false)
	private UUID characterId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private EquipmentSlot slot;

	@Column(name = "item_instance_id", nullable = false, unique = true)
	private UUID itemInstanceId;

	@Transient
	private boolean unsaved;

	protected EquipmentEntity() {
	}

	public EquipmentEntity(UUID id, UUID characterId, EquipmentSlot slot, UUID itemInstanceId) {
		this.id = id;
		this.characterId = characterId;
		this.slot = slot;
		this.itemInstanceId = itemInstanceId;
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

	public EquipmentSlot getSlot() {
		return slot;
	}

	public UUID getItemInstanceId() {
		return itemInstanceId;
	}

	public void equip(UUID itemInstanceId) {
		this.itemInstanceId = itemInstanceId;
	}
}
