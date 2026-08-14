package com.example.game.dungeon.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.dungeon.domain.DungeonRunStatus;

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
@Table(name = "dungeon_runs")
public class DungeonRunEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "character_id", nullable = false)
	private UUID characterId;

	@Column(name = "dungeon_id", nullable = false)
	private UUID dungeonId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private DungeonRunStatus status;

	@Column(nullable = false)
	private boolean paused;

	@Column(name = "current_room_code", nullable = false, length = 64)
	private String currentRoomCode;

	@Column(name = "chosen_branch", length = 32)
	private String chosenBranch;

	@Column(name = "unique_reward_granted", nullable = false)
	private boolean uniqueRewardGranted;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Transient
	private boolean unsaved;

	protected DungeonRunEntity() {
	}

	public DungeonRunEntity(
			UUID id,
			UUID characterId,
			UUID dungeonId,
			String currentRoomCode,
			Instant now) {
		this.id = id;
		this.characterId = characterId;
		this.dungeonId = dungeonId;
		this.status = DungeonRunStatus.ACTIVE;
		this.paused = false;
		this.currentRoomCode = currentRoomCode;
		this.createdAt = now;
		this.updatedAt = now;
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

	public UUID getDungeonId() {
		return dungeonId;
	}

	public DungeonRunStatus getStatus() {
		return status;
	}

	public boolean isPaused() {
		return paused;
	}

	public String getCurrentRoomCode() {
		return currentRoomCode;
	}

	public String getChosenBranch() {
		return chosenBranch;
	}

	public boolean isUniqueRewardGranted() {
		return uniqueRewardGranted;
	}

	public void pause(Instant now) {
		this.paused = true;
		this.updatedAt = now;
	}

	public void resume(Instant now) {
		this.paused = false;
		this.updatedAt = now;
	}

	public void moveTo(String roomCode, Instant now) {
		this.currentRoomCode = roomCode;
		this.paused = false;
		this.updatedAt = now;
	}

	public void chooseBranch(String branch, Instant now) {
		this.chosenBranch = branch;
		this.updatedAt = now;
	}

	public void complete(Instant now) {
		this.status = DungeonRunStatus.COMPLETED;
		this.paused = true;
		this.updatedAt = now;
	}

	public void abandon(Instant now) {
		this.status = DungeonRunStatus.ABANDONED;
		this.paused = true;
		this.updatedAt = now;
	}

	public void grantUniqueReward() {
		this.uniqueRewardGranted = true;
	}
}
