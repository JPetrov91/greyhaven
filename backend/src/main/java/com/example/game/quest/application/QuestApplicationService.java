package com.example.game.quest.application;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.activity.application.ActivityApplicationService;
import com.example.game.activity.domain.ActivityType;
import com.example.game.character.application.CharacterLocationService;
import com.example.game.character.application.CharacterLocationView;
import com.example.game.character.application.CharacterUnlockQuery;
import com.example.game.character.application.CharacterVitalsService;
import com.example.game.character.application.CharacterVitalsView;
import com.example.game.item.application.ItemCatalogService;
import com.example.game.item.application.ItemDefinitionView;
import com.example.game.quest.domain.QuestAvailability;
import com.example.game.quest.domain.QuestListStatus;
import com.example.game.quest.domain.QuestRewardKind;
import com.example.game.quest.domain.QuestStatus;
import com.example.game.quest.infrastructure.CharacterQuestEntity;
import com.example.game.quest.infrastructure.CharacterQuestObjectiveEntity;
import com.example.game.quest.infrastructure.CharacterQuestObjectiveRepository;
import com.example.game.quest.infrastructure.CharacterQuestRepository;
import com.example.game.quest.infrastructure.CharacterQuestTrackEntity;
import com.example.game.quest.infrastructure.CharacterQuestTrackRepository;
import com.example.game.quest.infrastructure.CharacterUnlockEntity;
import com.example.game.quest.infrastructure.CharacterUnlockRepository;
import com.example.game.quest.infrastructure.QuestDefinitionEntity;
import com.example.game.quest.infrastructure.QuestObjectiveDefinitionEntity;
import com.example.game.quest.infrastructure.QuestRewardDefinitionEntity;
import com.example.game.world.infrastructure.LocationEntity;
import com.example.game.world.infrastructure.LocationRepository;
import com.example.game.world.infrastructure.NpcDefinitionEntity;
import com.example.game.world.infrastructure.NpcDefinitionRepository;

@Service
public class QuestApplicationService implements CharacterUnlockQuery {

	static final int MAX_TRACKED = 3;

	private final CharacterVitalsService characterVitalsService;
	private final CharacterLocationService characterLocationService;
	private final LocationRepository locationRepository;
	private final NpcDefinitionRepository npcDefinitionRepository;
	private final ItemCatalogService itemCatalogService;
	private final QuestCatalog questCatalog;
	private final CharacterQuestRepository characterQuestRepository;
	private final CharacterQuestObjectiveRepository characterQuestObjectiveRepository;
	private final CharacterQuestTrackRepository characterQuestTrackRepository;
	private final CharacterUnlockRepository characterUnlockRepository;
	private final QuestProgressSink questProgressSink;
	private final QuestRewardService questRewardService;
	private final ActivityApplicationService activityApplicationService;
	private final Clock clock;

	public QuestApplicationService(
			CharacterVitalsService characterVitalsService,
			CharacterLocationService characterLocationService,
			LocationRepository locationRepository,
			NpcDefinitionRepository npcDefinitionRepository,
			ItemCatalogService itemCatalogService,
			QuestCatalog questCatalog,
			CharacterQuestRepository characterQuestRepository,
			CharacterQuestObjectiveRepository characterQuestObjectiveRepository,
			CharacterQuestTrackRepository characterQuestTrackRepository,
			CharacterUnlockRepository characterUnlockRepository,
			QuestProgressSink questProgressSink,
			QuestRewardService questRewardService,
			ActivityApplicationService activityApplicationService,
			Clock clock) {
		this.characterVitalsService = characterVitalsService;
		this.characterLocationService = characterLocationService;
		this.locationRepository = locationRepository;
		this.npcDefinitionRepository = npcDefinitionRepository;
		this.itemCatalogService = itemCatalogService;
		this.questCatalog = questCatalog;
		this.characterQuestRepository = characterQuestRepository;
		this.characterQuestObjectiveRepository = characterQuestObjectiveRepository;
		this.characterQuestTrackRepository = characterQuestTrackRepository;
		this.characterUnlockRepository = characterUnlockRepository;
		this.questProgressSink = questProgressSink;
		this.questRewardService = questRewardService;
		this.activityApplicationService = activityApplicationService;
		this.clock = clock;
	}

