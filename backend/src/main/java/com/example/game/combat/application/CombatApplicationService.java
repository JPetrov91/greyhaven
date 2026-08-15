package com.example.game.combat.application;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.game.activity.application.ActivityApplicationService;
import com.example.game.character.application.CharacterVitalsService;
import com.example.game.character.application.CharacterVitalsView;
import com.example.game.character.application.EquippedBonusProvider;
import com.example.game.character.application.EquippedBonuses;
import com.example.game.character.domain.CharacterStatCalculator;
import com.example.game.character.domain.DerivedCombatStats;
import com.example.game.character.domain.ProgressionBalance;
import com.example.game.combat.domain.ActionCombatBalance;
import com.example.game.combat.domain.Combat2State;
import com.example.game.combat.domain.CombatAction;
import com.example.game.combat.domain.CombatActionContext;
import com.example.game.combat.domain.CombatEngine;
import com.example.game.combat.domain.CombatEvent;
import com.example.game.combat.domain.CombatRoundResult;
import com.example.game.combat.domain.CombatRuleViolation;
import com.example.game.combat.domain.CombatRulesVersion;
import com.example.game.combat.domain.CombatSessionState;
import com.example.game.combat.domain.CombatSessionStatus;
import com.example.game.combat.domain.CombatV2Balance;
import com.example.game.combat.domain.CombatantSide;
import com.example.game.combat.domain.CombatantStats;
import com.example.game.combat.domain.EncounterStatus;
import com.example.game.combat.domain.LootDrop;
import com.example.game.combat.domain.LootGenerator;
import com.example.game.combat.domain.LootTableEntry;
import com.example.game.combat.domain.MonsterCombatProfile;
import com.example.game.combat.domain.MonsterCombatStats;
import com.example.game.combat.domain.Phase1CombatEngine;
import com.example.game.combat.domain.StatusEffectEngine;
import com.example.game.combat.domain.StatusInstance;
import com.example.game.combat.domain.StatusType;
import com.example.game.combat.domain.UniqueLoot;
import com.example.game.combat.infrastructure.CharacterUniqueDropEntity;
import com.example.game.combat.infrastructure.CharacterUniqueDropRepository;
import com.example.game.combat.infrastructure.CombatEventEntity;
import com.example.game.combat.infrastructure.CombatEventRepository;
import com.example.game.combat.infrastructure.CombatRewardItemEntity;
import com.example.game.combat.infrastructure.CombatRewardItemRepository;
import com.example.game.combat.infrastructure.CombatSessionEntity;
import com.example.game.combat.infrastructure.CombatSessionRepository;
import com.example.game.combat.infrastructure.CombatStatusEffectEntity;
import com.example.game.combat.infrastructure.CombatStatusEffectRepository;
import com.example.game.combat.infrastructure.EncounterEntity;
import com.example.game.combat.infrastructure.EncounterRepository;
import com.example.game.combat.infrastructure.MonsterDefinitionEntity;
import com.example.game.combat.infrastructure.MonsterDefinitionRepository;
import com.example.game.combat.infrastructure.MonsterLootEntryEntity;
import com.example.game.combat.infrastructure.MonsterLootEntryRepository;
import com.example.game.inventory.application.EquippedWeaponQuery;
import com.example.game.inventory.application.InventoryApplicationService;
import com.example.game.inventory.application.InventoryFullException;
import com.example.game.item.application.ItemCatalogService;
import com.example.game.item.application.ItemDefinitionView;
import com.example.game.item.domain.GeneratedItem;
import com.example.game.item.domain.WeaponFamily;
import com.example.game.mastery.application.CombatTechniqueCatalogService;
import com.example.game.mastery.application.MasteryApplicationService;
import com.example.game.mastery.application.TechniqueLoadoutQuery;
import com.example.game.mastery.domain.CombatTechniqueCatalog;
import com.example.game.mastery.domain.CombatTechniqueDefinition;
import com.example.game.mastery.domain.TechniqueEffectSpec;
import com.example.game.mastery.domain.TechniqueKind;
import com.example.game.shared.domain.RandomProvider;
import com.example.game.telemetry.application.GameTelemetry;
import com.example.game.telemetry.application.GameTelemetryRecorder;
import com.example.game.telemetry.domain.ItemCreateSource;

@Service
public class CombatApplicationService {

	private final CharacterVitalsService characterVitalsService;
	private final EquippedBonusProvider equippedBonusProvider;
	private final InventoryApplicationService inventoryApplicationService;
	private final ActivityApplicationService activityApplicationService;
	private final EncounterRepository encounterRepository;
	private final CombatSessionRepository combatSessionRepository;
	private final CombatEventRepository combatEventRepository;
	private final CombatRewardItemRepository combatRewardItemRepository;
	private final CombatStatusEffectRepository combatStatusEffectRepository;
	private final MonsterDefinitionRepository monsterDefinitionRepository;
	private final MonsterLootEntryRepository monsterLootEntryRepository;
	private final CharacterUniqueDropRepository characterUniqueDropRepository;
	private final ItemCatalogService itemCatalogService;
	private final MasteryApplicationService masteryApplicationService;
	private final TechniqueLoadoutQuery techniqueLoadoutQuery;
	private final CombatTechniqueCatalogService combatTechniqueCatalogService;
	private final EquippedWeaponQuery equippedWeaponQuery;
	private final GameTelemetryRecorder gameTelemetryRecorder;
	private final RandomProvider randomProvider;
	private final Clock clock;
	private final TransactionTemplate transactionTemplate;
	private final ApplicationEventPublisher eventPublisher;

