package com.example.game.combat.infrastructure;

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
@Table(name = "location_encounter_weights")
public class LocationEncounterWeightEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "location_id", nullable = false)
	private UUID locationId;

	@Column(name = "monster_definition_id")
	private UUID monsterDefinitionId;

	@Column(nullable = false)
	private int weight;

	@Transient
	private boolean unsaved;

	protected LocationEncounterWeightEntity() {
	}

	public LocationEncounterWeightEntity(
			UUID id,
			UUID locationId,
			UUID monsterDefinitionId,
			int weight) {
		this.id = id;
		this.locationId = locationId;
		this.monsterDefinitionId = monsterDefinitionId;
		this.weight = weight;
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

	public UUID getLocationId() {
		return locationId;
	}

	public UUID getMonsterDefinitionId() {
		return monsterDefinitionId;
	}

	public int getWeight() {
		return weight;
	}
}
