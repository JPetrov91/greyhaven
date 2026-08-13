package com.example.game.character.application;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.character.domain.CharacterBalance;
import com.example.game.character.infrastructure.CharacterEntity;
import com.example.game.character.infrastructure.CharacterRepository;
import com.example.game.shared.api.ApiException;
import com.example.game.shared.infrastructure.ConstraintViolations;
import com.example.game.world.domain.LocationCodes;
import com.example.game.world.infrastructure.LocationEntity;
import com.example.game.world.infrastructure.LocationRepository;

@Service
public class CharacterApplicationService {

	private static final String ONE_CHARACTER_PER_ACCOUNT_CONSTRAINT = "uq_characters_account_id";
	private static final String UNIQUE_NAME_CONSTRAINT = "uq_characters_name_lower";

	private final CharacterRepository characterRepository;
	private final LocationRepository locationRepository;
	private final Clock clock;

	public CharacterApplicationService(
			CharacterRepository characterRepository,
			LocationRepository locationRepository,
			Clock clock) {
		this.characterRepository = characterRepository;
		this.locationRepository = locationRepository;
		this.clock = clock;
	}

	@Transactional
	public CharacterView create(UUID accountId, String name) {
		if (characterRepository.existsByAccountId(accountId)) {
			throw characterAlreadyExists();
		}

		if (characterRepository.existsByNameIgnoreCase(name)) {
			throw characterNameTaken();
		}

		LocationEntity startingLocation = locationRepository.findByCode(LocationCodes.CITY_SQUARE)
				.orElseThrow(() -> new ApiException(
						"STARTING_LOCATION_MISSING",
						"Greyhaven starting location is not seeded.",
						HttpStatus.INTERNAL_SERVER_ERROR));

		int strength = CharacterBalance.STARTING_STRENGTH;
		int agility = CharacterBalance.STARTING_AGILITY;
		int endurance = CharacterBalance.STARTING_ENDURANCE;
		int perception = CharacterBalance.STARTING_PERCEPTION;
		int maxHealth = CharacterBalance.maxHealth(endurance);
		int maxStamina = CharacterBalance.maxStamina(endurance, agility);
		Instant now = Instant.now(clock);

		CharacterEntity character = new CharacterEntity(
				UUID.randomUUID(),
				accountId,
				name,
				CharacterBalance.STARTING_LEVEL,
				CharacterBalance.STARTING_EXPERIENCE,
				strength,
				agility,
				endurance,
				perception,
				maxHealth,
				maxHealth,
				maxStamina,
				maxStamina,
				CharacterBalance.STARTING_GOLD,
				startingLocation.getId(),
				now,
				now);

		try {
			return toView(characterRepository.saveAndFlush(character));
		}
		catch (DataIntegrityViolationException exception) {
			if (ConstraintViolations.caused(exception, ONE_CHARACTER_PER_ACCOUNT_CONSTRAINT)) {
				throw characterAlreadyExists();
			}
			if (ConstraintViolations.caused(exception, UNIQUE_NAME_CONSTRAINT)) {
				throw characterNameTaken();
			}
			throw exception;
		}
	}

	@Transactional(readOnly = true)
	public CharacterView current(UUID accountId) {
		CharacterEntity character = characterRepository.findByAccountId(accountId)
				.orElseThrow(() -> new ApiException(
						"CHARACTER_NOT_FOUND",
						"No character exists for this account.",
						HttpStatus.NOT_FOUND));
		return toView(character);
	}

	@Transactional(readOnly = true)
	public boolean existsForAccount(UUID accountId) {
		return characterRepository.existsByAccountId(accountId);
	}

	private static ApiException characterAlreadyExists() {
		return new ApiException(
				"CHARACTER_ALREADY_EXISTS",
				"This account already has a character.",
				HttpStatus.CONFLICT);
	}

	private static ApiException characterNameTaken() {
		return new ApiException(
				"CHARACTER_NAME_TAKEN",
				"A character with this name already exists.",
				HttpStatus.CONFLICT);
	}

	private static CharacterView toView(CharacterEntity character) {
		return new CharacterView(
				character.getId(),
				character.getAccountId(),
				character.getName(),
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
				character.getCurrentLocationId(),
				character.getCreatedAt(),
				character.getUpdatedAt());
	}
}
