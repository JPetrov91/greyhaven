package com.example.game.combat.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationEncounterWeightRepository
		extends JpaRepository<LocationEncounterWeightEntity, UUID> {

	List<LocationEncounterWeightEntity> findByLocationId(UUID locationId);
}
