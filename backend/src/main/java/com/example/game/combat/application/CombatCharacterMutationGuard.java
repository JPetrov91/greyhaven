package com.example.game.combat.application;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.example.game.character.application.CharacterCombatGuard;
import com.example.game.combat.domain.CombatSessionStatus;
import com.example.game.combat.infrastructure.CombatSessionRepository;
import com.example.game.shared.api.ApiException;

@Component
public class CombatCharacterMutationGuard implements CharacterCombatGuard {

	private final CombatSessionRepository combatSessionRepository;

	public CombatCharacterMutationGuard(CombatSessionRepository combatSessionRepository) {
		this.combatSessionRepository = combatSessionRepository;
	}

	@Override
	public void assertNotInActiveCombat(UUID characterId) {
		if (combatSessionRepository.existsByCharacterIdAndStatus(characterId, CombatSessionStatus.ACTIVE)) {
			throw new ApiException(
					"COMBAT_IN_PROGRESS",
					"That action is unavailable while combat is in progress.",
					HttpStatus.CONFLICT);
		}
	}
}
