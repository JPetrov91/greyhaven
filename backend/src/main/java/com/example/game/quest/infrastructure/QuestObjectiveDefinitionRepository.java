package com.example.game.quest.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestObjectiveDefinitionRepository extends JpaRepository<QuestObjectiveDefinitionEntity, UUID> {

	List<QuestObjectiveDefinitionEntity> findByQuestIdOrderBySortOrderAsc(UUID questId);

	List<QuestObjectiveDefinitionEntity> findByQuestIdInOrderBySortOrderAsc(Collection<UUID> questIds);
}