	public CombatApplicationService(
			CharacterVitalsService characterVitalsService,
			EquippedBonusProvider equippedBonusProvider,
			InventoryApplicationService inventoryApplicationService,
			ActivityApplicationService activityApplicationService,
			EncounterRepository encounterRepository,
			CombatSessionRepository combatSessionRepository,
			CombatEventRepository combatEventRepository,
			CombatRewardItemRepository combatRewardItemRepository,
			CombatStatusEffectRepository combatStatusEffectRepository,
			MonsterDefinitionRepository monsterDefinitionRepository,
			MonsterLootEntryRepository monsterLootEntryRepository,
			CharacterUniqueDropRepository characterUniqueDropRepository,
			ItemCatalogService itemCatalogService,
			MasteryApplicationService masteryApplicationService,
			TechniqueLoadoutQuery techniqueLoadoutQuery,
			CombatTechniqueCatalogService combatTechniqueCatalogService,
			EquippedWeaponQuery equippedWeaponQuery,
			GameTelemetryRecorder gameTelemetryRecorder,
			RandomProvider randomProvider,
			Clock clock,
			PlatformTransactionManager transactionManager,
			ApplicationEventPublisher eventPublisher) {
		this.characterVitalsService = characterVitalsService;
		this.equippedBonusProvider = equippedBonusProvider;
		this.inventoryApplicationService = inventoryApplicationService;
		this.activityApplicationService = activityApplicationService;
		this.encounterRepository = encounterRepository;
		this.combatSessionRepository = combatSessionRepository;
		this.combatEventRepository = combatEventRepository;
		this.combatRewardItemRepository = combatRewardItemRepository;
		this.combatStatusEffectRepository = combatStatusEffectRepository;
		this.monsterDefinitionRepository = monsterDefinitionRepository;
		this.monsterLootEntryRepository = monsterLootEntryRepository;
		this.characterUniqueDropRepository = characterUniqueDropRepository;
		this.itemCatalogService = itemCatalogService;
		this.masteryApplicationService = masteryApplicationService;
		this.techniqueLoadoutQuery = techniqueLoadoutQuery;
		this.combatTechniqueCatalogService = combatTechniqueCatalogService;
		this.equippedWeaponQuery = equippedWeaponQuery;
		this.gameTelemetryRecorder = gameTelemetryRecorder;
		this.randomProvider = randomProvider;
		this.clock = clock;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
		this.eventPublisher = eventPublisher;
	}

	@Transactional
	public CombatView startFromEncounter(UUID accountId, UUID encounterId) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		EncounterEntity encounter = encounterRepository.findWithLockById(encounterId)
				.orElseThrow(CombatErrors::encounterNotFound);
		if (!encounter.getCharacterId().equals(vitals.characterId())) {
			throw CombatErrors.encounterNotFound();
		}
		if (encounter.getStatus() != EncounterStatus.AVAILABLE) {
			throw CombatErrors.encounterNotAvailable();
		}
		if (combatSessionRepository.existsByCharacterIdAndStatus(vitals.characterId(), CombatSessionStatus.ACTIVE)) {
			throw CombatErrors.combatInProgress();
		}

		MonsterDefinitionEntity monster = monsterDefinitionRepository.findById(encounter.getMonsterDefinitionId())
				.orElseThrow(() -> new IllegalStateException("monster missing for encounter"));

		if (combatSessionRepository.existsByCharacterIdAndOutcomeAcknowledgedFalse(vitals.characterId())) {
			throw CombatErrors.outcomePending();
		}
		Instant now = Instant.now(clock);
		encounter.markCombatStarted(now);
		encounterRepository.saveAndFlush(encounter);

