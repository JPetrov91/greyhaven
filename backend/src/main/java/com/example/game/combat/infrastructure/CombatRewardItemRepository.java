package com.example.game.combat.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CombatRewardItemRepository extends JpaRepository<CombatRewardItemEntity, UUID> {

	List<CombatRewardItemEntity> findBySessionId(UUID sessionId);
}
