package com.example.game.character.application;

import java.time.Clock;
import java.time.Instant;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.example.game.activity.application.ActivityApplicationService;
import com.example.game.character.domain.CharacterRecovery;
import com.example.game.character.infrastructure.CharacterEntity;
import com.example.game.character.infrastructure.CharacterRepository;

/**
 * Applies pending Phase 2 level catch-up and lazy out-of-combat recovery against a locked
 * character row. Callers own the transaction.
 */
@Service
public class CharacterStateSyncService {

	private final CharacterRepository characterRepository;
	private final CharacterCombatGuard characterCombatGuard;
	private final ActivityApplicationService activityApplicationService;
	private final Clock clock;

	public CharacterStateSyncService(
			CharacterRepository characterRepository,
			CharacterCombatGuard characterCombatGuard,
			@Lazy ActivityApplicationService activityApplicationService,
			Clock clock) {
		this.characterRepository = characterRepository;
		this.characterCombatGuard = characterCombatGuard;
		this.activityApplicationService = activityApplicationService;
		this.clock = clock;
	}

	public void sync(CharacterEntity character) {
		Instant now = Instant.now(clock);
		int previousLevel = character.getLevel();
		character.applyPendingLevelCatchUp(now);
		if (character.getLevel() > previousLevel) {
			activityApplicationService.recordLevelUps(character.getId(), previousLevel, character.getLevel());
		}
		if (!characterCombatGuard.inActiveCombat(character.getId())) {
			CharacterRecovery.Result recovered = CharacterRecovery.apply(
					character.getLevel(),
					character.getCurrentHealth(),
					character.getMaxHealth(),
					character.getCurrentStamina(),
					character.getMaxStamina(),
					character.getLastRecoveryAt(),
					now);
			character.checkpointRecovery(recovered.currentHealth(), recovered.currentStamina(), now);
		}
		characterRepository.saveAndFlush(character);
	}
}
