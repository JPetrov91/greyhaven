package com.example.game.combat.application;

import java.util.EnumSet;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.example.game.character.application.CharacterExpeditionStartGuard;
import com.example.game.combat.domain.CombatSessionStatus;
import com.example.game.combat.domain.EncounterStatus;
import com.example.game.combat.infrastructure.CombatSessionRepository;
import com.example.game.combat.infrastructure.EncounterRepository;
import com.example.game.shared.api.ApiException;

@Component
public class CombatCharacterExpeditionStartGuard implements CharacterExpeditionStartGuard {

	private final CombatSessionRepository combatSessionRepository;
	private final EncounterRepository encounterRepository;

	public CombatCharacterExpeditionStartGuard(
			CombatSessionRepository combatSessionRepository,
			EncounterRepository encounterRepository) {
		this.combatSessionRepository = combatSessionRepository;
		this.encounterRepository = encounterRepository;
	}

	@Override
	public void assertCanStartExpedition(UUID characterId) {
		if (combatSessionRepository.existsByCharacterIdAndStatus(characterId, CombatSessionStatus.ACTIVE)) {
			throw new ApiException(
					"COMBAT_IN_PROGRESS",
					"You cannot start an expedition while combat is in progress.",
					HttpStatus.CONFLICT);
		}
		if (encounterRepository.existsByCharacterIdAndStatusIn(
				characterId,
				EnumSet.of(EncounterStatus.AVAILABLE, EncounterStatus.COMBAT_STARTED))) {
			throw new ApiException(
					"UNRESOLVED_ENCOUNTER",
					"You must resolve your current encounter before starting an expedition.",
					HttpStatus.CONFLICT);
		}
		if (combatSessionRepository.existsByCharacterIdAndOutcomeAcknowledgedFalse(characterId)) {
			throw new ApiException(
					"COMBAT_OUTCOME_PENDING",
					"Acknowledge your combat result before starting an expedition.",
					HttpStatus.CONFLICT);
		}
	}
}
