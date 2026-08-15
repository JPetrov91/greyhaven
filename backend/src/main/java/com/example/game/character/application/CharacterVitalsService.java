package com.example.game.character.application;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.character.domain.CharacterBalance;
import com.example.game.character.domain.XpSource;
import com.example.game.character.infrastructure.CharacterEntity;
import com.example.game.character.infrastructure.CharacterRepository;
import com.example.game.telemetry.application.GameTelemetry;
import com.example.game.telemetry.application.GameTelemetryRecorder;
import com.example.game.telemetry.domain.GoldCreateReason;
import com.example.game.telemetry.domain.GoldDestroyReason;

/**
 * Character health/attribute state for other gameplay modules. Callers describe intent
 * ({@code heal}, {@code syncCombatVitals}) instead of mutating the entity themselves.
 */
@Service
public class CharacterVitalsService {

	private final CharacterRepository characterRepository;
	private final CharacterStateSyncService characterStateSyncService;
	private final GameTelemetryRecorder gameTelemetryRecorder;
	private final Clock clock;

	public CharacterVitalsService(
			CharacterRepository characterRepository,
			CharacterStateSyncService characterStateSyncService,
			GameTelemetryRecorder gameTelemetryRecorder,
			Clock clock) {
		this.characterRepository = characterRepository;
		this.characterStateSyncService = characterStateSyncService;
		this.gameTelemetryRecorder = gameTelemetryRecorder;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public CharacterVitalsView vitalsOf(UUID accountId) {
		return toView(characterRepository.findByAccountId(accountId)
				.orElseThrow(CharacterErrors::characterNotFound));
	}

	/**
	 * Locks the character row for inventory mutations that also change vitals or depend on level.
	 * {@code MANDATORY} keeps the lock for the caller's transaction.
	 */
	@Transactional(propagation = Propagation.MANDATORY)
	public CharacterVitalsView lockVitalsOf(UUID accountId) {
		CharacterEntity character = characterRepository.findWithLockByAccountId(accountId)
				.orElseThrow(CharacterErrors::characterNotFound);
		characterStateSyncService.sync(character);
		return toView(character);
	}

	/**
	 * Locks by character id so inventory grants and internal equip paths serialize against the
	 * same row as account-scoped mutations.
	 */
	@Transactional(propagation = Propagation.MANDATORY)
	public CharacterVitalsView lockVitalsByCharacterId(UUID characterId) {
		CharacterEntity character = characterRepository.findWithLockById(characterId)
				.orElseThrow(CharacterErrors::characterNotFound);
		characterStateSyncService.sync(character);
		return toView(character);
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void heal(UUID accountId, int amount) {
		if (amount < 1) {
			throw new IllegalArgumentException("heal amount must be positive");
		}
		CharacterEntity character = characterRepository.findWithLockByAccountId(accountId)
				.orElseThrow(CharacterErrors::characterNotFound);
		characterStateSyncService.sync(character);
		Instant now = Instant.now(clock);
		int healed = Math.min(character.getMaxHealth(), character.getCurrentHealth() + amount);
		character.applyHealth(healed, now);
		character.restartRecoveryBaseline(now);
		characterRepository.saveAndFlush(character);
	}

	/**
	 * Writes combat session vitals back onto the character row (source of truth sync).
	 */
	@Transactional(propagation = Propagation.MANDATORY)
	public CharacterVitalsView syncCombatVitals(UUID characterId, int currentHealth, int currentStamina) {
		return syncCombatVitals(characterId, currentHealth, currentStamina, false);
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public CharacterVitalsView syncCombatVitals(
			UUID characterId,
			int currentHealth,
			int currentStamina,
			boolean restartRecoveryBaseline) {
		CharacterEntity character = characterRepository.findWithLockById(characterId)
				.orElseThrow(CharacterErrors::characterNotFound);
		Instant now = Instant.now(clock);
		int health = Math.max(0, Math.min(currentHealth, character.getMaxHealth()));
		int stamina = Math.max(0, Math.min(currentStamina, character.getMaxStamina()));
		character.syncCombatVitals(health, stamina, now);
		if (restartRecoveryBaseline) {
			character.restartRecoveryBaseline(now);
		}
		characterRepository.saveAndFlush(character);
		return toView(character);
	}

	/**
	 * Office-first defeat: restore a share of max vitals so the explore/fight loop can continue.
	 * The cost of losing is forfeited rewards plus the partial recovery.
	 */
	@Transactional(propagation = Propagation.MANDATORY)
	public CharacterVitalsView applyDefeatRecovery(UUID characterId) {
		CharacterEntity character = characterRepository.findWithLockById(characterId)
				.orElseThrow(CharacterErrors::characterNotFound);
		Instant now = Instant.now(clock);
		character.syncCombatVitals(
				CharacterBalance.defeatRecovery(character.getMaxHealth()),
				CharacterBalance.defeatRecovery(character.getMaxStamina()),
				now);
		character.restartRecoveryBaseline(now);
		characterRepository.saveAndFlush(character);
		return toView(character);
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void spendGold(UUID characterId, int amount) {
		spendGold(characterId, amount, null);
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void spendGold(UUID characterId, int amount, GoldDestroyReason destroyReason) {
		if (amount < 0) {
			throw new IllegalArgumentException("gold amount must be non-negative");
		}
		CharacterEntity character = characterRepository.findWithLockById(characterId)
				.orElseThrow(CharacterErrors::characterNotFound);
		characterStateSyncService.sync(character);
		if (character.getGold() < amount) {
			throw CharacterErrors.insufficientGold();
		}
		character.spendGold(amount, Instant.now(clock));
		characterRepository.saveAndFlush(character);
		if (destroyReason != null) {
			GameTelemetry.goldDestroyed(gameTelemetryRecorder, characterId, amount, destroyReason);
		}
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void addGold(UUID characterId, int amount) {
		addGold(characterId, amount, null);
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void addGold(UUID characterId, int amount, GoldCreateReason createReason) {
		CharacterEntity character = characterRepository.findWithLockById(characterId)
				.orElseThrow(CharacterErrors::characterNotFound);
		characterStateSyncService.sync(character);
		character.addGold(amount, Instant.now(clock));
		characterRepository.saveAndFlush(character);
		if (createReason != null) {
			GameTelemetry.goldCreated(gameTelemetryRecorder, characterId, amount, createReason);
		}
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public CharacterVitalsView grantCombatRewards(UUID characterId, int xp, int gold) {
		return grantRewards(characterId, xp, gold, XpSource.PVE_COMBAT, GoldCreateReason.PVE_COMBAT);
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public CharacterVitalsView grantRewards(
			UUID characterId,
			int xp,
			int gold,
			XpSource xpSource,
			GoldCreateReason goldReason) {
		CharacterEntity character = characterRepository.findWithLockById(characterId)
				.orElseThrow(CharacterErrors::characterNotFound);
		characterStateSyncService.sync(character);
		Instant now = Instant.now(clock);
		int previousLevel = character.getLevel();
		character.grantExperience(xp, now);
		character.addGold(gold, now);
		characterRepository.saveAndFlush(character);
		GameTelemetry.xpEarned(gameTelemetryRecorder, characterId, xp, xpSource);
		GameTelemetry.goldCreated(gameTelemetryRecorder, characterId, gold, goldReason);
		GameTelemetry.levelUp(gameTelemetryRecorder, characterId, previousLevel, character.getLevel());
		return toView(character);
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public CharacterVitalsView applyArenaSettlement(
			UUID characterId,
			int ratingDelta,
			int ratingFloor,
			int marksAwarded) {
		CharacterEntity character = characterRepository.findWithLockById(characterId)
				.orElseThrow(CharacterErrors::characterNotFound);
		Instant now = Instant.now(clock);
		int newRating = Math.max(ratingFloor, character.getArenaRating() + ratingDelta);
		character.applyArenaRating(newRating, now);
		if (marksAwarded > 0) {
			character.addArenaMarks(marksAwarded, now);
		}
		characterRepository.saveAndFlush(character);
		return toView(character);
	}

	/**
	 * Applies minor expedition injury while keeping the character playable (at least 1 HP).
	 */
	@Transactional(propagation = Propagation.MANDATORY)
	public CharacterVitalsView applyInjury(UUID characterId, int damage) {
		if (damage < 0) {
			throw new IllegalArgumentException("damage must be non-negative");
		}
		CharacterEntity character = characterRepository.findWithLockById(characterId)
				.orElseThrow(CharacterErrors::characterNotFound);
		characterStateSyncService.sync(character);
		if (damage == 0) {
			return toView(character);
		}
		Instant now = Instant.now(clock);
		int remaining = Math.max(1, character.getCurrentHealth() - damage);
		character.applyHealth(remaining, now);
		character.restartRecoveryBaseline(now);
		characterRepository.saveAndFlush(character);
		return toView(character);
	}

	private static CharacterVitalsView toView(CharacterEntity character) {
		return new CharacterVitalsView(
				character.getId(),
				character.getLevel(),
				character.getExperience(),
				character.getStrength(),
				character.getAgility(),
				character.getEndurance(),
				character.getPerception(),
				character.getCurrentHealth(),
				character.getMaxHealth(),
				character.getCurrentStamina(),
				character.getMaxStamina(),
				character.getGold(),
				character.getUnspentAttributePoints(),
				character.getArenaRating(),
				character.getArenaMarks());
	}
}
