package com.example.game.dungeon.infrastructure;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dungeon_definitions")
public class DungeonDefinitionEntity {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true, length = 64)
	private String code;

	@Column(nullable = false, length = 128)
	private String name;

	@Column(name = "entrance_location_id", nullable = false)
	private UUID entranceLocationId;

	protected DungeonDefinitionEntity() {
	}

	public UUID getId() {
		return id;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public UUID getEntranceLocationId() {
		return entranceLocationId;
	}
}
