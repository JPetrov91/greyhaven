package com.example.game.crafting.infrastructure;

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
@Table(name = "salvage_outputs")
public class SalvageOutputEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "source_item_definition_id", nullable = false)
	private UUID sourceItemDefinitionId;

	@Column(name = "result_item_definition_id", nullable = false)
	private UUID resultItemDefinitionId;

	@Column(name = "base_quantity", nullable = false)
	private int baseQuantity;

	@Transient
	private boolean unsaved;

	protected SalvageOutputEntity() {
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

	public UUID getSourceItemDefinitionId() {
		return sourceItemDefinitionId;
	}

	public UUID getResultItemDefinitionId() {
		return resultItemDefinitionId;
	}

	public int getBaseQuantity() {
		return baseQuantity;
	}
}
