package com.example.game.character.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.character.domain.CharacterBalance;
import com.example.game.character.domain.CharacterStatCalculator;
import com.example.game.character.domain.DerivedCombatStats;
import com.example.game.character.domain.ExperienceProgress;
import com.example.game.character.infrastructure.CharacterEntity;
import com.example.game.character.infrastructure.CharacterRepository;
import com.example.game.mastery.application.MasteryApplicationService;
import com.example.game.shared.api.ApiException;
import com.example.game.shared.infrastructure.ConstraintViolations;

@Service
public class CharacterApplicationService {

	private static final String ONE_CHARACTER_PER_ACCOUNT_CONSTRAINT = "uq_characters_account_id";
	private static final String UNIQUE_NAME_CONSTRAINT = "uq_characters_name_lower";

	private final CharacterRepository characterRepository;
	private final StartingLocationProvider startingLocationProvider;
	private final StarterLoadoutGranter starterLoadoutGranter;
	private final EquippedBonusProvider equippedBonusProvider;
	private final CharacterStateSyncService characterStateSyncService;
	private final MasteryApplicationService masteryApplicationService;
	private final Clock clock;

	public CharacterApplicationService(
			CharacterRepository characterRepository,
			StartingLocationProvider startingLocationProvider,
			StarterLoadoutGranter starterLoadoutGranter,
			EquippedBonusProvider equippedBonusProvider,
			CharacterStateSyncService characterStateSyncService,
			MasteryApplicationService masteryApplicationService,
			Clock clock) {
		this.characterRepository = characterRepository;
		this.startingLocationProvider = startingLocationProvider;
		this.starterLoadoutGranter = starterLoadoutGranter;
		this.equippedBonusProvider = equippedBonusProvider;
		this.characterStateSyncService = characterStateSyncService;
		this.masteryApplicationService = masteryApplicationService;
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

		UUID startingLocationId = startingLocationProvider.startingLocationId();

		int strength = CharacterBalance.STARTING_STRENGTH;
		int agility = CharacterBalance.STARTING_AGILITY;
		int endurance = CharacterBalance.STARTING_ENDURANCE;
		int perception = CharacterBalance.STARTING_PERCEPTION;
		int maxHealth = CharacterBalance.maxHealth(endurance, CharacterBalance.STARTING_LEVEL);
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
				0,
				startingLocationId,
				now,
				now,
				now);

		try {
			CharacterEntity saved = characterRepository.saveAndFlush(character);
			starterLoadoutGranter.grantStarterLoadout(saved.getId());
			masteryApplicationService.initializeForCharacter(saved.getId());
			return toView(saved);
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

	@Transactional
	public CharacterView current(UUID accountId) {
		CharacterEntity character = characterRepository.findWithLockByAccountId(accountId)
				.orElseThrow(CharacterErrors::characterNotFound);
		characterStateSyncService.sync(character);
		return toView(character);
	}

	@Transactional(readOnly = true)
	public boolean existsForAccount(UUID accountId) {
		return characterRepository.existsByAccountId(accountId);
	}

	@Transactional(readOnly = true)
	public CharacterPublicCore requirePublic(UUID characterId) {
		CharacterEntity character = characterRepository.findById(characterId)
				.orElseThrow(CharacterErrors::characterNotFound);
		return toPublicCore(character);
	}

	@Transactional(readOnly = true)
	public List<ArenaOpponentCore> arenaOpponents(
			UUID excludedId, int rating, int ratingBand, int page, int size) {
		return characterRepository.findArenaOpponents(
				excludedId, rating, ratingBand, PageRequest.of(page, size)).stream()
				.map(character -> new ArenaOpponentCore(
						character.getId(),
						character.getName(),
						character.getLevel(),
						character.getArenaRating()))
				.toList();
	}

	private static CharacterPublicCore toPublicCore(CharacterEntity character) {
		return new CharacterPublicCore(
				character.getId(),
				character.getName(),
				character.getLevel(),
				character.getStrength(),
				character.getAgility(),
				character.getEndurance(),
				character.getPerception(),
				character.getMaxHealth(),
				character.getMaxStamina(),
				character.getArenaRating(),
				character.getArenaMarks());
	}

	private CharacterView toView(CharacterEntity character) {
		EquippedBonuses bonuses = equippedBonusProvider.bonusesFor(character.getId());
		DerivedCombatStats derivedStats = CharacterStatCalculator.calculate(
				character.getStrength(),
				character.getAgility(),
				character.getPerception(),
				bonuses.weaponDamage(),
				bonuses.armorValue(),
				bonuses.accuracy(),
				bonuses.dodge(),
				bonuses.criticalChance(),
				bonuses.strength(),
				bonuses.agility(),
				bonuses.endurance(),
				bonuses.perception());
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
				character.getArenaRating(),
				character.getArenaMarks(),
				character.getUnspentAttributePoints(),
				ExperienceProgress.from(character.getLevel(), character.getExperience()),
				character.getCurrentLocationId(),
				derivedStats,
				character.getCreatedAt(),
				character.getUpdatedAt());
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
}
