package com.example.game.character.application;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.character.domain.CharacterBalance;
import com.example.game.character.infrastructure.CharacterEntity;
import com.example.game.character.infrastructure.CharacterRepository;

/**
 * Character health/attribute state for other gameplay modules. Callers describe intent
 * ({@code heal}, {@code syncCombatVitals}) instead of mutating the entity themselves.
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

	/**
	 * Writes combat session vitals back onto the character row (source of truth sync).
	 */
	@Transactional(propagation = Propagation.MANDATORY)
	public CharacterVitalsView syncCombatVitals(UUID characterId, int currentHealth, int currentStamina) {
		CharacterEntity character = characterRepository.findWithLockById(characterId)
				.orElseThrow(CharacterErrors::characterNotFound);
		int health = Math.max(0, Math.min(currentHealth, character.getMaxHealth()));
		int stamina = Math.max(0, Math.min(currentStamina, character.getMaxStamina()));
		character.syncCombatVitals(health, stamina, Instant.now(clock));
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
		character.syncCombatVitals(
				CharacterBalance.defeatRecovery(character.getMaxHealth()),
				CharacterBalance.defeatRecovery(character.getMaxStamina()),
				Instant.now(clock));
		characterRepository.saveAndFlush(character);
		return toView(character);
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void spendGold(UUID characterId, int amount) {
		if (amount < 0) {
			throw new IllegalArgumentException("gold amount must be non-negative");
		}
		CharacterEntity character = characterRepository.findWithLockById(characterId)
				.orElseThrow(CharacterErrors::characterNotFound);
		if (character.getGold() < amount) {
			throw CharacterErrors.insufficientGold();
		}
		character.spendGold(amount, Instant.now(clock));
		characterRepository.saveAndFlush(character);
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void addGold(UUID characterId, int amount) {
		CharacterEntity character = characterRepository.findWithLockById(characterId)
				.orElseThrow(CharacterErrors::characterNotFound);
		character.addGold(amount, Instant.now(clock));
		characterRepository.saveAndFlush(character);
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public CharacterVitalsView grantCombatRewards(UUID characterId, int xp, int gold) {
		CharacterEntity character = characterRepository.findWithLockById(characterId)
				.orElseThrow(CharacterErrors::characterNotFound);
		Instant now = Instant.now(clock);
		character.grantExperience(xp, now);
		character.addGold(gold, now);
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
		if (damage == 0) {
			return toView(character);
		}
		int remaining = Math.max(1, character.getCurrentHealth() - damage);
		character.applyHealth(remaining, Instant.now(clock));
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
				character.getUnspentAttributePoints());
	}
}
