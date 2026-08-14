package com.example.game.mastery.application;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.mastery.domain.CombatTechniqueCatalog;
import com.example.game.mastery.domain.CombatTechniqueDefinition;
import com.example.game.mastery.domain.MasteryBalance;
import com.example.game.mastery.domain.TechniqueEffectSpec;
import com.example.game.mastery.infrastructure.CombatTechniqueDefinitionEntity;
import com.example.game.mastery.infrastructure.CombatTechniqueDefinitionRepository;

@Service
public class CombatTechniqueCatalogService {

	private final CombatTechniqueDefinitionRepository combatTechniqueDefinitionRepository;

	public CombatTechniqueCatalogService(CombatTechniqueDefinitionRepository combatTechniqueDefinitionRepository) {
		this.combatTechniqueDefinitionRepository = combatTechniqueDefinitionRepository;
	}

	@Transactional(readOnly = true)
	public CombatTechniqueCatalog load() {
		CombatTechniqueCatalog catalog = new CombatTechniqueCatalog(
				combatTechniqueDefinitionRepository.findAll(Sort.by("code")).stream()
						.map(CombatTechniqueCatalogService::toDomain)
						.toList());
		for (CombatTechniqueDefinition technique : catalog.all()) {
			if (!MasteryBalance.isUnlockLevel(technique.unlockMasteryLevel())) {
				throw new IllegalStateException(
						"Technique " + technique.code() + " uses unconfigured unlock level "
								+ technique.unlockMasteryLevel());
			}
		}
		return catalog;
	}

	private static CombatTechniqueDefinition toDomain(CombatTechniqueDefinitionEntity entity) {
		return new CombatTechniqueDefinition(
				entity.getCode(),
				entity.getDisplayName(),
				entity.getDescription(),
				entity.getWeaponFamily(),
				entity.getUnlockMasteryLevel(),
				entity.getKind(),
				new TechniqueEffectSpec(
						entity.getEffectCode(),
						entity.getStaminaCost(),
						entity.getAccuracyModifier(),
						entity.getDamagePercentModifier(),
						entity.getAppliesStatus(),
						entity.getStatusStacks(),
						entity.getStatusDurationRounds(),
						entity.getTags()));
	}
}
