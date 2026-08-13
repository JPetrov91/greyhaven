package com.example.game.expedition.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.game.expedition.domain.ExpeditionStatus;

import jakarta.persistence.LockModeType;

public interface ExpeditionRepository extends JpaRepository<ExpeditionEntity, UUID> {

	boolean existsByCharacterIdAndStatus(UUID characterId, ExpeditionStatus status);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			select e
			from ExpeditionEntity e
			where e.characterId = :characterId and e.status = :status
			""")
	Optional<ExpeditionEntity> findWithLockByCharacterIdAndStatus(
			@Param("characterId") UUID characterId,
			@Param("status") ExpeditionStatus status);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select e from ExpeditionEntity e where e.id = :id")
	Optional<ExpeditionEntity> findWithLockById(@Param("id") UUID id);
}
