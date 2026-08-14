package com.example.game.mastery.application;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.activity.application.ActivityApplicationService;
import com.example.game.activity.domain.ActivityType;
import com.example.game.character.application.CharacterCombatGuard;
import com.example.game.character.application.CharacterVitalsService;
import com.example.game.character.application.CharacterVitalsView;
import com.example.game.inventory.application.EquippedWeaponQuery;
import com.example.game.item.domain.WeaponFamily;
import com.example.game.mastery.domain.CombatActiveTechniqueResolver;
import com.example.game.mastery.domain.CombatTechniqueCatalog;
import com.example.game.mastery.domain.CombatTechniqueDefinition;
import com.example.game.mastery.domain.MasteryBalance;
import com.example.game.mastery.domain.MasteryProgress;
import com.example.game.mastery.domain.MasteryProgression;
import com.example.game.mastery.domain.TechniqueLoadoutValidator;
import com.example.game.mastery.domain.TechniqueUnlockResolver;
import com.example.game.mastery.infrastructure.CharacterTechniqueEntity;
import com.example.game.mastery.infrastructure.CharacterTechniqueRepository;
import com.example.game.mastery.infrastructure.TechniqueLoadoutSlotEntity;
import com.example.game.mastery.infrastructure.TechniqueLoadoutSlotRepository;
import com.example.game.mastery.infrastructure.WeaponMasteryEntity;
import com.example.game.mastery.infrastructure.WeaponMasteryRepository;

@Service
public class MasteryApplicationService implements TechniqueLoadoutQuery {

	private final CharacterVitalsService characterVitalsService;
	private final CharacterCombatGuard characterCombatGuard;
	private final EquippedWeaponQuery equippedWeaponQuery;
	private final CombatTechniqueCatalogService combatTechniqueCatalogService;
	private final WeaponMasteryRepository weaponMasteryRepository;
	private final CharacterTechniqueRepository characterTechniqueRepository;
	private final TechniqueLoadoutSlotRepository techniqueLoadoutSlotRepository;
	private final ActivityApplicationService activityApplicationService;
	private final Clock clock;

