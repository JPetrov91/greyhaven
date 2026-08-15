package com.example.game.quest.application;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.activity.application.ActivityApplicationService;
import com.example.game.activity.domain.ActivityType;
import com.example.game.inventory.application.InventoryApplicationService;
import com.example.game.quest.domain.AcquireItemObjectiveHandler;
import com.example.game.quest.domain.CollectObjectiveHandler;
import com.example.game.quest.domain.CompleteDungeonObjectiveHandler;
import com.example.game.quest.domain.CompleteExpeditionObjectiveHandler;
import com.example.game.quest.domain.CraftItemObjectiveHandler;
import com.example.game.quest.domain.ItemQuantitySource;
import com.example.game.quest.domain.KillObjectiveHandler;
import com.example.game.quest.domain.ObjectiveHandler;
import com.example.game.quest.domain.ObjectiveProgress;
import com.example.game.quest.domain.QuestObjectiveSpec;
import com.example.game.quest.domain.QuestProgressFact;
import com.example.game.quest.domain.QuestProgressSourceKind;
import com.example.game.quest.domain.QuestStatus;
import com.example.game.quest.domain.TalkObjectiveHandler;
import com.example.game.quest.domain.VisitLocationObjectiveHandler;
import com.example.game.quest.domain.WinArenaMatchObjectiveHandler;
import com.example.game.quest.infrastructure.CharacterQuestEntity;
import com.example.game.quest.infrastructure.CharacterQuestObjectiveEntity;
import com.example.game.quest.infrastructure.CharacterQuestObjectiveRepository;
import com.example.game.quest.infrastructure.CharacterQuestProgressSourceEntity;
import com.example.game.quest.infrastructure.CharacterQuestProgressSourceRepository;
import com.example.game.quest.infrastructure.CharacterQuestRepository;
import com.example.game.quest.infrastructure.QuestDefinitionEntity;
import com.example.game.quest.infrastructure.QuestObjectiveDefinitionEntity;

@Service
public class QuestProgressService implements QuestProgressSink {

	private static final EnumSet<QuestStatus> OPEN = EnumSet.of(QuestStatus.ACTIVE, QuestStatus.READY_TO_TURN_IN);

	private final CharacterQuestRepository characterQuestRepository;
	private final CharacterQuestObjectiveRepository characterQuestObjectiveRepository;
	private final CharacterQuestProgressSourceRepository progressSourceRepository;
	private final QuestCatalog questCatalog;
	private final InventoryApplicationService inventoryApplicationService;
	private final ActivityApplicationService activityApplicationService;
	private final QuestRewardService questRewardService;
	private final Clock clock;
	private final List<ObjectiveHandler> handlers = List.of(
			new KillObjectiveHandler(),
			new VisitLocationObjectiveHandler(),
			new TalkObjectiveHandler(),
			new AcquireItemObjectiveHandler(),
			new CollectObjectiveHandler(),
			new CraftItemObjectiveHandler(),
			new CompleteExpeditionObjectiveHandler(),
			new CompleteDungeonObjectiveHandler(),
			new WinArenaMatchObjectiveHandler());

