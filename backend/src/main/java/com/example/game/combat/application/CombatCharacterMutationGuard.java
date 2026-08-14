package com.example.game.combat.application;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.example.game.character.application.CharacterCombatGuard;
import com.example.game.character.application.CharacterOccupationExtension;
import com.example.game.combat.domain.CombatSessionStatus;
import com.example.game.combat.infrastructure.CombatSessionRepository;
import com.example.game.shared.api.ApiException;

@Component
public class CombatCharacterMutationGuard implements CharacterCombatGuard {

	private final CombatSessionRepository combatSessionRepository;
	private final List<CharacterOccupationExtension> occupationExtensions;

	public CombatCharacterMutationGuard(
			CombatSessionRepository combatSessionRepository,
			List<CharacterOccupationExtension> occupationExtensions) {
		this.combatSessionRepository = combatSessionRepository;
		this.occupationExtensions = List.copyOf(occupationExtensions);
	}

	@Override
	public void assertNotInActiveCombat(UUID characterId) {
		if (inActiveCombat(characterId)) {
			throw new ApiException(
					"COMBAT_IN_PROGRESS",
					"That action is unavailable while combat is in progress.",
					HttpStatus.CONFLICT);
		}
	}

	@Override
	public boolean inActiveCombat(UUID characterId) {
		if (combatSessionRepository.existsByCharacterIdAndStatus(characterId, CombatSessionStatus.ACTIVE)) {
			return true;
		}
		for (CharacterOccupationExtension extension : occupationExtensions) {
			if (extension.occupied(characterId)) {
				return true;
			}
		}
		return false;
	}
}