		CombatSessionEntity session = new CombatSessionEntity(
				UUID.randomUUID(),
				encounter.getId(),
				vitals.characterId(),
				monster.getId(),
				CombatSessionStatus.ACTIVE,
				0,
				vitals.currentHealth(),
				vitals.currentStamina(),
				monster.getMaxHealth(),
				now,
				now);
		combatSessionRepository.saveAndFlush(session);
		captureCombat2Snapshot(session, vitals.characterId(), monster);
		combatSessionRepository.saveAndFlush(session);
		createRewardPlan(session, monster, now);
		GameTelemetry.combatStarted(
				gameTelemetryRecorder,
				vitals.characterId(),
				session.getWeaponFamily(),
				session.getTechniqueCodes(),
				monster.getLevel(),
				monster.getMonsterTier().name());
		return toView(session, monster, vitals, loadEvents(session.getId()), null);
	}

	@Transactional(readOnly = true)
	public CombatView current(UUID accountId) {
		CharacterVitalsView vitals = characterVitalsService.vitalsOf(accountId);
		return combatSessionRepository
				.findByCharacterIdAndStatus(vitals.characterId(), CombatSessionStatus.ACTIVE)
				.map(active -> {
					MonsterDefinitionEntity monster = requireMonster(active.getMonsterDefinitionId());
					return toView(active, monster, vitals, loadEvents(active.getId()), null);
				})
				.or(() -> combatSessionRepository
						.findByCharacterIdAndOutcomeAcknowledgedFalse(vitals.characterId())
						.map(pending -> {
							MonsterDefinitionEntity monster = requireMonster(pending.getMonsterDefinitionId());
							CombatRewardsView rewards = pending.isRewardsApplied() ? loadRewards(pending) : null;
							return toView(pending, monster, vitals, loadEvents(pending.getId()), rewards);
						}))
				.orElse(null);
	}

	@Transactional
	public void acknowledgeOutcome(UUID accountId, UUID combatId) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		CombatSessionEntity session = combatSessionRepository.findWithLockById(combatId)
				.orElseThrow(CombatErrors::combatNotFound);
		if (!session.getCharacterId().equals(vitals.characterId())) {
			throw CombatErrors.combatNotFound();
		}
		if (session.getStatus() == CombatSessionStatus.ACTIVE) {
			throw CombatErrors.combatStillActive();
		}
		if (session.getStatus() == CombatSessionStatus.PLAYER_WON && !session.isRewardsApplied()) {
			throw CombatErrors.rewardsNeedInventorySpace();
		}
		if (session.isOutcomeAcknowledged()) {
			return;
		}
		session.acknowledgeOutcome(Instant.now(clock));
		combatSessionRepository.saveAndFlush(session);
	}

	@Transactional
	public CombatView submitAction(
			UUID accountId,
			UUID combatId,
			CombatAction action,
			int expectedRoundNumber) {
		return submitAction(accountId, combatId, action, null, expectedRoundNumber);
	}

	public CombatView submitAction(
			UUID accountId,
			UUID combatId,
			CombatAction action,
			String techniqueCode,
			int expectedRoundNumber) {
		CombatView persisted = Objects.requireNonNull(transactionTemplate.execute(status -> persistTurn(
				accountId, combatId, action, techniqueCode, expectedRoundNumber)));
		if (persisted.status() == CombatSessionStatus.PLAYER_WON && persisted.rewards() == null) {
			try {
				return Objects.requireNonNull(transactionTemplate.execute(status -> claimVictoryRewards(accountId, combatId)));
			}
			catch (InventoryFullException exception) {
				throw CombatErrors.rewardsNeedInventorySpace();
			}
		}
		return persisted;
	}

	private CombatView persistTurn(
			UUID accountId,
			UUID combatId,
			CombatAction action,
			String techniqueCode,
			int expectedRoundNumber) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		CombatSessionEntity session = combatSessionRepository.findWithLockById(combatId)
				.orElseThrow(CombatErrors::combatNotFound);
		if (!session.getCharacterId().equals(vitals.characterId())) {
			throw CombatErrors.combatNotFound();
		}
		if (session.getStatus() != CombatSessionStatus.ACTIVE) {
			if (session.isRewardsApplied()) {
				MonsterDefinitionEntity monster = requireMonster(session.getMonsterDefinitionId());
				return toView(session, monster, vitals, loadEvents(session.getId()), loadRewards(session));
			}
			if (session.getStatus() == CombatSessionStatus.PLAYER_WON) {
				MonsterDefinitionEntity monster = requireMonster(session.getMonsterDefinitionId());
				return toView(session, monster, vitals, loadEvents(session.getId()), null);
			}
			throw CombatErrors.combatNotActive();
		}
		if (session.getRoundNumber() != expectedRoundNumber) {
			throw CombatErrors.staleCombatState();
		}

		MonsterDefinitionEntity monster = requireMonster(session.getMonsterDefinitionId());
		if (!session.isRewardPlanCreated()) {
			createRewardPlan(session, monster, Instant.now(clock));
		}
		EquippedBonuses bonuses = equippedBonusProvider.bonusesFor(vitals.characterId());
		DerivedCombatStats derived = CharacterStatCalculator.calculate(
				vitals.strength(),
				vitals.agility(),
				vitals.perception(),
				bonuses.weaponDamage(),
				bonuses.armorValue(),
				bonuses.accuracy(),
				bonuses.dodge(),
				bonuses.criticalChance(),
				bonuses.strength(),
				bonuses.agility(),
				bonuses.endurance(),
				bonuses.perception());
		int totalAgility = vitals.agility() + bonuses.agility();

		boolean playerStunned = session.getRulesVersion() == CombatRulesVersion.COMBAT_2
				&& StatusEffectEngine.has(loadStatuses(session.getId(), CombatantSide.PLAYER), StatusType.STUN);
		boolean potionAvailable = inventoryApplicationService.hasHealingPotion(vitals.characterId());
		int potionHeal = 0;
		if (action == CombatAction.USE_POTION && !playerStunned) {
			if (!potionAvailable) {
				throw CombatErrors.noPotion();
			}
			potionHeal = inventoryApplicationService.consumeOneHealingPotion(vitals.characterId());
		}

		CombatActionContext actionContext = new CombatActionContext(
				potionAvailable || action == CombatAction.USE_POTION, potionHeal);
		CombatRoundResult result;
		try {
			if (session.getRulesVersion() == CombatRulesVersion.PHASE_1) {
				if (action == CombatAction.USE_TECHNIQUE) {
					throw CombatErrors.invalidTechnique();
				}
				CombatSessionState state = new CombatSessionState(
						session.getRoundNumber(),
						session.getPlayerHealth(),
						vitals.maxHealth(),
						session.getPlayerStamina(),
						vitals.maxStamina(),
						session.getEnemyHealth(),
						session.getStatus(),
						new CombatantStats(
								derived.physicalDamage(),
								derived.accuracy(),
								derived.dodge(),
								derived.criticalChance(),
								derived.armor(),
								totalAgility),
						new MonsterCombatStats(
								monster.getName(),
								monster.getLevel(),
								monster.getDamageMin(),
								monster.getDamageMax()));
				result = Phase1CombatEngine.resolve(state, action, actionContext, randomProvider);
			}
			else {
				result = CombatEngine.resolve(
						buildCombat2State(session, vitals, derived, totalAgility, bonuses),
						action,
						techniqueCode,
						actionContext,
						randomProvider);
			}
		}
		catch (CombatRuleViolation violation) {
			throw switch (violation.getReason()) {
				case INSUFFICIENT_STAMINA -> CombatErrors.insufficientStamina();
				case NO_POTION -> CombatErrors.noPotion();
				case COMBAT_NOT_ACTIVE -> CombatErrors.combatNotActive();
				case INVALID_TECHNIQUE -> CombatErrors.invalidTechnique();
			};
		}

		Instant now = Instant.now(clock);
		session.applyRound(
				result.roundNumber(),
				result.playerHealth(),
				result.playerStamina(),
				result.enemyHealth(),
				result.enemyStamina(),
				result.status(),
				result.lastEnemyMissed(),
				result.lastPlayerGuarded(),
				now);
		combatSessionRepository.saveAndFlush(session);
		if (session.getRulesVersion() == CombatRulesVersion.COMBAT_2) {
			replaceStatuses(session.getId(), result.playerStatuses(), result.enemyStatuses());
		}
		persistEvents(session.getId(), result.roundNumber(), result.events(), now);
		if (result.status() != CombatSessionStatus.ACTIVE) {
			List<GameTelemetry.CombatLogLine> log = combatEventRepository
					.findBySessionIdOrderByRoundNumberAscSequenceNumberAsc(session.getId())
					.stream()
					.map(event -> new GameTelemetry.CombatLogLine(event.getEventType(), event.getMessage()))
					.toList();
			GameTelemetry.combatEnded(
					gameTelemetryRecorder,
					session.getCharacterId(),
					result.status(),
					Math.max(0, now.toEpochMilli() - session.getCreatedAt().toEpochMilli()),
					result.roundNumber(),
					session.getWeaponFamily(),
					session.getTechniqueCodes(),
					log);
		}

		CharacterVitalsView synced;
		if (result.status() == CombatSessionStatus.PLAYER_LOST) {
			synced = characterVitalsService.applyDefeatRecovery(vitals.characterId());
			resolveEncounter(session.getEncounterId(), EncounterCloseReason.LOST, now);
		}
		else if (result.status() == CombatSessionStatus.PLAYER_ESCAPED) {
			synced = characterVitalsService.syncCombatVitals(
					vitals.characterId(),
					result.playerHealth(),
					result.playerStamina(),
					true);
			resolveEncounter(session.getEncounterId(), EncounterCloseReason.ESCAPED, now);
		}
		else if (result.status() == CombatSessionStatus.PLAYER_WON) {
			synced = characterVitalsService.syncCombatVitals(
					vitals.characterId(),
					result.playerHealth(),
					result.playerStamina(),
					true);
			resolveEncounter(session.getEncounterId(), EncounterCloseReason.WON, now);
		}
		else {
			synced = characterVitalsService.syncCombatVitals(
					vitals.characterId(),
					result.playerHealth(),
					result.playerStamina());
		}

		CombatRewardsView rewards = session.isRewardsApplied() ? loadRewards(session) : null;
		return toView(session, monster, synced, loadEvents(session.getId()), rewards);
	}

	private CombatView claimVictoryRewards(UUID accountId, UUID combatId) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		CombatSessionEntity session = combatSessionRepository.findWithLockById(combatId)
				.orElseThrow(CombatErrors::combatNotFound);
		if (!session.getCharacterId().equals(vitals.characterId())) {
			throw CombatErrors.combatNotFound();
		}
		if (session.getStatus() != CombatSessionStatus.PLAYER_WON) {
			throw CombatErrors.combatNotActive();
		}
		MonsterDefinitionEntity monster = requireMonster(session.getMonsterDefinitionId());
		if (!session.isRewardsApplied()) {
			applyRewardsExactlyOnce(session, monster, Instant.now(clock));
			vitals = characterVitalsService.lockVitalsByCharacterId(vitals.characterId());
		}
		return toView(session, monster, vitals, loadEvents(session.getId()), loadRewards(session));
	}

	/**
	 * Idempotent reward application. Safe under concurrent completion attempts because the session
	 * row is locked and {@code rewards_applied} flips in the same transaction.
	 *
	 * <p>The combat round is committed before this runs. A full inventory leaves the victory in
	 * place so the player can make room and claim without re-rolling the fight.
	 */
	void applyRewardsExactlyOnce(CombatSessionEntity session, MonsterDefinitionEntity monster, Instant now) {
		if (session.isRewardsApplied()) {
			return;
		}
		int gold = session.getPlannedGold();
		int xp = session.getPlannedXp();
		List<CombatRewardItemEntity> rewardRows = combatRewardItemRepository.findBySessionId(session.getId());
		Map<UUID, ItemDefinitionView> definitions = itemCatalogService.findByIds(
				rewardRows.stream().map(CombatRewardItemEntity::getItemDefinitionId).toList());

		CharacterVitalsView before = characterVitalsService.lockVitalsByCharacterId(session.getCharacterId());
		int previousLevel = before.level();

		characterVitalsService.grantCombatRewards(session.getCharacterId(), xp, gold);
		masteryApplicationService.grantVictoryMastery(session.getCharacterId());
		Set<String> uniqueCodes = uniqueItemCodes(monster.getId());

		for (CombatRewardItemEntity reward : rewardRows) {
			ItemDefinitionView item = requireItem(definitions, reward.getItemDefinitionId());
			if (item.type().isStackable() || !reward.hasPlannedRoll()) {
				inventoryApplicationService.grantItems(
						session.getCharacterId(),
						item.code(),
						reward.getQuantity());
			}
			else {
				inventoryApplicationService.grantRolled(
						session.getCharacterId(),
						item.code(),
						reward.getQuantity(),
						reward.toGenerated());
			}
			activityApplicationService.recordItemFound(
					session.getCharacterId(),
					item.name(),
					reward.getQuantity());
			GameTelemetry.itemCreated(
					gameTelemetryRecorder,
					session.getCharacterId(),
					item.code(),
					reward.hasPlannedRoll() ? reward.toGenerated().rarity() : item.rarity(),
					reward.getQuantity(),
					ItemCreateSource.PVE_LOOT);
			if (uniqueCodes.contains(item.code())
					&& !characterUniqueDropRepository.existsByCharacterIdAndItemCode(
							session.getCharacterId(), item.code())) {
				characterUniqueDropRepository.saveAndFlush(new CharacterUniqueDropEntity(
						UUID.randomUUID(),
						session.getCharacterId(),
						item.code(),
						now));
			}
		}

		CharacterVitalsView after = characterVitalsService.lockVitalsByCharacterId(session.getCharacterId());
		activityApplicationService.recordCombatVictory(session.getCharacterId(), monster.getName());
		activityApplicationService.recordLevelUps(session.getCharacterId(), previousLevel, after.level());

		session.markRewards(xp, gold, previousLevel, after.level(), now);
		combatSessionRepository.saveAndFlush(session);
	}

	private static int attributePointsGained(CombatSessionEntity session) {
		Integer previous = session.getRewardPreviousLevel();
		Integer next = session.getRewardNewLevel();
		if (previous == null || next == null || next <= previous) {
			return 0;
		}
		return (next - previous) * ProgressionBalance.ATTRIBUTE_POINTS_PER_LEVEL;
	}

	private void createRewardPlan(
			CombatSessionEntity session,
			MonsterDefinitionEntity monster,
			Instant now) {
		if (session.isRewardPlanCreated()) {
			return;
		}
		int gold = LootGenerator.rollGold(monster.getGoldMin(), monster.getGoldMax(), randomProvider);
		Set<String> granted = new HashSet<>(
				characterUniqueDropRepository.findItemCodesByCharacterId(session.getCharacterId()));
		List<LootTableEntry> table = UniqueLoot.excludingGranted(buildLootTable(monster.getId()), granted);
		Set<String> uniqueCodes = uniqueItemCodes(monster.getId());
		List<LootDrop> drops = LootGenerator.generate(table, randomProvider);
		Map<UUID, ItemDefinitionView> definitions = itemCatalogService.findByIds(
				drops.stream().map(LootDrop::itemDefinitionId).toList());
		List<CombatRewardItemEntity> rewardRows = new ArrayList<>();
		for (LootDrop drop : drops) {
			ItemDefinitionView item = requireItem(definitions, drop.itemDefinitionId());
			if (item.type().isStackable()) {
				rewardRows.add(new CombatRewardItemEntity(
						UUID.randomUUID(),
						session.getId(),
						drop.itemDefinitionId(),
						drop.quantity(),
						new GeneratedItem(item.rarity(), null, null, List.of())));
			}
			else if (uniqueCodes.contains(item.code())) {
				for (int i = 0; i < drop.quantity(); i++) {
					rewardRows.add(new CombatRewardItemEntity(
							UUID.randomUUID(),
							session.getId(),
							drop.itemDefinitionId(),
							1,
							new GeneratedItem(
									item.rarity(),
									item.weaponDamage(),
									item.armorValue(),
									List.of())));
				}
			}
			else {
				for (int i = 0; i < drop.quantity(); i++) {
					rewardRows.add(new CombatRewardItemEntity(
							UUID.randomUUID(),
							session.getId(),
							drop.itemDefinitionId(),
							1,
							inventoryApplicationService.rollItem(item.code())));
				}
			}
		}
		if (!rewardRows.isEmpty()) {
			combatRewardItemRepository.saveAll(rewardRows);
			combatRewardItemRepository.flush();
		}
		session.markRewardPlan(monster.getXpReward(), gold, now);
		combatSessionRepository.saveAndFlush(session);
	}

	private void resolveEncounter(UUID encounterId, EncounterCloseReason reason, Instant now) {
		EncounterEntity encounter = encounterRepository.findWithLockById(encounterId)
				.orElseThrow(CombatErrors::encounterNotFound);
		if (encounter.getStatus() != EncounterStatus.RESOLVED && encounter.getStatus() != EncounterStatus.EXPIRED) {
			encounter.resolve(now);
			encounterRepository.saveAndFlush(encounter);
		}
		if (encounter.isDungeonEncounter()) {
			eventPublisher.publishEvent(new EncounterClosedEvent(encounter.getCharacterId(), encounterId, reason));
		}
	}

	private List<LootTableEntry> buildLootTable(UUID monsterDefinitionId) {
		List<MonsterLootEntryEntity> rows = monsterLootEntryRepository.findByMonsterDefinitionId(monsterDefinitionId);
		Map<UUID, ItemDefinitionView> items = itemCatalogService.findByIds(
				rows.stream().map(MonsterLootEntryEntity::getItemDefinitionId).toList());
		List<LootTableEntry> table = new ArrayList<>(rows.size());
		for (MonsterLootEntryEntity row : rows) {
			ItemDefinitionView item = requireItem(items, row.getItemDefinitionId());
			table.add(new LootTableEntry(
					item.id(),
					item.code(),
					row.getDropChancePercent(),
					row.getQuantityMin(),
					row.getQuantityMax(),
					row.isOncePerCharacter()));
		}
		return table;
	}

	private Set<String> uniqueItemCodes(UUID monsterDefinitionId) {
		Set<String> codes = new HashSet<>();
		for (LootTableEntry entry : buildLootTable(monsterDefinitionId)) {
			if (entry.oncePerCharacter()) {
				codes.add(entry.itemCode());
			}
		}
		return codes;
	}

	private void persistEvents(UUID sessionId, int roundNumber, List<CombatEvent> events, Instant now) {
		int sequence = 1;
		List<CombatEventEntity> rows = new ArrayList<>(events.size());
		for (CombatEvent event : events) {
			rows.add(new CombatEventEntity(
					UUID.randomUUID(),
					sessionId,
					roundNumber,
					sequence++,
					event.type(),
					event.message(),
					now));
		}
		combatEventRepository.saveAll(rows);
		combatEventRepository.flush();
	}

	private List<CombatEventView> loadEvents(UUID sessionId) {
		return combatEventRepository.findBySessionIdOrderByRoundNumberAscSequenceNumberAsc(sessionId).stream()
				.map(event -> new CombatEventView(
						event.getRoundNumber(),
						event.getSequenceNumber(),
						event.getEventType(),
						event.getMessage()))
				.toList();
	}

	private CombatRewardsView loadRewards(CombatSessionEntity session) {
		List<CombatRewardItemEntity> rows = combatRewardItemRepository.findBySessionId(session.getId());
		Map<UUID, ItemDefinitionView> definitions = itemCatalogService.findByIds(
				rows.stream().map(CombatRewardItemEntity::getItemDefinitionId).toList());
		List<CombatRewardItemView> items = rows.stream()
				.map(row -> {
					ItemDefinitionView item = requireItem(definitions, row.getItemDefinitionId());
					return new CombatRewardItemView(item.code(), item.name(), row.getQuantity());
				})
				.toList();
		return new CombatRewardsView(
				session.getXpAwarded() == null ? 0 : session.getXpAwarded(),
				session.getGoldAwarded() == null ? 0 : session.getGoldAwarded(),
				session.getRewardPreviousLevel() == null ? 0 : session.getRewardPreviousLevel(),
				session.getRewardNewLevel() == null ? 0 : session.getRewardNewLevel(),
				attributePointsGained(session),
				items);
	}

	private static ItemDefinitionView requireItem(Map<UUID, ItemDefinitionView> definitions, UUID itemDefinitionId) {
		ItemDefinitionView item = definitions.get(itemDefinitionId);
		if (item == null) {
			throw new IllegalStateException("item definition missing: " + itemDefinitionId);
		}
		return item;
	}

	private MonsterDefinitionEntity requireMonster(UUID monsterDefinitionId) {
		return monsterDefinitionRepository.findById(monsterDefinitionId)
				.orElseThrow(() -> new IllegalStateException("monster definition missing"));
	}

	private CombatView toView(
			CombatSessionEntity session,
			MonsterDefinitionEntity monster,
			CharacterVitalsView vitals,
			List<CombatEventView> events,
			CombatRewardsView rewards) {
		boolean potionAvailable = inventoryApplicationService.hasHealingPotion(session.getCharacterId());
		List<CombatStatusView> playerStatuses = toStatusViews(loadStatuses(session.getId(), CombatantSide.PLAYER));
		List<CombatStatusView> enemyStatuses = toStatusViews(loadStatuses(session.getId(), CombatantSide.ENEMY));
		boolean stunned = playerStatuses.stream().anyMatch(status -> status.type() == StatusType.STUN);
		EquippedBonuses bonuses = equippedBonusProvider.bonusesFor(session.getCharacterId());
		int reduction = session.getRulesVersion() == CombatRulesVersion.COMBAT_2
				? session.getStaminaCostReduction()
				: bonuses.staminaCostReduction();
		CoreActionCostsView costs = new CoreActionCostsView(
				CombatV2Balance.reducedStaminaCost(ActionCombatBalance.QUICK_STAMINA_COST, reduction),
				CombatV2Balance.reducedStaminaCost(ActionCombatBalance.HEAVY_STAMINA_COST, reduction),
				CombatV2Balance.reducedStaminaCost(ActionCombatBalance.PRECISE_STAMINA_COST, reduction));
		List<CombatTechniqueOptionView> techniques = session.getRulesVersion() == CombatRulesVersion.COMBAT_2
				? techniqueOptions(session, stunned)
				: List.of();
		int enemyMaxStamina = session.getRulesVersion() == CombatRulesVersion.COMBAT_2
				? session.getEnemyMaxStamina()
				: 0;
		CombatIntentView enemyIntent = null;
		List<CombatActionPreviewView> actionPreviews;
		if (session.getRulesVersion() == CombatRulesVersion.COMBAT_2) {
			DerivedCombatStats derived = CharacterStatCalculator.calculate(
					vitals.strength(),
					vitals.agility(),
					vitals.perception(),
					bonuses.weaponDamage(),
					bonuses.armorValue(),
					bonuses.accuracy(),
					bonuses.dodge(),
					bonuses.criticalChance(),
					bonuses.strength(),
					bonuses.agility(),
					bonuses.endurance(),
					bonuses.perception());
			int totalAgility = vitals.agility() + bonuses.agility();
			Combat2State state = buildCombat2State(session, vitals, derived, totalAgility, bonuses);
			if (session.getStatus() == CombatSessionStatus.ACTIVE) {
				var kind = CombatEngine.previewEnemyIntent(state);
				enemyIntent = new CombatIntentView(kind.name(), enemyIntentLabel(kind, state.enemy().signatureStatus()));
			}
			actionPreviews = buildActionPreviews(session, state, stunned, potionAvailable, techniques);
		}
		else {
			actionPreviews = phase1ActionPreviews(costs, stunned, potionAvailable, session.getPlayerStamina());
		}
		return new CombatView(
				session.getId(),
				session.getEncounterId(),
				session.getStatus(),
				session.getRulesVersion(),
				session.getRoundNumber(),
				session.getPlayerHealth(),
				vitals.maxHealth(),
				session.getPlayerStamina(),
				vitals.maxStamina(),
				session.getEnemyHealth(),
				monster.getMaxHealth(),
				session.getEnemyStamina(),
				enemyMaxStamina,
				EncounterApplicationService.toMonsterView(monster),
				potionAvailable,
				stunned,
				playerStatuses,
				enemyStatuses,
				techniques,
				costs,
				events,
				rewards,
				enemyIntent,
				actionPreviews,
				lootPreview(monster.getId()));
	}

	private void captureCombat2Snapshot(
			CombatSessionEntity session,
			UUID characterId,
			MonsterDefinitionEntity monster) {
		EquippedBonuses bonuses = equippedBonusProvider.bonusesFor(characterId);
		WeaponFamily family = equippedWeaponQuery.mainHandFamily(characterId).orElse(null);
		List<String> codes = techniqueLoadoutQuery.activeTechniqueCodes(characterId);
		session.captureCombat2Snapshot(
				monster.getMaxStamina(),
				monster.getMaxStamina(),
				monster.getArmor(),
				monster.getAccuracy(),
				monster.getDodge(),
				monster.getCriticalChance(),
				monster.getDamageMin(),
				monster.getDamageMax(),
				monster.getAiArchetype(),
				monster.getSignatureStatus(),
				monster.getMonsterTier(),
				family,
				codes.isEmpty() ? null : String.join(",", codes),
				bonuses.staminaCostReduction());
	}

	private Combat2State buildCombat2State(
			CombatSessionEntity session,
			CharacterVitalsView vitals,
			DerivedCombatStats derived,
			int totalAgility,
			EquippedBonuses bonuses) {
		CombatTechniqueCatalog catalog = combatTechniqueCatalogService.load();
		List<String> codes = parseTechniqueCodes(session.getTechniqueCodes());
		java.util.Map<String, TechniqueEffectSpec> specs = new java.util.LinkedHashMap<>();
		for (String code : codes) {
			specs.put(code, catalog.require(code).effect());
		}
		TechniqueEffectSpec passive = masteryPassive(session.getCharacterId(), session.getWeaponFamily(), catalog);
		MonsterCombatProfile enemy = new MonsterCombatProfile(
				requireMonster(session.getMonsterDefinitionId()).getName(),
				requireMonster(session.getMonsterDefinitionId()).getLevel(),
				session.getSnapEnemyDamageMin(),
				session.getSnapEnemyDamageMax(),
				session.getSnapEnemyArmor(),
				session.getSnapEnemyAccuracy(),
				session.getSnapEnemyDodge(),
				session.getSnapEnemyCriticalChance(),
				requireMonster(session.getMonsterDefinitionId()).getMaxHealth(),
				session.getEnemyMaxStamina(),
				session.getSnapAiArchetype(),
				session.getSnapSignatureStatus(),
				session.getSnapMonsterTier());
		return new Combat2State(
				session.getRoundNumber(),
				session.getPlayerHealth(),
				vitals.maxHealth(),
				session.getPlayerStamina(),
				vitals.maxStamina(),
				session.getEnemyHealth(),
				enemy.maxHealth(),
				session.getEnemyStamina(),
				session.getEnemyMaxStamina(),
				session.getStatus(),
				new CombatantStats(
						derived.physicalDamage(),
						derived.accuracy(),
						derived.dodge(),
						derived.criticalChance(),
						derived.armor(),
						totalAgility),
				enemy,
				loadStatuses(session.getId(), CombatantSide.PLAYER),
				loadStatuses(session.getId(), CombatantSide.ENEMY),
				codes,
				specs,
				passive,
				session.getStaminaCostReduction(),
				session.isLastEnemyMissed(),
				session.isLastPlayerGuarded());
	}

	private TechniqueEffectSpec masteryPassive(
			UUID characterId,
			WeaponFamily family,
			CombatTechniqueCatalog catalog) {
		if (family == null || techniqueLoadoutQuery.masteryLevel(characterId, family) < 10) {
			return null;
		}
		return catalog.forFamily(family).stream()
				.filter(definition -> definition.kind() == TechniqueKind.PASSIVE)
				.map(CombatTechniqueDefinition::effect)
				.findFirst()
				.orElse(null);
	}

	private List<CombatLootPreviewView> lootPreview(UUID monsterDefinitionId) {
		List<MonsterLootEntryEntity> entries = monsterLootEntryRepository.findByMonsterDefinitionId(monsterDefinitionId);
		if (entries.isEmpty()) {
			return List.of();
		}
		Map<UUID, ItemDefinitionView> definitions = itemCatalogService.findByIds(
				entries.stream().map(MonsterLootEntryEntity::getItemDefinitionId).toList());
		List<CombatLootPreviewView> preview = new ArrayList<>();
		for (MonsterLootEntryEntity entry : entries) {
			ItemDefinitionView item = definitions.get(entry.getItemDefinitionId());
			if (item == null) {
				continue;
			}
			preview.add(new CombatLootPreviewView(item.name(), entry.getDropChancePercent()));
		}
		return List.copyOf(preview);
	}

	private List<CombatActionPreviewView> buildActionPreviews(
			CombatSessionEntity session,
			Combat2State state,
			boolean stunned,
			boolean potionAvailable,
			List<CombatTechniqueOptionView> techniques) {
		List<CombatActionPreviewView> previews = new ArrayList<>();
		previews.add(corePreview(state, CombatAction.QUICK_ATTACK, session, stunned, potionAvailable));
		previews.add(corePreview(state, CombatAction.HEAVY_ATTACK, session, stunned, potionAvailable));
		previews.add(corePreview(state, CombatAction.PRECISE_ATTACK, session, stunned, potionAvailable));
		previews.add(corePreview(state, CombatAction.DEFEND, session, stunned, potionAvailable));
		previews.add(corePreview(state, CombatAction.USE_POTION, session, stunned, potionAvailable));
		previews.add(corePreview(state, CombatAction.RETREAT, session, stunned, potionAvailable));
		for (CombatTechniqueOptionView technique : techniques) {
			TechniqueEffectSpec spec = state.techniqueSpecs().get(technique.code());
			Integer hit = spec == null
					? null
					: CombatEngine.previewPlayerHitChance(state, CombatAction.USE_TECHNIQUE, spec);
			previews.add(new CombatActionPreviewView(
					CombatAction.USE_TECHNIQUE,
					technique.code(),
					technique.name(),
					technique.description(),
					technique.staminaCost(),
					hit,
					technique.disabledReason()));
		}
		return List.copyOf(previews);
	}

	private CombatActionPreviewView corePreview(
			Combat2State state,
			CombatAction action,
			CombatSessionEntity session,
			boolean stunned,
			boolean potionAvailable) {
		int cost = CombatV2Balance.reducedStaminaCost(
				ActionCombatBalance.staminaCost(action), session.getStaminaCostReduction());
		Integer hit = ActionCombatBalance.isAttack(action)
				? CombatEngine.previewPlayerHitChance(state, action, null)
				: null;
		return new CombatActionPreviewView(
				action,
				null,
				coreActionName(action),
				coreActionDescription(action),
				cost,
				hit,
				coreDisabledReason(action, stunned, potionAvailable, session.getPlayerStamina(), cost));
	}

	private List<CombatActionPreviewView> phase1ActionPreviews(
			CoreActionCostsView costs,
			boolean stunned,
			boolean potionAvailable,
			int playerStamina) {
		List<CombatActionPreviewView> previews = new ArrayList<>();
		previews.add(phase1Preview(CombatAction.QUICK_ATTACK, costs.quickAttack(), stunned, potionAvailable, playerStamina));
		previews.add(phase1Preview(CombatAction.HEAVY_ATTACK, costs.heavyAttack(), stunned, potionAvailable, playerStamina));
		previews.add(phase1Preview(CombatAction.PRECISE_ATTACK, costs.preciseAttack(), stunned, potionAvailable, playerStamina));
		previews.add(phase1Preview(CombatAction.DEFEND, 0, stunned, potionAvailable, playerStamina));
		previews.add(phase1Preview(CombatAction.USE_POTION, 0, stunned, potionAvailable, playerStamina));
		previews.add(phase1Preview(CombatAction.RETREAT, 0, stunned, potionAvailable, playerStamina));
		return List.copyOf(previews);
	}

	private static CombatActionPreviewView phase1Preview(
			CombatAction action,
			int cost,
			boolean stunned,
			boolean potionAvailable,
			int playerStamina) {
		return new CombatActionPreviewView(
				action,
				null,
				coreActionName(action),
				coreActionDescription(action),
				cost,
				null,
				coreDisabledReason(action, stunned, potionAvailable, playerStamina, cost));
	}

	private static String coreDisabledReason(
			CombatAction action,
			boolean stunned,
			boolean potionAvailable,
			int playerStamina,
			int cost) {
		if (stunned) {
			return "STUNNED";
		}
		if (action == CombatAction.USE_POTION && !potionAvailable) {
			return "NO_POTION";
		}
		if (cost > 0 && playerStamina < cost) {
			return "INSUFFICIENT_STAMINA";
		}
		return null;
	}

	private static String coreActionName(CombatAction action) {
		return switch (action) {
			case QUICK_ATTACK -> "Quick Attack";
			case HEAVY_ATTACK -> "Heavy Attack";
			case PRECISE_ATTACK -> "Precise Attack";
			case DEFEND -> "Defend";
			case USE_POTION -> "Use Potion";
			case RETREAT -> "Flee Encounter";
			case USE_TECHNIQUE -> "Technique";
		};
	}

	private static String coreActionDescription(CombatAction action) {
		return switch (action) {
			case QUICK_ATTACK -> "A reliable strike with balanced stamina cost.";
			case HEAVY_ATTACK -> "A slower blow that hits harder and is easier to dodge.";
			case PRECISE_ATTACK -> "Aim for a weak point. Lower damage, higher accuracy.";
			case DEFEND -> "Guard yourself and recover stamina.";
			case USE_POTION -> "Drink a healing potion.";
			case RETREAT -> "Attempt to leave combat.";
			case USE_TECHNIQUE -> "Use an equipped technique.";
		};
	}

	private static String enemyIntentLabel(
			com.example.game.combat.domain.EnemyActionKind kind,
			com.example.game.combat.domain.StatusType signatureStatus) {
		return switch (kind) {
			case BASIC_ATTACK -> "Basic Attack";
			case HEAVY_ATTACK -> "Heavy Attack";
			case DEFEND -> "Defend";
			case STATUS_ATTACK -> signatureStatus == null
					? "Status Attack"
					: signatureStatus.name().charAt(0)
							+ signatureStatus.name().substring(1).toLowerCase().replace('_', ' ')
							+ " Attack";
		};
	}

	private List<CombatTechniqueOptionView> techniqueOptions(CombatSessionEntity session, boolean stunned) {
		List<String> codes = parseTechniqueCodes(session.getTechniqueCodes());
		if (codes.isEmpty()) {
			return List.of();
		}
		CombatTechniqueCatalog catalog = combatTechniqueCatalogService.load();
		List<CombatTechniqueOptionView> options = new java.util.ArrayList<>();
		for (String code : codes) {
			CombatTechniqueDefinition definition = catalog.require(code);
			int cost = CombatV2Balance.reducedStaminaCost(
					definition.effect().staminaCost(), session.getStaminaCostReduction());
			String disabled = null;
			if (stunned) {
				disabled = "STUNNED";
			}
			else if (session.getPlayerStamina() < cost) {
				disabled = "INSUFFICIENT_STAMINA";
			}
			options.add(new CombatTechniqueOptionView(
					definition.code(),
					definition.displayName(),
					definition.description(),
					cost,
					disabled));
		}
		return List.copyOf(options);
	}

	private List<StatusInstance> loadStatuses(UUID sessionId, CombatantSide side) {
		return combatStatusEffectRepository.findBySessionIdAndTarget(sessionId, side).stream()
				.map(row -> new StatusInstance(row.getStatusType(), row.getStacks(), row.getRemainingRounds()))
				.toList();
	}

	private List<CombatStatusView> toStatusViews(List<StatusInstance> statuses) {
		return statuses.stream()
				.map(status -> new CombatStatusView(status.type(), status.stacks(), status.remainingRounds()))
				.toList();
	}

	private void replaceStatuses(
			UUID sessionId,
			List<StatusInstance> playerStatuses,
			List<StatusInstance> enemyStatuses) {
		combatStatusEffectRepository.deleteBySessionId(sessionId);
		combatStatusEffectRepository.flush();
		List<CombatStatusEffectEntity> rows = new java.util.ArrayList<>();
		for (StatusInstance status : playerStatuses) {
			rows.add(new CombatStatusEffectEntity(
					UUID.randomUUID(), sessionId, CombatantSide.PLAYER, status.type(), status.stacks(),
					status.remainingRounds()));
		}
		for (StatusInstance status : enemyStatuses) {
			rows.add(new CombatStatusEffectEntity(
					UUID.randomUUID(), sessionId, CombatantSide.ENEMY, status.type(), status.stacks(),
					status.remainingRounds()));
		}
		if (!rows.isEmpty()) {
			combatStatusEffectRepository.saveAll(rows);
			combatStatusEffectRepository.flush();
		}
	}

	private static List<String> parseTechniqueCodes(String stored) {
		if (stored == null || stored.isBlank()) {
			return List.of();
		}
		return java.util.Arrays.stream(stored.split(","))
				.map(String::trim)
				.filter(code -> !code.isEmpty())
				.toList();
	}
}
