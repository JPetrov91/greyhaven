package com.example.game.pvp.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PvpBattleHistoryRepository extends JpaRepository<PvpBattleHistoryEntity, UUID> {

	List<PvpBattleHistoryEntity> findByCharacterIdOrderByCreatedAtDescIdDesc(UUID characterId, Pageable pageable);

	long countByCharacterId(UUID characterId);
}
