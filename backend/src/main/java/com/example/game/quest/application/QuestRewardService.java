package com.example.game.quest.application;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.activity.application.ActivityApplicationService;
import com.example.game.activity.domain.ActivityType;
import com.example.game.character.application.CharacterVitalsService;
import com.example.game.character.domain.XpSource;
import com.example.game.inventory.application.InventoryApplicationService;
import com.example.game.inventory.application.InventoryFullException;
import com.example.game.inventory.domain.InventoryBalance;
import com.example.game.item.application.ItemCatalogService;
import com.example.game.item.application.ItemDefinitionView;
import com.example.game.quest.domain.QuestObjectiveType;
import com.example.game.quest.domain.QuestRewardKind;
import com.example.game.quest.infrastructure.CharacterQuestEntity;
import com.example.game.quest.infrastructure.CharacterQuestObjectiveEntity;
import com.example.game.quest.infrastructure.CharacterQuestObjectiveRepository;
import com.example.game.quest.infrastructure.CharacterQuestRepository;
import com.example.game.quest.infrastructure.CharacterUnlockEntity;
import com.example.game.quest.infrastructure.CharacterUnlockRepository;
import com.example.game.quest.infrastructure.QuestDefinitionEntity;
import com.example.game.quest.infrastructure.QuestObjectiveDefinitionEntity;
import com.example.game.quest.infrastructure.QuestRewardDefinitionEntity;
import com.example.game.telemetry.application.GameTelemetry;
import com.example.game.telemetry.application.GameTelemetryRecorder;
import com.example.game.telemetry.domain.GoldCreateReason;
import com.example.game.telemetry.domain.ItemCreateSource;

@Service
public class QuestRewardService {

	private final CharacterQuestRepository characterQuestRepository;
	private final CharacterQuestObjectiveRepository characterQuestObjectiveRepository;
	private final QuestCatalog questCatalog;
	private final CharacterVitalsService characterVitalsService;
	private final InventoryApplicationService inventoryApplicationService;
	private final ItemCatalogService itemCatalogService;
	private final CharacterUnlockRepository characterUnlockRepository;
	private final ActivityApplicationService activityApplicationService;
	private final GameTelemetryRecorder gameTelemetryRecorder;
	private final Clock clock;