	public MasteryApplicationService(
			CharacterVitalsService characterVitalsService,
			CharacterCombatGuard characterCombatGuard,
			EquippedWeaponQuery equippedWeaponQuery,
			CombatTechniqueCatalogService combatTechniqueCatalogService,
			WeaponMasteryRepository weaponMasteryRepository,
			CharacterTechniqueRepository characterTechniqueRepository,
			TechniqueLoadoutSlotRepository techniqueLoadoutSlotRepository,
			ActivityApplicationService activityApplicationService,
			Clock clock) {
		this.characterVitalsService = characterVitalsService;
		this.characterCombatGuard = characterCombatGuard;
		this.equippedWeaponQuery = equippedWeaponQuery;
		this.combatTechniqueCatalogService = combatTechniqueCatalogService;
		this.weaponMasteryRepository = weaponMasteryRepository;
		this.characterTechniqueRepository = characterTechniqueRepository;
		this.techniqueLoadoutSlotRepository = techniqueLoadoutSlotRepository;
		this.activityApplicationService = activityApplicationService;
		this.clock = clock;
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void initializeForCharacter(UUID characterId) {
		ensureInitialized(characterId);
	}

	/**
	 * Awards mastery XP for a PvE victory using the equipped main-hand weapon family. Safe to call
	 * from the combat reward transaction: session {@code rewards_applied} plus the mastery unique
	 * constraint prevent duplicate XP and unlocks.
	 */
	@Transactional(propagation = Propagation.MANDATORY)
	public void grantVictoryMastery(UUID characterId) {
		Optional<WeaponFamily> family = equippedWeaponQuery.mainHandFamily(characterId);
		if (family.isEmpty()) {
			return;
		}
		ensureInitialized(characterId);
		WeaponMasteryEntity mastery = weaponMasteryRepository
				.findWithLockByCharacterIdAndWeaponFamily(characterId, family.get())
				.orElseThrow(() -> new IllegalStateException("mastery row missing after initialize"));
		int previousLevel = mastery.getLevel();
		MasteryProgression.ProgressionResult result = MasteryProgression.applyExperience(
				previousLevel,
				mastery.getTotalExperience(),
				MasteryBalance.XP_PER_VICTORY);
		Instant now = Instant.now(clock);
		mastery.apply(result.level(), result.experience(), now);
		weaponMasteryRepository.saveAndFlush(mastery);

		if (result.level() > previousLevel) {
			for (int level = previousLevel + 1; level <= result.level(); level++) {
				activityApplicationService.record(
						characterId,
						ActivityType.MASTERY_UNLOCK,
						"Your " + titleCaseFamily(family.get()) + " mastery reached " + level + ".");
			}
		}

		CombatTechniqueCatalog catalog = combatTechniqueCatalogService.load();
		Set<String> alreadyUnlocked = unlockedCodes(characterId);
		for (CombatTechniqueDefinition technique : TechniqueUnlockResolver.unlocksBetween(
				catalog,
				family.get(),
				previousLevel,
				result.level())) {
			if (alreadyUnlocked.contains(technique.code())) {
				continue;
			}
			if (insertUnlock(characterId, technique.code(), now)) {
				activityApplicationService.record(
						characterId,
						ActivityType.TECHNIQUE_UNLOCK,
						"You learned " + technique.displayName() + ".");
			}
		}
	}

	@Transactional
	public MasteriesView masteries(UUID accountId) {
		CharacterVitalsView vitals = characterVitalsService.vitalsOf(accountId);
		ensureInitialized(vitals.characterId());
		CombatTechniqueCatalog catalog = combatTechniqueCatalogService.load();
		WeaponFamily equipped = equippedWeaponQuery.mainHandFamily(vitals.characterId()).orElse(null);
		Map<WeaponFamily, WeaponMasteryEntity> rows = rowsByFamily(vitals.characterId());
		List<WeaponMasteryView> views = new ArrayList<>();
		for (WeaponFamily family : WeaponFamily.values()) {
			WeaponMasteryEntity row = rows.get(family);
			int level = row.getLevel();
			MasteryProgress progress = MasteryProgress.from(level, row.getTotalExperience());
			List<String> nextUnlocks = catalog.forFamily(family).stream()
					.filter(technique -> technique.unlockMasteryLevel() > level)
					.sorted(Comparator.comparingInt(CombatTechniqueDefinition::unlockMasteryLevel)
							.thenComparing(CombatTechniqueDefinition::code))
					.map(CombatTechniqueDefinition::code)
					.toList();
			views.add(new WeaponMasteryView(
					family,
					level,
					row.getTotalExperience(),
					progress,
					nextUnlocks));
		}
		return new MasteriesView(equipped, views);
	}

	@Transactional
	public TechniquesView techniques(UUID accountId) {
		CharacterVitalsView vitals = characterVitalsService.vitalsOf(accountId);
		ensureInitialized(vitals.characterId());
		CombatTechniqueCatalog catalog = combatTechniqueCatalogService.load();
		Set<String> unlocked = unlockedCodes(vitals.characterId());
		WeaponFamily equipped = equippedWeaponQuery.mainHandFamily(vitals.characterId()).orElse(null);
		List<TechniqueDefinitionView> techniques = catalog.all().stream()
				.map(definition -> toTechniqueView(definition, unlocked.contains(definition.code())))
				.toList();
		return new TechniquesView(equipped, techniques, loadoutView(vitals.characterId(), catalog, equipped));
	}

	@Transactional
	public TechniquesView replaceLoadout(UUID accountId, List<String> slots) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		characterCombatGuard.assertNotInActiveCombat(vitals.characterId());
		ensureInitialized(vitals.characterId());
		CombatTechniqueCatalog catalog = combatTechniqueCatalogService.load();
		List<String> normalized = normalizeSlots(slots);
		TechniqueLoadoutValidator.Result result = TechniqueLoadoutValidator.validate(
				normalized,
				unlockedCodes(vitals.characterId()),
				catalog);
		if (!result.valid()) {
			throw MasteryErrors.invalidLoadout(result.reason());
		}
		List<TechniqueLoadoutSlotEntity> rows = techniqueLoadoutSlotRepository
				.findWithLockByCharacterIdOrderBySlotIndexAsc(vitals.characterId());
		if (rows.size() != TechniqueLoadoutValidator.SLOT_COUNT) {
			throw new IllegalStateException("loadout slots missing after initialize");
		}
		for (int i = 0; i < TechniqueLoadoutValidator.SLOT_COUNT; i++) {
			rows.get(i).assign(normalized.get(i));
		}
		techniqueLoadoutSlotRepository.saveAll(rows);
		techniqueLoadoutSlotRepository.flush();
		WeaponFamily equipped = equippedWeaponQuery.mainHandFamily(vitals.characterId()).orElse(null);
		Set<String> unlocked = unlockedCodes(vitals.characterId());
		List<TechniqueDefinitionView> techniques = catalog.all().stream()
				.map(definition -> toTechniqueView(definition, unlocked.contains(definition.code())))
				.toList();
		return new TechniquesView(equipped, techniques, loadoutView(vitals.characterId(), catalog, equipped));
	}

	@Override
	@Transactional
	public List<String> activeTechniqueCodes(UUID characterId) {
		ensureInitialized(characterId);
		WeaponFamily equipped = equippedWeaponQuery.mainHandFamily(characterId).orElse(null);
		CombatTechniqueCatalog catalog = combatTechniqueCatalogService.load();
		List<String> slots = techniqueLoadoutSlotRepository.findByCharacterIdOrderBySlotIndexAsc(characterId).stream()
				.map(TechniqueLoadoutSlotEntity::getTechniqueCode)
				.toList();
		return CombatActiveTechniqueResolver.resolve(slots, equipped, catalog);
	}

