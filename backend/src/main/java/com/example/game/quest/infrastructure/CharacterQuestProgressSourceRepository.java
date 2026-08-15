package com.example.game.quest.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.game.quest.domain.QuestProgressSourceKind;

public interface CharacterQuestProgressSourceRepository extends JpaRepository<CharacterQuestProgressSourceEntity, UUID> {

	boolean existsByCharacterIdAndSourceKindAndSourceId(
			UUID characterId,
			QuestProgressSourceKind sourceKind,
			String sourceId);
}
