package com.example.game.character.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface CharacterRepository extends JpaRepository<CharacterEntity, UUID> {

	int countByAccountId(UUID accountId);

	List<CharacterEntity> findByAccountIdOrderBySlotIndexAsc(UUID accountId);

	boolean existsByAccountIdAndSlotIndex(UUID accountId, int slotIndex);

	/**
	 * Serializes read-modify-write flows against a character id (inventory grants, starter loadout).
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

	@Query("""
			select c from CharacterEntity c
			where c.id <> :excludedId
			and abs(c.arenaRating - :rating) <= :band
			order by abs(c.arenaRating - :rating) asc, lower(c.name) asc
			""")
	List<CharacterEntity> findArenaOpponents(
			@Param("excludedId") UUID excludedId,
			@Param("rating") int rating,
			@Param("band") int band,
			Pageable pageable);
}
