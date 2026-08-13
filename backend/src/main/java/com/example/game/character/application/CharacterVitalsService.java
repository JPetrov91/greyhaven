package com.example.game.character.application;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.character.infrastructure.CharacterEntity;
import com.example.game.character.infrastructure.CharacterRepository;

/**
 * Character health/attribute state for other gameplay modules. Callers describe intent
 * ({@code heal}) instead of mutating the entity themselves.
 */
@Service
public class CharacterVitalsService {

	private final CharacterRepository characterRepository;
	private final Clock clock;

	public CharacterVitalsService(CharacterRepository characterRepository, Clock clock) {
		this.characterRepository = characterRepository;
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
		return toView(characterRepository.findWithLockByAccountId(accountId)
				.orElseThrow(CharacterErrors::characterNotFound));
	}

	/**
	 * Locks by character id so inventory grants and internal equip paths serialize against the
	 * same row as account-scoped mutations.
	 */
	@Transactional(propagation = Propagation.MANDATORY)
	public CharacterVitalsView lockVitalsByCharacterId(UUID characterId) {
		return toView(characterRepository.findWithLockById(characterId)
				.orElseThrow(CharacterErrors::characterNotFound));
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void heal(UUID accountId, int amount) {
		if (amount < 1) {
			throw new IllegalArgumentException("heal amount must be positive");
		}
		CharacterEntity character = characterRepository.findByAccountId(accountId)
				.orElseThrow(CharacterErrors::characterNotFound);
		int healed = Math.min(character.getMaxHealth(), character.getCurrentHealth() + amount);
		character.applyHealth(healed, Instant.now(clock));
		characterRepository.saveAndFlush(character);
	}

	private static CharacterVitalsView toView(CharacterEntity character) {
		return new CharacterVitalsView(
				character.getId(),
				character.getLevel(),
				character.getStrength(),
				character.getAgility(),
				character.getEndurance(),
				character.getPerception(),
				character.getCurrentHealth(),
				character.getMaxHealth(),
				character.getCurrentStamina(),
				character.getMaxStamina());
	}
}
