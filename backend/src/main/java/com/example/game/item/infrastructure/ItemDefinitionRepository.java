package com.example.game.item.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemDefinitionRepository extends JpaRepository<ItemDefinitionEntity, UUID> {

	Optional<ItemDefinitionEntity> findByCode(String code);

	boolean existsByCode(String code);
}
