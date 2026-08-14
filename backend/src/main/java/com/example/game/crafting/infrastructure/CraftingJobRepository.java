package com.example.game.crafting.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.game.crafting.domain.CraftingJobStatus;

import jakarta.persistence.LockModeType;

public interface CraftingJobRepository extends JpaRepository<CraftingJobEntity, UUID> {

	boolean existsByCharacterIdAndStatusIn(UUID characterId, Collection<CraftingJobStatus> statuses);

	Optional<CraftingJobEntity> findFirstByCharacterIdAndStatusInOrderByCreatedAtDesc(
			UUID characterId,
			Collection<CraftingJobStatus> statuses);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select j from CraftingJobEntity j where j.id = :id")
	Optional<CraftingJobEntity> findWithLockById(@Param("id") UUID id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			select j from CraftingJobEntity j
			where j.characterId = :characterId
			  and j.status in :statuses
			order by j.createdAt desc
			""")
	List<CraftingJobEntity> findWithLockByCharacterIdAndStatusIn(
			@Param("characterId") UUID characterId,
			@Param("statuses") Collection<CraftingJobStatus> statuses);
}
