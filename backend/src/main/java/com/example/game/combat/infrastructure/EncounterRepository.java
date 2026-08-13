package com.example.game.combat.infrastructure;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.game.combat.domain.EncounterStatus;

import jakarta.persistence.LockModeType;

public interface EncounterRepository extends JpaRepository<EncounterEntity, UUID> {

	/**
	 * Serializes read-modify-write flows against an encounter row.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select e from EncounterEntity e where e.id = :id")
	Optional<EncounterEntity> findWithLockById(@Param("id") UUID id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			select e from EncounterEntity e
			where e.characterId = :characterId and e.status in :statuses
			""")
	Optional<EncounterEntity> findWithLockByCharacterIdAndStatusIn(
			@Param("characterId") UUID characterId,
			@Param("statuses") Collection<EncounterStatus> statuses);

	boolean existsByCharacterIdAndStatusIn(UUID characterId, Collection<EncounterStatus> statuses);

	Optional<EncounterEntity> findByCharacterIdAndStatusIn(
			UUID characterId,
			Collection<EncounterStatus> statuses);
}
