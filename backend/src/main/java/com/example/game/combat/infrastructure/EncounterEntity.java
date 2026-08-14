package com.example.game.combat.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.combat.domain.EncounterStatus;

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
@Table(name = "encounters")
public class EncounterEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "character_id", nullable = false)
	private UUID characterId;

	@Column(name = "location_id", nullable = false)
	private UUID locationId;

	@Column(name = "monster_definition_id")
	private UUID monsterDefinitionId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private EncounterStatus status;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "dungeon_run_id")
	private UUID dungeonRunId;

	@Column(name = "dungeon_room_code", length = 64)
	private String dungeonRoomCode;

	@Column(name = "dungeon_optional", nullable = false)
	private boolean dungeonOptional;

	@Transient
	private boolean unsaved;

	protected EncounterEntity() {
	}

	public EncounterEntity(
			UUID id,
			UUID characterId,
			UUID locationId,
			UUID monsterDefinitionId,
			EncounterStatus status,
			Instant createdAt,
			Instant updatedAt) {
		this.id = id;
		this.characterId = characterId;
		this.locationId = locationId;
		this.monsterDefinitionId = monsterDefinitionId;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.unsaved = true;
	}

	public void bindDungeon(UUID dungeonRunId, String dungeonRoomCode, boolean optional) {
		this.dungeonRunId = dungeonRunId;
		this.dungeonRoomCode = dungeonRoomCode;
		this.dungeonOptional = optional;
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

	public UUID getLocationId() {
		return locationId;
	}

	public UUID getMonsterDefinitionId() {
		return monsterDefinitionId;
	}

	public EncounterStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public UUID getDungeonRunId() {
		return dungeonRunId;
	}

	public String getDungeonRoomCode() {
		return dungeonRoomCode;
	}

	public boolean isDungeonOptional() {
		return dungeonOptional;
	}

	public boolean isDungeonEncounter() {
		return dungeonRunId != null;
	}

	public void markCombatStarted(Instant updatedAt) {
		if (status != EncounterStatus.AVAILABLE) {
			throw new IllegalStateException("encounter must be AVAILABLE to start combat");
		}
		if (monsterDefinitionId == null) {
			throw new IllegalStateException("encounter requires a monster to start combat");
		}
		this.status = EncounterStatus.COMBAT_STARTED;
		this.updatedAt = updatedAt;
	}

	public void resolve(Instant updatedAt) {
		if (status == EncounterStatus.RESOLVED || status == EncounterStatus.EXPIRED) {
			throw new IllegalStateException("encounter is already closed");
		}
		this.status = EncounterStatus.RESOLVED;
		this.updatedAt = updatedAt;
	}

	public void expire(Instant updatedAt) {
		if (status != EncounterStatus.AVAILABLE) {
			throw new IllegalStateException("only an available encounter can be deferred");
		}
		this.status = EncounterStatus.EXPIRED;
		this.updatedAt = updatedAt;
	}
}
