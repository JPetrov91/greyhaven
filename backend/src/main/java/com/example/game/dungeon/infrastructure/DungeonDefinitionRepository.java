package com.example.game.dungeon.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DungeonDefinitionRepository extends JpaRepository<DungeonDefinitionEntity, UUID> {

	Optional<DungeonDefinitionEntity> findByCode(String code);

	Optional<DungeonDefinitionEntity> findByEntranceLocationId(UUID entranceLocationId);
}
