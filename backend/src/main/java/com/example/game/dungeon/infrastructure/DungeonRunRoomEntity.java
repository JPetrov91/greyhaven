package com.example.game.dungeon.infrastructure;

import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.dungeon.domain.DungeonRoomState;

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
@Table(name = "dungeon_run_rooms")
public class DungeonRunRoomEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "run_id", nullable = false)
	private UUID runId;

	@Column(name = "room_code", nullable = false, length = 64)
	private String roomCode;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private DungeonRoomState state;

	@Transient
	private boolean unsaved;

	protected DungeonRunRoomEntity() {
	}

	public DungeonRunRoomEntity(UUID id, UUID runId, String roomCode, DungeonRoomState state) {
		this.id = id;
		this.runId = runId;
		this.roomCode = roomCode;
		this.state = state;
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

	public String getRoomCode() {
		return roomCode;
	}

	public DungeonRoomState getState() {
		return state;
	}

	public void setState(DungeonRoomState state) {
		this.state = state;
	}
}
