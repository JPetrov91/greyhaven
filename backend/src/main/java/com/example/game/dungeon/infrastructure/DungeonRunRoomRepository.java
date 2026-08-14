package com.example.game.dungeon.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DungeonRunRoomRepository extends JpaRepository<DungeonRunRoomEntity, UUID> {

	List<DungeonRunRoomEntity> findByRunId(UUID runId);

	Optional<DungeonRunRoomEntity> findByRunIdAndRoomCode(UUID runId, String roomCode);
}
