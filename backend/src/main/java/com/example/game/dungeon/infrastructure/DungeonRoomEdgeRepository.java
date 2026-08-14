package com.example.game.dungeon.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DungeonRoomEdgeRepository extends JpaRepository<DungeonRoomEdgeEntity, UUID> {

	List<DungeonRoomEdgeEntity> findByFromRoomId(UUID fromRoomId);

	List<DungeonRoomEdgeEntity> findByFromRoomIdIn(List<UUID> fromRoomIds);
}
