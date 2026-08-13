package com.example.game.combat.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CombatEventRepository extends JpaRepository<CombatEventEntity, UUID> {

	List<CombatEventEntity> findBySessionIdOrderByRoundNumberAscSequenceNumberAsc(UUID sessionId);

	List<CombatEventEntity> findBySessionIdAndRoundNumberGreaterThanEqualOrderByRoundNumberAscSequenceNumberAsc(
			UUID sessionId,
			int roundNumber);
}
