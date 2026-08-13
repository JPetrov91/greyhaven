package com.example.game.character.application;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.character.infrastructure.CharacterEntity;
import com.example.game.character.infrastructure.CharacterRepository;
import com.example.game.shared.api.ApiException;

@Service
public class CharacterProgressionService {

	private final CharacterRepository characterRepository;
	private final CharacterApplicationService characterApplicationService;
	private final CharacterCombatGuard characterCombatGuard;
	private final Clock clock;

	public CharacterProgressionService(
			CharacterRepository characterRepository,
			CharacterApplicationService characterApplicationService,
			CharacterCombatGuard characterCombatGuard,
			Clock clock) {
		this.characterRepository = characterRepository;
		this.characterApplicationService = characterApplicationService;
		this.characterCombatGuard = characterCombatGuard;
		this.clock = clock;
	}

	@Transactional
	public CharacterView allocateAttributes(
			UUID accountId,
			int strengthDelta,
			int agilityDelta,
			int enduranceDelta,
			int perceptionDelta) {
		CharacterEntity character = characterRepository.findWithLockByAccountId(accountId)
				.orElseThrow(CharacterErrors::characterNotFound);
		characterCombatGuard.assertNotInActiveCombat(character.getId());
		try {
			character.allocateAttributes(
					strengthDelta,
					agilityDelta,
					enduranceDelta,
					perceptionDelta,
					Instant.now(clock));
		}
		catch (IllegalArgumentException exception) {
			throw new ApiException(
					"INVALID_ATTRIBUTE_ALLOCATION",
					exception.getMessage(),
					HttpStatus.BAD_REQUEST);
		}
		characterRepository.saveAndFlush(character);
		return characterApplicationService.current(accountId);
	}
}
