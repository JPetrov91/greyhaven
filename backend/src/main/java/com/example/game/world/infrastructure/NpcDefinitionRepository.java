package com.example.game.world.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NpcDefinitionRepository extends JpaRepository<NpcDefinitionEntity, UUID> {

	Optional<NpcDefinitionEntity> findByCode(String code);

	List<NpcDefinitionEntity> findByLocationCodeOrderBySortOrderAsc(String locationCode);

	List<NpcDefinitionEntity> findAllByOrderBySortOrderAsc();
}
