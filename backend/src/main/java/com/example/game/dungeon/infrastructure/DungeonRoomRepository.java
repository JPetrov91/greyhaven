package com.example.game.dungeon.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DungeonRoomRepository extends JpaRepository<DungeonRoomEntity, UUID> {

	List<DungeonRoomEntity> findByDungeonIdOrderBySortOrderAsc(UUID dungeonId);
}
