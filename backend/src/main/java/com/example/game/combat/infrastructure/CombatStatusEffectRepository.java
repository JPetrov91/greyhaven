package com.example.game.combat.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.game.combat.domain.CombatantSide;

public interface CombatStatusEffectRepository extends JpaRepository<CombatStatusEffectEntity, UUID> {

	List<CombatStatusEffectEntity> findBySessionId(UUID sessionId);

	@Modifying
	@Query("delete from CombatStatusEffectEntity e where e.sessionId = :sessionId")
	void deleteBySessionId(@Param("sessionId") UUID sessionId);

	List<CombatStatusEffectEntity> findBySessionIdAndTarget(UUID sessionId, CombatantSide target);
}
