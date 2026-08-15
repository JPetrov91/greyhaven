package com.example.game.quest.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterUnlockRepository extends JpaRepository<CharacterUnlockEntity, UUID> {

	List<CharacterUnlockEntity> findByCharacterIdOrderByUnlockCodeAsc(UUID characterId);

	boolean existsByCharacterIdAndUnlockCode(UUID characterId, String unlockCode);
}
