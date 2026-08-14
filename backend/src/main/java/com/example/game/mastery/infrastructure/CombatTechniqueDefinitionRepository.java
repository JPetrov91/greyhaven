package com.example.game.mastery.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CombatTechniqueDefinitionRepository
		extends JpaRepository<CombatTechniqueDefinitionEntity, String> {
}
