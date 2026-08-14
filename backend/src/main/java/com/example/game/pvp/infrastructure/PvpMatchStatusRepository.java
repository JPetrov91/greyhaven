package com.example.game.pvp.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.game.combat.domain.CombatantSide;

public interface PvpMatchStatusRepository extends JpaRepository<PvpMatchStatusEntity, UUID> {

	List<PvpMatchStatusEntity> findByMatchIdAndTarget(UUID matchId, CombatantSide target);

	@Modifying
	void deleteByMatchId(UUID matchId);
}
