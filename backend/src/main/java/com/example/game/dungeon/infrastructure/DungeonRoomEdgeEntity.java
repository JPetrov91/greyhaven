package com.example.game.dungeon.infrastructure;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dungeon_room_edges")
public class DungeonRoomEdgeEntity {

	@Id
	private UUID id;

	@Column(name = "from_room_id", nullable = false)
	private UUID fromRoomId;

	@Column(name = "to_room_id", nullable = false)
	private UUID toRoomId;

	@Column(name = "edge_code", nullable = false, length = 32)
	private String edgeCode;

	@Column(name = "skip_room_code", length = 64)
	private String skipRoomCode;

	protected DungeonRoomEdgeEntity() {
	}

	public UUID getId() {
		return id;
	}

	public UUID getFromRoomId() {
		return fromRoomId;
	}

	public UUID getToRoomId() {
		return toRoomId;
	}

	public String getEdgeCode() {
		return edgeCode;
	}

	public String getSkipRoomCode() {
		return skipRoomCode;
	}
}
