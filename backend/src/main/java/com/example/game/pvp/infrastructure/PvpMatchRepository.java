package com.example.game.pvp.infrastructure;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.game.pvp.domain.PvpMatchKind;
import com.example.game.pvp.domain.PvpMatchStatus;

import jakarta.persistence.LockModeType;

public interface PvpMatchRepository extends JpaRepository<PvpMatchEntity, UUID> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select m from PvpMatchEntity m where m.id = :id")
	Optional<PvpMatchEntity> findWithLockById(@Param("id") UUID id);

	Optional<PvpMatchEntity> findByAttackerIdAndMatchKindAndStatus(
			UUID attackerId,
			PvpMatchKind matchKind,
			PvpMatchStatus status);

	Optional<PvpMatchEntity> findByAttackerIdAndMatchKindAndOutcomeAcknowledgedFalse(
			UUID attackerId,
			PvpMatchKind matchKind);

	boolean existsByAttackerIdAndStatus(UUID attackerId, PvpMatchStatus status);

	boolean existsByDefenderIdAndStatus(UUID defenderId, PvpMatchStatus status);

	boolean existsByAttackerIdAndMatchKindAndStatusIn(
			UUID attackerId,
			PvpMatchKind matchKind,
			List<PvpMatchStatus> statuses);

	boolean existsByDefenderIdAndMatchKindAndStatusIn(
			UUID defenderId,
			PvpMatchKind matchKind,
			List<PvpMatchStatus> statuses);

	Optional<PvpMatchEntity> findFirstByAttackerIdAndMatchKindAndStatusIn(
			UUID attackerId,
			PvpMatchKind matchKind,
			List<PvpMatchStatus> statuses);

	Optional<PvpMatchEntity> findFirstByDefenderIdAndMatchKindAndStatusIn(
			UUID defenderId,
			PvpMatchKind matchKind,
			List<PvpMatchStatus> statuses);

	@Query("""
			select count(m) from PvpMatchEntity m
			where m.matchKind = com.example.game.pvp.domain.PvpMatchKind.ARENA
			and m.attackerId = :attackerId
			and m.defenderId = :defenderId
			and m.status in (
				com.example.game.pvp.domain.PvpMatchStatus.ATTACKER_WON,
				com.example.game.pvp.domain.PvpMatchStatus.DEFENDER_WON,
				com.example.game.pvp.domain.PvpMatchStatus.ATTACKER_FORFEIT)
			and m.createdAt >= :since
			""")
	long countCompletedArenaBetweenSince(
			@Param("attackerId") UUID attackerId,
			@Param("defenderId") UUID defenderId,
			@Param("since") Instant since);

	long countByAttackerIdAndMatchKindAndCreatedAtGreaterThanEqual(
			UUID attackerId,
			PvpMatchKind matchKind,
			Instant createdAt);

	@Query("""
			select m from PvpMatchEntity m
			where m.matchKind = com.example.game.pvp.domain.PvpMatchKind.DUEL
			and m.status in (
				com.example.game.pvp.domain.PvpMatchStatus.PENDING,
				com.example.game.pvp.domain.PvpMatchStatus.ACTIVE)
			and (m.attackerId = :characterId or m.defenderId = :characterId)
			""")
	Optional<PvpMatchEntity> findOpenDuelFor(@Param("characterId") UUID characterId);
}
