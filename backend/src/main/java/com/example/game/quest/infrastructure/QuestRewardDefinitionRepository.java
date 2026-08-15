package com.example.game.quest.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestRewardDefinitionRepository extends JpaRepository<QuestRewardDefinitionEntity, UUID> {

	List<QuestRewardDefinitionEntity> findByQuestIdOrderBySortOrderAsc(UUID questId);

	List<QuestRewardDefinitionEntity> findByQuestIdInOrderBySortOrderAsc(Collection<UUID> questIds);
}