	public QuestProgressService(
			CharacterQuestRepository characterQuestRepository,
			CharacterQuestObjectiveRepository characterQuestObjectiveRepository,
			CharacterQuestProgressSourceRepository progressSourceRepository,
			QuestCatalog questCatalog,
			InventoryApplicationService inventoryApplicationService,
			ActivityApplicationService activityApplicationService,
			@Lazy QuestRewardService questRewardService,
			Clock clock) {
		this.characterQuestRepository = characterQuestRepository;
		this.characterQuestObjectiveRepository = characterQuestObjectiveRepository;
		this.progressSourceRepository = progressSourceRepository;
		this.questCatalog = questCatalog;
		this.inventoryApplicationService = inventoryApplicationService;
		this.activityApplicationService = activityApplicationService;
		this.questRewardService = questRewardService;
		this.clock = clock;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void notify(UUID characterId, QuestProgressFact fact) {
		if (fact.dedupeKind() != null && fact.dedupeId() != null && !recordSource(characterId, fact.dedupeKind(), fact.dedupeId())) {
			return;
		}
		List<CharacterQuestEntity> open = characterQuestRepository.findWithLockByCharacterIdAndStatusIn(
				characterId,
				OPEN);
		if (open.isEmpty()) {
			return;
		}
		List<UUID> questIds = open.stream().map(CharacterQuestEntity::getQuestId).toList();
		Map<UUID, QuestObjectiveDefinitionEntity> definitions = questCatalog.objectivesById(questIds);
		Map<UUID, List<CharacterQuestObjectiveEntity>> progressByQuest = characterQuestObjectiveRepository
				.findByCharacterQuestIdIn(open.stream().map(CharacterQuestEntity::getId).toList())
				.stream()
				.collect(java.util.stream.Collectors.groupingBy(CharacterQuestObjectiveEntity::getCharacterQuestId));
		ItemQuantitySource items = code -> inventoryApplicationService.unreservedQuantityByCode(characterId, code);
		Instant now = Instant.now(clock);
		for (CharacterQuestEntity characterQuest : open) {
			if (characterQuest.getStatus() == QuestStatus.COMPLETED) {
				continue;
			}
			boolean changed = false;
			List<CharacterQuestObjectiveEntity> rows = progressByQuest.getOrDefault(characterQuest.getId(), List.of());
			for (CharacterQuestObjectiveEntity row : rows) {
				QuestObjectiveDefinitionEntity definition = definitions.get(row.getObjectiveId());
				if (definition == null) {
					continue;
				}
				ObjectiveHandler handler = handlerFor(definition.getType());
				ObjectiveProgress progress = new ObjectiveProgress(row.getCurrentAmount(), row.isCompleted());
				QuestObjectiveSpec spec = new QuestObjectiveSpec(
						definition.getType(),
						definition.getTargetCode(),
						definition.getRequiredAmount(),
						definition.isConsumeOnTurnIn());
				if (handler.apply(spec, progress, fact, items)) {
					row.apply(progress.currentAmount(), progress.completed());
					characterQuestObjectiveRepository.save(row);
					changed = true;
					if (progress.completed()) {
						activityApplicationService.record(
								characterId,
								ActivityType.QUEST_OBJECTIVE,
								definition.getDisplayText() + " complete.");
					}
				}
			}
			if (changed && rows.stream().allMatch(CharacterQuestObjectiveEntity::isCompleted)) {
				QuestDefinitionEntity quest = questCatalog.requireById(characterQuest.getQuestId());
				if (quest.getTurnInNpcCode() == null || quest.getTurnInNpcCode().isBlank()) {
					if (questRewardService.hasRoomForItemRewards(characterId, quest)) {
						questRewardService.completeAndGrant(characterQuest, quest);
					}
					else {
						characterQuest.markReady(now);
						characterQuestRepository.saveAndFlush(characterQuest);
					}
				}
				else {
					characterQuest.markReady(now);
					characterQuestRepository.saveAndFlush(characterQuest);
				}
			}
			else if (changed && characterQuest.getStatus() == QuestStatus.READY_TO_TURN_IN) {
				characterQuest.markActive();
				characterQuestRepository.saveAndFlush(characterQuest);
			}
		}
	}

	private boolean recordSource(UUID characterId, QuestProgressSourceKind kind, String sourceId) {
		if (progressSourceRepository.existsByCharacterIdAndSourceKindAndSourceId(characterId, kind, sourceId)) {
			return false;
		}
		try {
			progressSourceRepository.saveAndFlush(new CharacterQuestProgressSourceEntity(
					UUID.randomUUID(),
					characterId,
					kind,
					sourceId));
			return true;
		}
		catch (DataIntegrityViolationException exception) {
			return false;
		}
	}

	private ObjectiveHandler handlerFor(com.example.game.quest.domain.QuestObjectiveType type) {
		return handlers.stream()
				.filter(handler -> handler.supports(type))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("No handler for " + type));
	}
}
