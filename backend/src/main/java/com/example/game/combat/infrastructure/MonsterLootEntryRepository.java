package com.example.game.combat.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MonsterLootEntryRepository extends JpaRepository<MonsterLootEntryEntity, UUID> {

	List<MonsterLootEntryEntity> findByMonsterDefinitionId(UUID monsterDefinitionId);
}
