package com.example.game.character.application;

import java.time.Clock;
import java.time.Instant;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.example.game.activity.application.ActivityApplicationService;
import com.example.game.character.domain.CharacterProgression;
import com.example.game.character.domain.CharacterRecovery;
import com.example.game.character.infrastructure.CharacterEntity;
import com.example.game.character.infrastructure.CharacterRepository;

/**
 * Applies pending Phase 2 level catch-up and lazy out-of-combat recovery against a locked
 * character row. Callers own the transaction. Persists only when vitals or level actually change.
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

	/**
	 * True when a later {@link #sync} would write level catch-up or recovered HP/stamina.
	 */
	public boolean wouldMutate(CharacterEntity character) {
		return pendingCatchUp(character) || pendingRecovery(character, Instant.now(clock));
	}

	/**
	 * @return {@code true} when the character row was written
	 */
	public boolean sync(CharacterEntity character) {
		Instant now = Instant.now(clock);
		boolean mutated = false;
		if (pendingCatchUp(character)) {
			int previousLevel = character.getLevel();
			character.applyPendingLevelCatchUp(now);
			if (character.getLevel() > previousLevel) {
				activityApplicationService.recordLevelUps(character.getId(), previousLevel, character.getLevel());
			}
			mutated = true;
		}
		if (pendingRecovery(character, now)) {
			CharacterRecovery.Result recovered = CharacterRecovery.apply(
					character.getLevel(),
					character.getCurrentHealth(),
					character.getMaxHealth(),
					character.getCurrentStamina(),
					character.getMaxStamina(),
					character.getLastRecoveryAt(),
					now);
			character.checkpointRecovery(recovered.currentHealth(), recovered.currentStamina(), now);
			mutated = true;
		}
		if (mutated) {
			characterRepository.saveAndFlush(character);
		}
		return mutated;
	}

	private static boolean pendingCatchUp(CharacterEntity character) {
		CharacterProgression.ProgressionResult catchUp = CharacterProgression.applyExperience(
				character.getLevel(),
				character.getExperience(),
				0);
		return catchUp.level() != character.getLevel() || catchUp.unspentAttributePointsGained() > 0;
	}

	private boolean pendingRecovery(CharacterEntity character, Instant now) {
		if (characterCombatGuard.inActiveCombat(character.getId())) {
			return false;
		}
		CharacterRecovery.Result recovered = CharacterRecovery.apply(
				character.getLevel(),
				character.getCurrentHealth(),
				character.getMaxHealth(),
				character.getCurrentStamina(),
				character.getMaxStamina(),
				character.getLastRecoveryAt(),
				now);
		return recovered.currentHealth() != character.getCurrentHealth()
				|| recovered.currentStamina() != character.getCurrentStamina();
	}
}
