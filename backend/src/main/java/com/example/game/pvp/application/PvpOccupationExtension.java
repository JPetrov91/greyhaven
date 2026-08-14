package com.example.game.pvp.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.example.game.character.application.CharacterOccupationExtension;
import com.example.game.pvp.domain.PvpMatchKind;
import com.example.game.pvp.domain.PvpMatchStatus;
import com.example.game.pvp.infrastructure.PvpMatchRepository;

@Component
public class PvpOccupationExtension implements CharacterOccupationExtension {

	private final PvpMatchRepository pvpMatchRepository;

	public PvpOccupationExtension(PvpMatchRepository pvpMatchRepository) {
		this.pvpMatchRepository = pvpMatchRepository;
	}

	@Override
	public boolean occupied(UUID characterId) {
		if (pvpMatchRepository.existsByAttackerIdAndStatus(characterId, PvpMatchStatus.ACTIVE)) {
			return true;
		}
		return pvpMatchRepository.existsByDefenderIdAndMatchKindAndStatusIn(
				characterId, PvpMatchKind.DUEL, List.of(PvpMatchStatus.ACTIVE));
	}
}