	@Override
	@Transactional(readOnly = true)
	public List<String> unlockCodesOf(UUID characterId) {
		return characterUnlockRepository.findByCharacterIdOrderByUnlockCodeAsc(characterId).stream()
				.map(CharacterUnlockEntity::getUnlockCode)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<QuestView> list(UUID accountId) {
		CharacterVitalsView vitals = characterVitalsService.vitalsOf(accountId);
		return listForCharacter(vitals);
	}

	@Transactional(readOnly = true)
	public QuestView get(UUID accountId, String code) {
		CharacterVitalsView vitals = characterVitalsService.vitalsOf(accountId);
		return listForCharacter(vitals).stream()
				.filter(quest -> quest.code().equals(code))
				.findFirst()
				.orElseThrow(QuestErrors::questNotFound);
	}

	@Transactional
	public QuestView accept(UUID accountId, String code) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		QuestDefinitionEntity definition = questCatalog.requireByCode(code);
		CharacterLocationView location = characterLocationService.lockLocationOf(accountId);
		if (!isAvailable(vitals, definition)) {
			CharacterQuestEntity existing = characterQuestRepository
					.findByCharacterIdAndQuestId(vitals.characterId(), definition.getId())
					.orElse(null);
			if (existing != null) {
				throw QuestErrors.questAlreadyAccepted();
			}
			throw QuestErrors.questNotAvailable();
		}
		assertAtNpc(location.currentLocationId(), definition.getStartNpcCode());
		CharacterQuestEntity characterQuest = characterQuestRepository
				.findWithLockByCharacterIdAndQuestId(vitals.characterId(), definition.getId())
				.orElse(null);
		if (characterQuest != null && characterQuest.getStatus() == QuestStatus.COMPLETED && definition.isRepeatable()) {
			characterQuest.reopen(Instant.now(clock));
			characterQuestRepository.saveAndFlush(characterQuest);
			for (CharacterQuestObjectiveEntity row : characterQuestObjectiveRepository.findByCharacterQuestId(characterQuest.getId())) {
				row.apply(0, false);
				characterQuestObjectiveRepository.save(row);
			}
			characterQuestObjectiveRepository.flush();
		}
		else if (characterQuest != null) {
			throw QuestErrors.questAlreadyAccepted();
		}
		else {
			try {
				characterQuest = characterQuestRepository.saveAndFlush(new CharacterQuestEntity(
						UUID.randomUUID(),
						vitals.characterId(),
						definition.getId(),
						Instant.now(clock)));
			}
			catch (DataIntegrityViolationException exception) {
				throw QuestErrors.questAlreadyAccepted();
			}
			for (QuestObjectiveDefinitionEntity objective : questCatalog.objectivesOf(definition.getId())) {
				characterQuestObjectiveRepository.save(new CharacterQuestObjectiveEntity(
						UUID.randomUUID(),
						characterQuest.getId(),
						objective.getId()));
			}
			characterQuestObjectiveRepository.flush();
		}
		if (characterQuestTrackRepository.findByCharacterIdAndQuestId(vitals.characterId(), definition.getId()).isEmpty()
				&& characterQuestTrackRepository.countByCharacterId(vitals.characterId()) < MAX_TRACKED) {
			characterQuestTrackRepository.save(new CharacterQuestTrackEntity(
					UUID.randomUUID(),
					vitals.characterId(),
					definition.getId(),
					(int) characterQuestTrackRepository.countByCharacterId(vitals.characterId()) + 1));
		}
		activityApplicationService.record(
				vitals.characterId(),
				ActivityType.QUEST_ACCEPTED,
				"Accepted: " + definition.getName() + ".");
		String locationCode = locationRepository.findById(location.currentLocationId())
				.map(LocationEntity::getCode)
				.orElse(null);
		if (locationCode != null) {
			questProgressSink.onLocationVisited(vitals.characterId(), locationCode);
		}
		questProgressSink.onInventoryChanged(vitals.characterId());
		return requireView(vitals, code);
	}

