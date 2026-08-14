package com.example.game.item.infrastructure;

import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.item.domain.AffixStat;

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
@Table(name = "item_definition_modifiers")
public class ItemDefinitionModifierEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "item_definition_id", nullable = false)
	private UUID itemDefinitionId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private AffixStat stat;

	@Column(nullable = false)
	private int magnitude;

	@Transient
	private boolean unsaved;

	protected ItemDefinitionModifierEntity() {
	}

	public ItemDefinitionModifierEntity(UUID id, UUID itemDefinitionId, AffixStat stat, int magnitude) {
		this.id = id;
		this.itemDefinitionId = itemDefinitionId;
		this.stat = stat;
		this.magnitude = magnitude;
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

	public UUID getItemDefinitionId() {
		return itemDefinitionId;
	}

	public AffixStat getStat() {
		return stat;
	}

	public int getMagnitude() {
		return magnitude;
	}
}