	public QuestRewardService(
			CharacterQuestRepository characterQuestRepository,
			CharacterQuestObjectiveRepository characterQuestObjectiveRepository,
			QuestCatalog questCatalog,
			CharacterVitalsService characterVitalsService,
			InventoryApplicationService inventoryApplicationService,
			ItemCatalogService itemCatalogService,
			CharacterUnlockRepository characterUnlockRepository,
			ActivityApplicationService activityApplicationService,
			GameTelemetryRecorder gameTelemetryRecorder,
			Clock clock) {
		this.characterQuestRepository = characterQuestRepository;
		this.characterQuestObjectiveRepository = characterQuestObjectiveRepository;
		this.questCatalog = questCatalog;
		this.characterVitalsService = characterVitalsService;
		this.inventoryApplicationService = inventoryApplicationService;
		this.itemCatalogService = itemCatalogService;
		this.characterUnlockRepository = characterUnlockRepository;
		this.activityApplicationService = activityApplicationService;
		this.gameTelemetryRecorder = gameTelemetryRecorder;
		this.clock = clock;
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public List<String> completeAndGrant(CharacterQuestEntity characterQuest, QuestDefinitionEntity definition) {
		if (characterQuest.isRewardsApplied()) {
			return characterUnlockRepository.findByCharacterIdOrderByUnlockCodeAsc(characterQuest.getCharacterId())
					.stream()
					.map(CharacterUnlockEntity::getUnlockCode)
					.toList();
		}
		characterVitalsService.lockVitalsByCharacterId(characterQuest.getCharacterId());
		if (!hasRoomForItemRewards(characterQuest.getCharacterId(), definition)) {
			throw new InventoryFullException("Your pack is full.");
		}
		if (!readyToComplete(characterQuest.getCharacterId(), characterQuest, definition)) {
			throw QuestErrors.questNotReady();
		}
		consumeCollectItems(characterQuest, definition);
		List<QuestRewardDefinitionEntity> rewards = questCatalog.rewardsOf(definition.getId());
		int xp = 0;
		int gold = 0;
		List<QuestRewardDefinitionEntity> itemRewards = new ArrayList<>();
		List<String> unlocks = new ArrayList<>();
		for (QuestRewardDefinitionEntity reward : rewards) {
			if (reward.getKind() == QuestRewardKind.XP) {
				xp += reward.getAmount();
			}
			else if (reward.getKind() == QuestRewardKind.GOLD) {
				gold += reward.getAmount();
			}
			else if (reward.getKind() == QuestRewardKind.ITEM) {
				itemRewards.add(reward);
			}
			else if (reward.getKind() == QuestRewardKind.UNLOCK && reward.getUnlockCode() != null) {
				unlocks.add(reward.getUnlockCode());
			}
		}
		if (xp > 0 || gold > 0) {
			characterVitalsService.grantRewards(
					characterQuest.getCharacterId(),
					xp,
					gold,
					XpSource.QUEST,
					GoldCreateReason.QUEST);
		}
		Instant now = Instant.now(clock);
		for (QuestRewardDefinitionEntity reward : itemRewards) {
			grantItem(characterQuest.getCharacterId(), reward);
		}
		for (String unlock : unlocks) {
			if (!characterUnlockRepository.existsByCharacterIdAndUnlockCode(characterQuest.getCharacterId(), unlock)) {
				characterUnlockRepository.saveAndFlush(new CharacterUnlockEntity(
						UUID.randomUUID(),
						characterQuest.getCharacterId(),
						unlock,
						now));
			}
		}
		characterQuest.markCompleted(now);
		characterQuestRepository.saveAndFlush(characterQuest);
		activityApplicationService.record(
				characterQuest.getCharacterId(),
				ActivityType.QUEST_COMPLETED,
				"Quest complete: " + definition.getName() + ".");
		return unlocks;
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public boolean readyToComplete(
			UUID characterId,
			CharacterQuestEntity characterQuest,
			QuestDefinitionEntity definition) {
		List<CharacterQuestObjectiveEntity> rows = characterQuestObjectiveRepository
				.findByCharacterQuestId(characterQuest.getId());
		Map<UUID, CharacterQuestObjectiveEntity> byObjective = rows.stream()
				.collect(Collectors.toMap(CharacterQuestObjectiveEntity::getObjectiveId, row -> row));
		for (QuestObjectiveDefinitionEntity objective : questCatalog.objectivesOf(definition.getId())) {
			if (objective.getType() == QuestObjectiveType.COLLECT) {
				if (inventoryApplicationService.unreservedQuantityByCode(characterId, objective.getTargetCode())
						< objective.getRequiredAmount()) {
					return false;
				}
				continue;
			}
			CharacterQuestObjectiveEntity row = byObjective.get(objective.getId());
			if (row == null || !row.isCompleted()) {
				return false;
			}
		}
		return true;
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public boolean hasRoomForItemRewards(UUID characterId, QuestDefinitionEntity definition) {
		int extraSlots = 0;
		for (QuestRewardDefinitionEntity reward : questCatalog.rewardsOf(definition.getId())) {
			if (reward.getKind() != QuestRewardKind.ITEM || reward.getItemCode() == null) {
				continue;
			}
			ItemDefinitionView item = itemCatalogService.findByCode(reward.getItemCode())
					.orElseThrow(() -> new IllegalStateException("quest item missing: " + reward.getItemCode()));
			if (item.type().isStackable()) {
				if (inventoryApplicationService.unreservedQuantityByCode(characterId, item.code()) < 1) {
					extraSlots++;
				}
			}
			else {
				extraSlots += Math.max(1, reward.getAmount());
			}
		}
		return InventoryBalance.hasRoom(inventoryApplicationService.usedCapacity(characterId), extraSlots);
	}

	private void consumeCollectItems(CharacterQuestEntity characterQuest, QuestDefinitionEntity definition) {
		for (QuestObjectiveDefinitionEntity objective : questCatalog.objectivesOf(definition.getId())) {
			if (objective.getType() != QuestObjectiveType.COLLECT || !objective.isConsumeOnTurnIn()) {
				continue;
			}
			inventoryApplicationService.consumeUnreservedByCode(
					characterQuest.getCharacterId(),
					objective.getTargetCode(),
					objective.getRequiredAmount());
		}
	}

	private void grantItem(UUID characterId, QuestRewardDefinitionEntity reward) {
		ItemDefinitionView item = itemCatalogService.findByCode(reward.getItemCode())
				.orElseThrow(() -> new IllegalStateException("quest item missing: " + reward.getItemCode()));
		try {
			if (item.type().isStackable()) {
				inventoryApplicationService.grantItems(characterId, item.code(), reward.getAmount());
			}
			else {
				inventoryApplicationService.grantCatalogExact(characterId, item.code(), reward.getAmount());
			}
		}
		catch (InventoryFullException exception) {
			throw exception;
		}
		GameTelemetry.itemCreated(
				gameTelemetryRecorder,
				characterId,
				item.code(),
				item.rarity(),
				reward.getAmount(),
				ItemCreateSource.QUEST);
	}
}
