package com.example.game.expedition.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpeditionRewardItemRepository extends JpaRepository<ExpeditionRewardItemEntity, UUID> {

	List<ExpeditionRewardItemEntity> findByExpeditionId(UUID expeditionId);
}
