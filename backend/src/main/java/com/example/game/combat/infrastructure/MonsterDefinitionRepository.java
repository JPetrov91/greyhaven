package com.example.game.combat.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MonsterDefinitionRepository extends JpaRepository<MonsterDefinitionEntity, UUID> {

	Optional<MonsterDefinitionEntity> findByCode(String code);
}
