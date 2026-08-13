package com.example.game.combat.application;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.character.application.CharacterLocationService;
import com.example.game.character.application.CharacterLocationView;
import com.example.game.character.application.CharacterVitalsService;
import com.example.game.character.application.CharacterVitalsView;
import com.example.game.combat.domain.CombatSessionStatus;
import com.example.game.combat.domain.EncounterStatus;
import com.example.game.combat.domain.WeightedPicker;
import com.example.game.combat.infrastructure.CombatSessionRepository;
import com.example.game.combat.infrastructure.EncounterEntity;
import com.example.game.combat.infrastructure.EncounterRepository;
import com.example.game.combat.infrastructure.LocationEncounterWeightEntity;
import com.example.game.combat.infrastructure.LocationEncounterWeightRepository;
import com.example.game.combat.infrastructure.MonsterDefinitionEntity;
import com.example.game.combat.infrastructure.MonsterDefinitionRepository;
import com.example.game.shared.domain.RandomProvider;
import com.example.game.world.application.LocationView;
import com.example.game.world.application.WorldApplicationService;
import com.example.game.world.domain.LocationSafety;

@Service
public class EncounterApplicationService {

	private static final EnumSet<EncounterStatus> UNRESOLVED = EnumSet.of(
			EncounterStatus.AVAILABLE,
			EncounterStatus.COMBAT_STARTED);

	private final CharacterLocationService characterLocationService;
	private final CharacterVitalsService characterVitalsService;
	private final WorldApplicationService worldApplicationService;
	private final LocationEncounterWeightRepository locationEncounterWeightRepository;
	private final MonsterDefinitionRepository monsterDefinitionRepository;
	private final EncounterRepository encounterRepository;
	private final CombatSessionRepository combatSessionRepository;
	private final CombatApplicationService combatApplicationService;
	private final RandomProvider randomProvider;
	private final Clock clock;

	public EncounterApplicationService(
			CharacterLocationService characterLocationService,
			CharacterVitalsService characterVitalsService,
			WorldApplicationService worldApplicationService,
			LocationEncounterWeightRepository locationEncounterWeightRepository,
			MonsterDefinitionRepository monsterDefinitionRepository,
			EncounterRepository encounterRepository,
			CombatSessionRepository combatSessionRepository,
			CombatApplicationService combatApplicationService,
			RandomProvider randomProvider,
			Clock clock) {
		this.characterLocationService = characterLocationService;
		this.characterVitalsService = characterVitalsService;
		this.worldApplicationService = worldApplicationService;
		this.locationEncounterWeightRepository = locationEncounterWeightRepository;
		this.monsterDefinitionRepository = monsterDefinitionRepository;
		this.encounterRepository = encounterRepository;
		this.combatSessionRepository = combatSessionRepository;
		this.combatApplicationService = combatApplicationService;
		this.randomProvider = randomProvider;
		this.clock = clock;
	}

	@Transactional(readOnly = true)
	public EncounterSearchView current(UUID accountId) {
		CharacterVitalsView vitals = characterVitalsService.vitalsOf(accountId);
		return encounterRepository
				.findByCharacterIdAndStatusIn(vitals.characterId(), EnumSet.of(EncounterStatus.AVAILABLE))
				.map(encounter -> {
					MonsterDefinitionEntity monster = monsterDefinitionRepository
							.findById(encounter.getMonsterDefinitionId())
							.orElseThrow(() -> new IllegalStateException(
									"monster definition missing: " + encounter.getMonsterDefinitionId()));
					return EncounterSearchView.found(encounter.getId(), toMonsterView(monster));
				})
				.orElseGet(EncounterSearchView::nothing);
	}

	@Transactional
	public EncounterSearchView search(UUID accountId) {
		CharacterLocationView locationView = characterLocationService.lockLocationOf(accountId);
		CharacterVitalsView vitals = characterVitalsService.lockVitalsByCharacterId(locationView.characterId());

		if (combatSessionRepository.existsByCharacterIdAndStatus(vitals.characterId(), CombatSessionStatus.ACTIVE)) {
			throw CombatErrors.combatInProgress();
		}
		if (encounterRepository.existsByCharacterIdAndStatusIn(vitals.characterId(), UNRESOLVED)) {
			throw CombatErrors.unresolvedEncounter();
		}

		combatApplicationService.acknowledgePendingOutcomes(vitals.characterId(), Instant.now(clock));

		LocationView location = worldApplicationService.currentLocation(accountId);
		if (location.safety() != LocationSafety.DANGEROUS) {
			throw CombatErrors.locationNotDangerous();
		}

		List<LocationEncounterWeightEntity> weights = locationEncounterWeightRepository
				.findByLocationId(location.id());
		if (weights.isEmpty()) {
			return EncounterSearchView.nothing();
		}

		List<WeightedPicker.WeightedOption<UUID>> options = weights.stream()
				.map(row -> new WeightedPicker.WeightedOption<>(row.getMonsterDefinitionId(), row.getWeight()))
				.toList();
		UUID monsterId = WeightedPicker.pick(options, randomProvider);
		if (monsterId == null) {
			return EncounterSearchView.nothing();
		}

		MonsterDefinitionEntity monster = monsterDefinitionRepository.findById(monsterId)
				.orElseThrow(() -> new IllegalStateException("monster definition missing: " + monsterId));
		Instant now = Instant.now(clock);
		EncounterEntity encounter = new EncounterEntity(
				UUID.randomUUID(),
				vitals.characterId(),
				location.id(),
				monster.getId(),
				EncounterStatus.AVAILABLE,
				now,
				now);
		encounterRepository.saveAndFlush(encounter);
		return EncounterSearchView.found(encounter.getId(), toMonsterView(monster));
	}

	@Transactional
	public CombatView fight(UUID accountId, UUID encounterId) {
		return combatApplicationService.startFromEncounter(accountId, encounterId);
	}

	@Transactional
	public EncounterView ignore(UUID accountId, UUID encounterId) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		EncounterEntity encounter = encounterRepository.findWithLockById(encounterId)
				.orElseThrow(CombatErrors::encounterNotFound);
		if (!encounter.getCharacterId().equals(vitals.characterId())) {
			throw CombatErrors.encounterNotFound();
		}
		if (encounter.getStatus() != EncounterStatus.AVAILABLE) {
			throw CombatErrors.encounterNotAvailable();
		}
		encounter.resolve(Instant.now(clock));
		encounterRepository.saveAndFlush(encounter);

		MonsterView monster = null;
		if (encounter.getMonsterDefinitionId() != null) {
			monster = monsterDefinitionRepository.findById(encounter.getMonsterDefinitionId())
					.map(EncounterApplicationService::toMonsterView)
					.orElse(null);
		}
		return new EncounterView(encounter.getId(), encounter.getStatus(), monster);
	}

	static MonsterView toMonsterView(MonsterDefinitionEntity monster) {
		return new MonsterView(
				monster.getId(),
				monster.getCode(),
				monster.getName(),
				monster.getLevel(),
				monster.getMaxHealth());
	}
}