	@Transactional
	public QuestView turnIn(UUID accountId, String code) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		QuestDefinitionEntity definition = questCatalog.requireByCode(code);
		CharacterQuestEntity characterQuest = characterQuestRepository
				.findWithLockByCharacterIdAndQuestId(vitals.characterId(), definition.getId())
				.orElseThrow(QuestErrors::questNotActive);
		if (characterQuest.isRewardsApplied()) {
			return requireView(vitals, code);
		}
		if (!questRewardService.readyToComplete(vitals.characterId(), characterQuest, definition)) {
			throw QuestErrors.questNotReady();
		}
		CharacterLocationView location = characterLocationService.lockLocationOf(accountId);
		assertAtNpc(location.currentLocationId(), definition.getTurnInNpcCode());
		questRewardService.completeAndGrant(characterQuest, definition);
		return requireView(vitals, code);
	}

	@Transactional
	public QuestView track(UUID accountId, String code) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		QuestDefinitionEntity definition = questCatalog.requireByCode(code);
		characterQuestRepository.findByCharacterIdAndQuestId(vitals.characterId(), definition.getId())
				.filter(quest -> quest.getStatus() != QuestStatus.COMPLETED)
				.orElseThrow(QuestErrors::questNotActive);
		if (characterQuestTrackRepository.findByCharacterIdAndQuestId(vitals.characterId(), definition.getId()).isPresent()) {
			return requireView(vitals, code);
		}
		if (characterQuestTrackRepository.countByCharacterId(vitals.characterId()) >= MAX_TRACKED) {
			throw QuestErrors.trackLimit();
		}
		characterQuestTrackRepository.save(new CharacterQuestTrackEntity(
				UUID.randomUUID(),
				vitals.characterId(),
				definition.getId(),
				(int) characterQuestTrackRepository.countByCharacterId(vitals.characterId()) + 1));
		return requireView(vitals, code);
	}

	@Transactional
	public QuestView untrack(UUID accountId, String code) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		QuestDefinitionEntity definition = questCatalog.requireByCode(code);
		characterQuestTrackRepository.findByCharacterIdAndQuestId(vitals.characterId(), definition.getId())
				.ifPresent(characterQuestTrackRepository::delete);
		return requireView(vitals, code);
	}

	@Transactional(readOnly = true)
	public List<QuestView> tracked(UUID accountId) {
		return list(accountId).stream().filter(QuestView::tracked).toList();
	}

	private QuestView requireView(CharacterVitalsView vitals, String code) {
		return listForCharacter(vitals).stream()
				.filter(quest -> quest.code().equals(code))
				.findFirst()
				.orElseThrow(QuestErrors::questNotFound);
	}

	private List<QuestView> listForCharacter(CharacterVitalsView vitals) {
		List<QuestDefinitionEntity> definitions = questCatalog.allQuests();
		List<UUID> questIds = definitions.stream().map(QuestDefinitionEntity::getId).toList();
		Map<UUID, List<QuestObjectiveDefinitionEntity>> objectives = questCatalog.objectivesByQuestId(questIds);
		Map<UUID, List<QuestRewardDefinitionEntity>> rewards = questCatalog.rewardsByQuestId(questIds);
		Map<UUID, CharacterQuestEntity> states = characterQuestRepository.findByCharacterId(vitals.characterId())
				.stream()
				.collect(Collectors.toMap(CharacterQuestEntity::getQuestId, quest -> quest));
		Map<UUID, List<CharacterQuestObjectiveEntity>> progress = characterQuestObjectiveRepository
				.findByCharacterQuestIdIn(states.values().stream().map(CharacterQuestEntity::getId).toList())
				.stream()
				.collect(Collectors.groupingBy(CharacterQuestObjectiveEntity::getCharacterQuestId));
		Set<UUID> tracked = characterQuestTrackRepository.findByCharacterIdOrderBySortOrderAsc(vitals.characterId())
				.stream()
				.map(CharacterQuestTrackEntity::getQuestId)
				.collect(Collectors.toSet());
		Set<String> completedCodes = new HashSet<>();
		for (QuestDefinitionEntity definition : definitions) {
			CharacterQuestEntity state = states.get(definition.getId());
			if (state != null && (state.getStatus() == QuestStatus.COMPLETED || state.getCompletedAt() != null)) {
				completedCodes.add(definition.getCode());
			}
		}
		Map<String, NpcDefinitionEntity> npcs = npcDefinitionRepository.findAllByOrderBySortOrderAsc().stream()
				.collect(Collectors.toMap(NpcDefinitionEntity::getCode, npc -> npc, (left, right) -> left));
		Map<String, String> questNames = definitions.stream()
				.collect(Collectors.toMap(QuestDefinitionEntity::getCode, QuestDefinitionEntity::getName, (left, right) -> left));
		Set<String> itemCodes = rewards.values().stream()
				.flatMap(List::stream)
				.map(QuestRewardDefinitionEntity::getItemCode)
				.filter(code -> code != null && !code.isBlank())
				.collect(Collectors.toSet());
		Map<String, ItemDefinitionView> items = itemCatalogService.findByCodes(itemCodes);
		List<QuestView> views = new ArrayList<>();
		for (QuestDefinitionEntity definition : definitions) {
			CharacterQuestEntity state = states.get(definition.getId());
			QuestListStatus status;
			if (state == null) {
				if (!QuestAvailability.isAvailable(
						vitals.level(),
						definition.getMinLevel(),
						definition.getPrerequisiteQuestCode(),
						completedCodes,
						false)) {
					continue;
				}
				status = QuestListStatus.AVAILABLE;
			}
			else if (state.getStatus() == QuestStatus.COMPLETED) {
				status = QuestListStatus.COMPLETED;
			}
			else if (state.getStatus() == QuestStatus.READY_TO_TURN_IN) {
				status = QuestListStatus.READY_TO_TURN_IN;
			}
			else {
				status = QuestListStatus.ACTIVE;
			}
			views.add(toView(definition, status, state, objectives, rewards, progress, tracked, npcs, questNames, items));
		}
		return views;
	}

	private boolean isAvailable(CharacterVitalsView vitals, QuestDefinitionEntity definition) {
		Set<String> completed = characterQuestRepository.findByCharacterId(vitals.characterId()).stream()
				.filter(quest -> quest.getStatus() == QuestStatus.COMPLETED || quest.getCompletedAt() != null)
				.map(quest -> questCatalog.requireById(quest.getQuestId()).getCode())
				.collect(Collectors.toSet());
		CharacterQuestEntity existing = characterQuestRepository
				.findByCharacterIdAndQuestId(vitals.characterId(), definition.getId())
				.orElse(null);
		boolean blocked = existing != null
				&& (existing.getStatus() != QuestStatus.COMPLETED || !definition.isRepeatable());
		return QuestAvailability.isAvailable(
				vitals.level(),
				definition.getMinLevel(),
				definition.getPrerequisiteQuestCode(),
				completed,
				blocked);
	}

	private void assertAtNpc(UUID locationId, String npcCode) {
		if (npcCode == null || npcCode.isBlank()) {
			return;
		}
		NpcDefinitionEntity npc = npcDefinitionRepository.findByCode(npcCode).orElseThrow(QuestErrors::npcNotFound);
		LocationEntity location = locationRepository.findById(locationId).orElseThrow(QuestErrors::wrongLocation);
		if (!npc.getLocationCode().equals(location.getCode())) {
			throw QuestErrors.wrongLocation();
		}
	}

	private static QuestView toView(
			QuestDefinitionEntity definition,
			QuestListStatus status,
			CharacterQuestEntity state,
			Map<UUID, List<QuestObjectiveDefinitionEntity>> objectives,
			Map<UUID, List<QuestRewardDefinitionEntity>> rewards,
			Map<UUID, List<CharacterQuestObjectiveEntity>> progress,
			Set<UUID> tracked,
			Map<String, NpcDefinitionEntity> npcs,
			Map<String, String> questNames,
			Map<String, ItemDefinitionView> items) {
		Map<UUID, CharacterQuestObjectiveEntity> progressByObjective = state == null
				? Map.of()
				: progress.getOrDefault(state.getId(), List.of()).stream()
						.collect(Collectors.toMap(CharacterQuestObjectiveEntity::getObjectiveId, row -> row));
		List<QuestObjectiveView> objectiveViews = objectives.getOrDefault(definition.getId(), List.of()).stream()
				.map(objective -> {
					CharacterQuestObjectiveEntity row = progressByObjective.get(objective.getId());
					return QuestObjectiveView.of(
							objective.getType(),
							objective.getTargetCode(),
							objective.getRequiredAmount(),
							row == null ? 0 : row.getCurrentAmount(),
							row != null && row.isCompleted(),
							objective.getDisplayText(),
							objective.isConsumeOnTurnIn());
				})
				.toList();
		List<QuestRewardView> rewardViews = rewards.getOrDefault(definition.getId(), List.of()).stream()
				.map(reward -> {
					String itemName = reward.getItemCode() == null
							? null
							: items.containsKey(reward.getItemCode()) ? items.get(reward.getItemCode()).name() : reward.getItemCode();
					return QuestRewardView.of(
							reward.getKind(),
							reward.getAmount(),
							reward.getItemCode(),
							itemName,
							reward.getUnlockCode());
				})
				.toList();
		List<String> unlocks = status != QuestListStatus.COMPLETED
				? List.of()
				: rewards.getOrDefault(definition.getId(), List.of()).stream()
						.filter(reward -> reward.getKind() == QuestRewardKind.UNLOCK && reward.getUnlockCode() != null)
						.map(QuestRewardDefinitionEntity::getUnlockCode)
						.toList();
		return QuestView.of(
				definition.getCode(),
				definition.getName(),
				definition.getDescription(),
				definition.getCategory(),
				status,
				definition.getRecommendedLevel(),
				definition.getStartNpcCode(),
				npcName(npcs, definition.getStartNpcCode()),
				definition.getTurnInNpcCode(),
				npcName(npcs, definition.getTurnInNpcCode()),
				definition.getNextQuestCode(),
				definition.getNextQuestCode() == null ? null : questNames.get(definition.getNextQuestCode()),
				definition.isRepeatable(),
				tracked.contains(definition.getId()),
				objectiveViews,
				rewardViews,
				unlocks);
	}

	private static String npcName(Map<String, NpcDefinitionEntity> npcs, String code) {
		if (code == null || code.isBlank()) {
			return null;
		}
		NpcDefinitionEntity npc = npcs.get(code);
		return npc == null ? code : npc.getName();
	}
}
