package com.example.game.pvp.application;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.example.game.pvp.domain.PvpMatchStatus;
import com.example.game.pvp.infrastructure.PvpMatchRepository;
import com.example.game.shared.api.ApiException;
import com.example.game.world.application.CharacterTravelGuard;

@Component
public class PvpTravelGuard implements CharacterTravelGuard {

	private final PvpMatchRepository pvpMatchRepository;

	public PvpTravelGuard(PvpMatchRepository pvpMatchRepository) {
		this.pvpMatchRepository = pvpMatchRepository;
	}

	@Override
	public void assertCanTravel(UUID characterId) {
		if (pvpMatchRepository.existsByAttackerIdAndStatus(characterId, PvpMatchStatus.ACTIVE)
				|| pvpMatchRepository.findOpenDuelFor(characterId).isPresent()) {
			throw new ApiException(
					"COMBAT_IN_PROGRESS",
					"You cannot travel while a PvP match is in progress.",
					HttpStatus.CONFLICT);
		}
	}
}
