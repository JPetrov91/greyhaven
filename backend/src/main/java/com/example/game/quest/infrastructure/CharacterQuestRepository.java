package com.example.game.quest.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.game.quest.domain.QuestStatus;

import jakarta.persistence.LockModeType;

public interface CharacterQuestRepository extends JpaRepository<CharacterQuestEntity, UUID> {

	List<CharacterQuestEntity> findByCharacterId(UUID characterId);

	Optional<CharacterQuestEntity> findByCharacterIdAndQuestId(UUID characterId, UUID questId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select q from CharacterQuestEntity q where q.characterId = :characterId and q.questId = :questId")
	Optional<CharacterQuestEntity> findWithLockByCharacterIdAndQuestId(
			@Param("characterId") UUID characterId,
			@Param("questId") UUID questId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select q from CharacterQuestEntity q where q.characterId = :characterId and q.status in :statuses")
	List<CharacterQuestEntity> findWithLockByCharacterIdAndStatusIn(
			@Param("characterId") UUID characterId,
			@Param("statuses") Collection<QuestStatus> statuses);
}
