package com.example.game.dungeon.infrastructure;

import java.util.UUID;

import com.example.game.dungeon.domain.DungeonRoomKind;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dungeon_rooms")
public class DungeonRoomEntity {

	@Id
	private UUID id;

	@Column(name = "dungeon_id", nullable = false)
	private UUID dungeonId;

	@Column(nullable = false, length = 64)
	private String code;

	@Column(nullable = false, length = 128)
	private String name;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "room_kind", nullable = false, length = 16)
	private DungeonRoomKind roomKind;

	@Column(name = "monster_definition_id")
	private UUID monsterDefinitionId;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	protected DungeonRoomEntity() {
	}

	public UUID getId() {
		return id;
	}

	public UUID getDungeonId() {
		return dungeonId;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public DungeonRoomKind getRoomKind() {
		return roomKind;
	}

	public UUID getMonsterDefinitionId() {
		return monsterDefinitionId;
	}

	public int getSortOrder() {
		return sortOrder;
	}
}
