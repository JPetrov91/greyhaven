package com.example.game.quest.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterQuestObjectiveRepository extends JpaRepository<CharacterQuestObjectiveEntity, UUID> {

	List<CharacterQuestObjectiveEntity> findByCharacterQuestId(UUID characterQuestId);

	List<CharacterQuestObjectiveEntity> findByCharacterQuestIdIn(Collection<UUID> characterQuestIds);
}
