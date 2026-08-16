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

import com.example.game.account.infrastructure.AccountEntity;
import com.example.game.account.infrastructure.AccountRepository;
import com.example.game.character.domain.CharacterAppearance;
import com.example.game.character.domain.CharacterBalance;
import com.example.game.character.domain.CharacterGender;
import com.example.game.character.domain.CharacterSlots;
import com.example.game.character.domain.CharacterStatCalculator;
import com.example.game.character.domain.DerivedCombatStats;
import com.example.game.character.domain.ExperienceProgress;
import com.example.game.character.infrastructure.CharacterEntity;
import com.example.game.character.infrastructure.CharacterRepository;
import com.example.game.crafting.application.CraftingApplicationService;
import com.example.game.inventory.application.PublicEquipmentQuery;
import com.example.game.mastery.application.MasteryApplicationService;
import com.example.game.world.infrastructure.LocationEntity;
import com.example.game.world.infrastructure.LocationRepository;
import com.example.game.shared.api.ApiException;
import com.example.game.shared.infrastructure.ConstraintViolations;
import com.example.game.telemetry.application.GameTelemetry;
import com.example.game.telemetry.application.GameTelemetryRecorder;
import com.example.game.telemetry.domain.GoldCreateReason;

@Service
public class CharacterApplicationService {

	private static final String SLOT_CONSTRAINT = "uq_characters_account_slot";
	private static final String UNIQUE_NAME_CONSTRAINT = "uq_characters_name_lower";

	private final CharacterRepository characterRepository;
	private final AccountRepository accountRepository;
	private final LocationRepository locationRepository;
	private final ActiveCharacterResolver activeCharacterResolver;
	private final CharacterCombatGuard characterCombatGuard;
	private final StartingLocationProvider startingLocationProvider;
	private final StarterLoadoutGranter starterLoadoutGranter;
	private final com.example.game.quest.application.QuestApplicationService questApplicationService;
	private final EquippedBonusProvider equippedBonusProvider;
	private final PublicEquipmentQuery publicEquipmentQuery;
	private final CharacterStateSyncService characterStateSyncService;
	private final MasteryApplicationService masteryApplicationService;
	private final CraftingApplicationService craftingApplicationService;
	private final GameTelemetryRecorder gameTelemetryRecorder;
	private final CharacterUnlockQuery characterUnlockQuery;
	private final Clock clock;

	public CharacterApplicationService(
			CharacterRepository characterRepository,
			AccountRepository accountRepository,
			LocationRepository locationRepository,
			ActiveCharacterResolver activeCharacterResolver,
			CharacterCombatGuard characterCombatGuard,
			StartingLocationProvider startingLocationProvider,
			StarterLoadoutGranter starterLoadoutGranter,
			com.example.game.quest.application.QuestApplicationService questApplicationService,
			EquippedBonusProvider equippedBonusProvider,
			PublicEquipmentQuery publicEquipmentQuery,
			CharacterStateSyncService characterStateSyncService,
			MasteryApplicationService masteryApplicationService,
			CraftingApplicationService craftingApplicationService,
			GameTelemetryRecorder gameTelemetryRecorder,
			CharacterUnlockQuery characterUnlockQuery,
			Clock clock) {
		this.characterRepository = characterRepository;
		this.accountRepository = accountRepository;
		this.locationRepository = locationRepository;
		this.activeCharacterResolver = activeCharacterResolver;
		this.characterCombatGuard = characterCombatGuard;
		this.startingLocationProvider = startingLocationProvider;
		this.starterLoadoutGranter = starterLoadoutGranter;
		this.questApplicationService = questApplicationService;
		this.equippedBonusProvider = equippedBonusProvider;
		this.publicEquipmentQuery = publicEquipmentQuery;
		this.characterStateSyncService = characterStateSyncService;
		this.masteryApplicationService = masteryApplicationService;
		this.craftingApplicationService = craftingApplicationService;
		this.gameTelemetryRecorder = gameTelemetryRecorder;
		this.characterUnlockQuery = characterUnlockQuery;
		this.clock = clock;
	}

	@Transactional
	public CharacterView create(UUID accountId, String name) {
		return create(accountId, name, null, null, null);
	}

	@Transactional
	public CharacterView create(UUID accountId, String name, CharacterGender gender, String avatarCode) {
		return create(accountId, name, gender, avatarCode, null);
	}

	@Transactional
	public CharacterView create(
			UUID accountId,
			String name,
			CharacterGender gender,
			String avatarCode,
			Integer slotIndex) {
		int resolvedSlot = resolveSlot(accountId, slotIndex);

		if (characterRepository.existsByNameIgnoreCase(name)) {
			throw characterNameTaken();
		}

		CharacterGender resolvedGender = CharacterAppearance.resolveGender(gender);
		String resolvedAvatar = avatarCode == null || avatarCode.isBlank()
				? CharacterAppearance.defaultAvatar(resolvedGender)
				: avatarCode.trim();
		if (!CharacterAppearance.isAllowed(resolvedGender, resolvedAvatar)) {
			throw CharacterErrors.invalidAppearance();
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
				resolvedSlot,
				name,
				resolvedGender,
				resolvedAvatar,
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
			GameTelemetry.goldCreated(
					gameTelemetryRecorder,
					saved.getId(),
					CharacterBalance.STARTING_GOLD,
					GoldCreateReason.STARTING);
			starterLoadoutGranter.grantStarterLoadout(saved.getId());
			questApplicationService.activateForNewCharacter(saved.getId());
			masteryApplicationService.initializeForCharacter(saved.getId());
			craftingApplicationService.initializeForCharacter(saved.getId());
			assignActive(accountId, saved.getId(), now);
			return toView(saved);
		}
		catch (DataIntegrityViolationException exception) {
			if (ConstraintViolations.caused(exception, SLOT_CONSTRAINT)) {
				throw CharacterErrors.slotOccupied();
			}
			if (ConstraintViolations.caused(exception, UNIQUE_NAME_CONSTRAINT)) {
				throw characterNameTaken();
			}
			throw exception;
		}
	}

