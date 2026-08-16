package com.example.game.quest.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestDefinitionRepository extends JpaRepository<QuestDefinitionEntity, UUID> {

	Optional<QuestDefinitionEntity> findByCode(String code);

	List<QuestDefinitionEntity> findAllByOrderBySortOrderAsc();

	List<QuestDefinitionEntity> findByBoardLocationCodeAndEnabledTrueOrderBySortOrderAsc(String boardLocationCode);
}
