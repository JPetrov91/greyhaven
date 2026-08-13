package com.example.game.world.infrastructure;

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
@Table(name = "location_connections")
public class LocationConnectionEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "from_location_id", nullable = false)
	private UUID fromLocationId;

	@Column(name = "to_location_id", nullable = false)
	private UUID toLocationId;

	@Transient
	private boolean unsaved;

	protected LocationConnectionEntity() {
	}

	public LocationConnectionEntity(UUID id, UUID fromLocationId, UUID toLocationId) {
		this.id = id;
		this.fromLocationId = fromLocationId;
		this.toLocationId = toLocationId;
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

	public UUID getFromLocationId() {
		return fromLocationId;
	}

	public UUID getToLocationId() {
		return toLocationId;
	}
}
