package com.example.game.character.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface CharacterRepository extends JpaRepository<CharacterEntity, UUID> {

	boolean existsByAccountId(UUID accountId);

	Optional<CharacterEntity> findByAccountId(UUID accountId);

	/**
	 * Serializes read-modify-write flows against a character. Without the row lock two concurrent
	 * requests can both read the same state and the second write silently discards the first.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	Optional<CharacterEntity> findWithLockByAccountId(UUID accountId);

	/**
	 * Same serialization as {@link #findWithLockByAccountId} for callers that already have the
	 * character id (inventory grants, starter loadout equip).
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select c from CharacterEntity c where c.id = :id")
	Optional<CharacterEntity> findWithLockById(@Param("id") UUID id);

	@Query("select count(c) > 0 from CharacterEntity c where lower(c.name) = lower(:name)")
	boolean existsByNameIgnoreCase(@Param("name") String name);

	List<CharacterEntity> findByCurrentLocationIdAndIdNotOrderByNameAsc(
			UUID currentLocationId,
			UUID excludedCharacterId,
			Limit limit);
}