	@Transactional(readOnly = true)
	public List<CharacterRosterSlotView> roster(UUID accountId) {
		List<CharacterEntity> owned = characterRepository.findByAccountIdOrderBySlotIndexAsc(accountId);
		CharacterRosterSlotView[] slots = new CharacterRosterSlotView[CharacterSlots.MAX];
		for (int index = 0; index < CharacterSlots.MAX; index++) {
			slots[index] = CharacterRosterSlotView.empty(index);
		}
		for (CharacterEntity character : owned) {
			if (!CharacterSlots.validIndex(character.getSlotIndex())) {
				continue;
			}
			String locationName = locationRepository.findById(character.getCurrentLocationId())
					.map(LocationEntity::getName)
					.orElse(null);
			EquippedBonuses bonuses = equippedBonusProvider.bonusesFor(character.getId());
			DerivedCombatStats derived = CharacterStatCalculator.calculate(
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
			List<CharacterRosterEquippedView> equipped = publicEquipmentQuery.equippedItems(character.getId()).stream()
					.map(item -> new CharacterRosterEquippedView(
							item.slot().name(),
							item.displayName(),
							item.rarity().name()))
					.toList();
			int potions = publicEquipmentQuery.healingPotionStock(character.getId()).quantity();
			slots[character.getSlotIndex()] = new CharacterRosterSlotView(
					character.getSlotIndex(),
					false,
					character.getId(),
					character.getName(),
					character.getGender().name(),
					character.getAvatarCode(),
					character.getLevel(),
					character.getGold(),
					character.getCurrentLocationId(),
					locationName,
					character.getStrength(),
					character.getAgility(),
					character.getEndurance(),
					character.getPerception(),
					character.getCurrentHealth(),
					character.getMaxHealth(),
					character.getCurrentStamina(),
					character.getMaxStamina(),
					derived.physicalDamage(),
					derived.accuracy(),
					derived.dodge(),
					derived.criticalChance(),
					derived.armor(),
					potions,
					equipped);
		}
		return List.of(slots);
	}

	@Transactional
	public CharacterView select(UUID accountId, UUID characterId) {
		CharacterEntity target = characterRepository.findById(characterId)
				.orElseThrow(CharacterErrors::characterNotFound);
		if (!accountId.equals(target.getAccountId())) {
			throw CharacterErrors.characterNotFound();
		}
		AccountEntity account = accountRepository.findById(accountId)
				.orElseThrow(CharacterErrors::characterNotFound);
		UUID currentActive = account.getActiveCharacterId();
		if (currentActive != null && !currentActive.equals(characterId)) {
			characterCombatGuard.assertNotInActiveCombat(currentActive);
		}
		assignActive(accountId, characterId, Instant.now(clock));
		return current(accountId);
	}

	@Transactional
	public CharacterView current(UUID accountId) {
		CharacterEntity character = activeCharacterResolver.requireActive(accountId);
		if (characterStateSyncService.wouldMutate(character)) {
			character = activeCharacterResolver.requireActiveLocked(accountId);
			characterStateSyncService.sync(character);
		}
		return toView(character);
	}

	@Transactional(readOnly = true)
	public boolean isNameAvailable(String name) {
		return !characterRepository.existsByNameIgnoreCase(name);
	}

	@Transactional(readOnly = true)
	public int countForAccount(UUID accountId) {
		return characterRepository.countByAccountId(accountId);
	}

	@Transactional(readOnly = true)
	public UUID activeCharacterId(UUID accountId) {
		return accountRepository.findById(accountId)
				.map(AccountEntity::getActiveCharacterId)
				.orElse(null);
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
				character.getGender().name(),
				character.getAvatarCode(),
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
				character.getUpdatedAt(),
				characterUnlockQuery.unlockCodesOf(character.getId()));
	}

	private int resolveSlot(UUID accountId, Integer requestedSlot) {
		if (requestedSlot != null) {
			if (!CharacterSlots.validIndex(requestedSlot)) {
				throw CharacterErrors.invalidSlot();
			}
			if (characterRepository.existsByAccountIdAndSlotIndex(accountId, requestedSlot)) {
				throw CharacterErrors.slotOccupied();
			}
			return requestedSlot;
		}
		List<CharacterEntity> owned = characterRepository.findByAccountIdOrderBySlotIndexAsc(accountId);
		if (owned.size() >= CharacterSlots.MAX) {
			throw CharacterErrors.characterSlotsFull();
		}
		boolean[] taken = new boolean[CharacterSlots.MAX];
		for (CharacterEntity character : owned) {
			if (CharacterSlots.validIndex(character.getSlotIndex())) {
				taken[character.getSlotIndex()] = true;
			}
		}
		for (int index = 0; index < CharacterSlots.MAX; index++) {
			if (!taken[index]) {
				return index;
			}
		}
		throw CharacterErrors.characterSlotsFull();
	}

	private void assignActive(UUID accountId, UUID characterId, Instant updatedAt) {
		AccountEntity account = accountRepository.findById(accountId)
				.orElseThrow(CharacterErrors::characterNotFound);
		account.assignActiveCharacter(characterId, updatedAt);
		accountRepository.saveAndFlush(account);
	}

	private static ApiException characterNameTaken() {
		return new ApiException(
				"CHARACTER_NAME_TAKEN",
				"A character with this name already exists.",
				HttpStatus.CONFLICT);
	}
}
