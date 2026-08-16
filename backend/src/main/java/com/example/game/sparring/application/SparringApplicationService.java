package com.example.game.sparring.application;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.character.application.CharacterCombatGuard;
import com.example.game.character.application.CharacterVitalsService;
import com.example.game.character.application.CharacterVitalsView;
import com.example.game.combat.application.CombatApplicationService;
import com.example.game.combat.application.CombatView;
import com.example.game.combat.domain.CombatSessionStatus;
import com.example.game.combat.domain.EncounterStatus;
import com.example.game.combat.infrastructure.CombatSessionRepository;
import com.example.game.combat.infrastructure.EncounterEntity;
import com.example.game.combat.infrastructure.EncounterRepository;
import com.example.game.combat.infrastructure.MonsterDefinitionEntity;
import com.example.game.combat.infrastructure.MonsterDefinitionRepository;
import com.example.game.shared.domain.RandomProvider;
import com.example.game.sparring.domain.SparringBotCatalogEntry;
import com.example.game.sparring.domain.SparringBotGenerator;
import com.example.game.sparring.domain.SparringBotProfile;
import com.example.game.sparring.domain.SparringBots;
import com.example.game.world.application.LocationView;
import com.example.game.world.application.WorldApplicationService;
import com.example.game.world.domain.LocationCodes;

@Service
public class SparringApplicationService {

	private static final EnumSet<EncounterStatus> UNRESOLVED = EnumSet.of(
			EncounterStatus.AVAILABLE,
			EncounterStatus.COMBAT_STARTED);

	private final WorldApplicationService worldApplicationService;
	private final CharacterVitalsService characterVitalsService;
	private final CharacterCombatGuard characterCombatGuard;
	private final MonsterDefinitionRepository monsterDefinitionRepository;
	private final EncounterRepository encounterRepository;
	private final CombatSessionRepository combatSessionRepository;
	private final CombatApplicationService combatApplicationService;
	private final RandomProvider randomProvider;
	private final Clock clock;

	public SparringApplicationService(
			WorldApplicationService worldApplicationService,
			CharacterVitalsService characterVitalsService,
			CharacterCombatGuard characterCombatGuard,
			MonsterDefinitionRepository monsterDefinitionRepository,
			EncounterRepository encounterRepository,
			CombatSessionRepository combatSessionRepository,
			CombatApplicationService combatApplicationService,
			RandomProvider randomProvider,
			Clock clock) {
		this.worldApplicationService = worldApplicationService;
		this.characterVitalsService = characterVitalsService;
		this.characterCombatGuard = characterCombatGuard;
		this.monsterDefinitionRepository = monsterDefinitionRepository;
		this.encounterRepository = encounterRepository;
		this.combatSessionRepository = combatSessionRepository;
		this.combatApplicationService = combatApplicationService;
		this.randomProvider = randomProvider;
		this.clock = clock;
	}

	public List<SparringBotCatalogEntry> catalog() {
		return SparringBots.catalog();
	}

	@Transactional
	public CombatView startDrill(UUID accountId, int botLevel) {
		LocationView location = worldApplicationService.currentLocation(accountId);
		if (!LocationCodes.SPARRING_YARD.equals(location.code())) {
			throw SparringErrors.notAtYard();
		}
		if (!SparringBots.isValidBotLevel(botLevel)) {
			throw SparringErrors.invalidBotLevel();
		}
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		if (vitals.level() > SparringBots.MAX_PLAYER_LEVEL) {
			throw SparringErrors.playerLevelTooHigh();
		}
		characterCombatGuard.assertNotInActiveCombat(vitals.characterId());
		if (combatSessionRepository.existsByCharacterIdAndStatus(vitals.characterId(), CombatSessionStatus.ACTIVE)) {
			throw SparringErrors.combatInProgress();
		}
		if (encounterRepository.existsByCharacterIdAndStatusIn(vitals.characterId(), UNRESOLVED)) {
			throw SparringErrors.unresolvedEncounter();
		}
		if (combatSessionRepository.existsByCharacterIdAndOutcomeAcknowledgedFalse(vitals.characterId())) {
			throw SparringErrors.outcomePending();
		}
		MonsterDefinitionEntity monster = monsterDefinitionRepository.findByCode(SparringBots.codeForLevel(botLevel))
				.orElseThrow(() -> new IllegalStateException("sparring bot missing for level " + botLevel));
		SparringBotProfile bot = SparringBotGenerator.generate(botLevel, randomProvider);
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
		return combatApplicationService.startSparringDrill(accountId, encounter.getId(), bot.toMonsterProfile());
	}
}
