package com.example.game.crafting.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SalvageOutputRepository extends JpaRepository<SalvageOutputEntity, UUID> {

	List<SalvageOutputEntity> findBySourceItemDefinitionId(UUID sourceItemDefinitionId);
}
