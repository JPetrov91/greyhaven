package com.example.game.pvp.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PvpMatchEventRepository extends JpaRepository<PvpMatchEventEntity, UUID> {

	List<PvpMatchEventEntity> findByMatchIdOrderByRoundNumberAscSequenceNumberAsc(UUID matchId);

	int countByMatchId(UUID matchId);
}