	@Override
	@Transactional
	public int masteryLevel(UUID characterId, WeaponFamily family) {
		ensureInitialized(characterId);
		return weaponMasteryRepository.findByCharacterIdAndWeaponFamily(characterId, family)
				.map(WeaponMasteryEntity::getLevel)
				.orElse(0);
	}

	private void ensureInitialized(UUID characterId) {
		Instant now = Instant.now(clock);
		for (WeaponFamily family : WeaponFamily.values()) {
			if (!weaponMasteryRepository.existsByCharacterIdAndWeaponFamily(characterId, family)) {
				try {
					weaponMasteryRepository.saveAndFlush(new WeaponMasteryEntity(
							UUID.randomUUID(),
							characterId,
							family,
							0,
							0,
							now));
				}
				catch (DataIntegrityViolationException ignored) {
					// Concurrent initialize of the same family.
				}
			}
		}
		List<TechniqueLoadoutSlotEntity> slots = techniqueLoadoutSlotRepository
				.findByCharacterIdOrderBySlotIndexAsc(characterId);
		Set<Integer> present = slots.stream()
				.map(TechniqueLoadoutSlotEntity::getSlotIndex)
				.collect(Collectors.toSet());
		for (int index = 0; index < TechniqueLoadoutValidator.SLOT_COUNT; index++) {
			if (present.contains(index)) {
				continue;
			}
			try {
				techniqueLoadoutSlotRepository.saveAndFlush(new TechniqueLoadoutSlotEntity(
						UUID.randomUUID(),
						characterId,
						index,
						null));
			}
			catch (DataIntegrityViolationException ignored) {
				// Concurrent initialize of the same slot.
			}
		}
	}

	private boolean insertUnlock(UUID characterId, String techniqueCode, Instant unlockedAt) {
		if (characterTechniqueRepository.existsByCharacterIdAndTechniqueCode(characterId, techniqueCode)) {
			return false;
		}
		try {
			characterTechniqueRepository.saveAndFlush(new CharacterTechniqueEntity(
					UUID.randomUUID(),
					characterId,
					techniqueCode,
					unlockedAt));
			return true;
		}
		catch (DataIntegrityViolationException ignored) {
			return false;
		}
	}

	private Set<String> unlockedCodes(UUID characterId) {
		return characterTechniqueRepository.findByCharacterId(characterId).stream()
				.map(CharacterTechniqueEntity::getTechniqueCode)
				.collect(Collectors.toCollection(HashSet::new));
	}

	private Map<WeaponFamily, WeaponMasteryEntity> rowsByFamily(UUID characterId) {
		Map<WeaponFamily, WeaponMasteryEntity> rows = new EnumMap<>(WeaponFamily.class);
		for (WeaponMasteryEntity row : weaponMasteryRepository.findByCharacterIdOrderByWeaponFamilyAsc(characterId)) {
			rows.put(row.getWeaponFamily(), row);
		}
		return rows;
	}

	private TechniqueLoadoutView loadoutView(
			UUID characterId,
			CombatTechniqueCatalog catalog,
			WeaponFamily equipped) {
		List<String> slots = new ArrayList<>();
		for (TechniqueLoadoutSlotEntity row : techniqueLoadoutSlotRepository
				.findByCharacterIdOrderBySlotIndexAsc(characterId)) {
			slots.add(row.getTechniqueCode());
		}
		while (slots.size() < TechniqueLoadoutValidator.SLOT_COUNT) {
			slots.add(null);
		}
		WeaponFamily loadoutFamily = null;
		for (String code : slots) {
			if (code == null || code.isBlank()) {
				continue;
			}
			loadoutFamily = catalog.require(code).weaponFamily();
			break;
		}
		boolean compatible = loadoutFamily == null || equipped == null || loadoutFamily == equipped;
		return new TechniqueLoadoutView(
				Collections.unmodifiableList(new ArrayList<>(slots.subList(0, TechniqueLoadoutValidator.SLOT_COUNT))),
				loadoutFamily,
				compatible);
	}

	private static List<String> normalizeSlots(List<String> slots) {
		if (slots == null) {
			return null;
		}
		List<String> normalized = new ArrayList<>(slots.size());
		for (String slot : slots) {
			if (slot == null || slot.isBlank()) {
				normalized.add(null);
			}
			else {
				normalized.add(slot.trim());
			}
		}
		return normalized;
	}

	private static TechniqueDefinitionView toTechniqueView(CombatTechniqueDefinition definition, boolean unlocked) {
		return new TechniqueDefinitionView(
				definition.code(),
				definition.displayName(),
				definition.description(),
				definition.weaponFamily(),
				definition.unlockMasteryLevel(),
				definition.kind(),
				unlocked,
				definition.effect().staminaCost(),
				definition.effect().accuracyModifier(),
				definition.effect().damagePercentModifier(),
				definition.effect().appliesStatus(),
				definition.effect().tags());
	}

	private static String titleCaseFamily(WeaponFamily family) {
		String raw = family.name().toLowerCase(Locale.ROOT);
		return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
	}
}
