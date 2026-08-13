package com.example.game.world.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<LocationEntity, UUID> {

	Optional<LocationEntity> findByCode(String code);

	boolean existsByCode(String code);
}
