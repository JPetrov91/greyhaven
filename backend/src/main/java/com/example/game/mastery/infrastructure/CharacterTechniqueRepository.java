package com.example.game.mastery.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterTechniqueRepository extends JpaRepository<CharacterTechniqueEntity, UUID> {

	List<CharacterTechniqueEntity> findByCharacterId(UUID characterId);

	boolean existsByCharacterIdAndTechniqueCode(UUID characterId, String techniqueCode);
}
