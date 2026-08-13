package com.example.game.combat.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.game.combat.domain.CombatSessionStatus;

import jakarta.persistence.LockModeType;

public interface CombatSessionRepository extends JpaRepository<CombatSessionEntity, UUID> {

	/**
	 * Serializes read-modify-write flows against a combat session row.
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from CombatSessionEntity s where s.id = :id")
	Optional<CombatSessionEntity> findWithLockById(@Param("id") UUID id);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			select s from CombatSessionEntity s
			where s.characterId = :characterId and s.status = :status
			""")
	Optional<CombatSessionEntity> findWithLockByCharacterIdAndStatus(
			@Param("characterId") UUID characterId,
			@Param("status") CombatSessionStatus status);

	Optional<CombatSessionEntity> findByCharacterIdAndStatus(UUID characterId, CombatSessionStatus status);

	Optional<CombatSessionEntity> findByCharacterIdAndOutcomeAcknowledgedFalse(UUID characterId);

	boolean existsByCharacterIdAndOutcomeAcknowledgedFalse(UUID characterId);

	Optional<CombatSessionEntity> findByEncounterId(UUID encounterId);

	boolean existsByCharacterIdAndStatus(UUID characterId, CombatSessionStatus status);
}
