package com.example.game.quest.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterQuestTrackRepository extends JpaRepository<CharacterQuestTrackEntity, UUID> {

	List<CharacterQuestTrackEntity> findByCharacterIdOrderBySortOrderAsc(UUID characterId);

	Optional<CharacterQuestTrackEntity> findByCharacterIdAndQuestId(UUID characterId, UUID questId);

	long countByCharacterId(UUID characterId);
}
