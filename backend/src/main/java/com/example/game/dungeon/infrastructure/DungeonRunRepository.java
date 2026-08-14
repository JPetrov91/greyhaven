package com.example.game.dungeon.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.game.dungeon.domain.DungeonRunStatus;

import jakarta.persistence.LockModeType;

public interface DungeonRunRepository extends JpaRepository<DungeonRunEntity, UUID> {

	Optional<DungeonRunEntity> findByCharacterIdAndStatus(UUID characterId, DungeonRunStatus status);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select r from DungeonRunEntity r where r.characterId = :characterId and r.status = :status")
	Optional<DungeonRunEntity> findWithLockByCharacterIdAndStatus(
			@Param("characterId") UUID characterId,
			@Param("status") DungeonRunStatus status);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select r from DungeonRunEntity r where r.id = :id")
	Optional<DungeonRunEntity> findWithLockById(@Param("id") UUID id);

	boolean existsByCharacterIdAndUniqueRewardGrantedTrue(UUID characterId);
}
