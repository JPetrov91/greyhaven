package com.example.game.character.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CharacterRepository extends JpaRepository<CharacterEntity, UUID> {

	boolean existsByAccountId(UUID accountId);

	Optional<CharacterEntity> findByAccountId(UUID accountId);

	@Query("select count(c) > 0 from CharacterEntity c where lower(c.name) = lower(:name)")
	boolean existsByNameIgnoreCase(@Param("name") String name);
}
