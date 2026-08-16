package com.example.game.quest.application;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.inventory.application.InventoryApplicationService;
import com.example.game.inventory.domain.InventoryBalance;
import com.example.game.item.application.ItemCatalogService;
import com.example.game.item.domain.ItemCodes;
import com.example.game.item.infrastructure.ItemInstanceEntity;
import com.example.game.item.infrastructure.ItemInstanceRepository;
import com.example.game.quest.domain.IssuedSteelCopy;
import com.example.game.quest.domain.IssuedSteelKitFamily;
import com.example.game.quest.domain.IssuedSteelSearchOutcome;
import com.example.game.quest.domain.QuestCodes;
import com.example.game.quest.infrastructure.CharacterQuestEntity;
import com.example.game.quest.infrastructure.CharacterQuestRepository;
import com.example.game.quest.infrastructure.QuestDefinitionEntity;
import com.example.game.world.infrastructure.LocationEntity;
import com.example.game.world.infrastructure.LocationRepository;
import com.example.game.telemetry.application.GameTelemetry;
import com.example.game.telemetry.application.GameTelemetryRecorder;
import com.example.game.telemetry.domain.ItemCreateSource;

@Service
public class IssuedSteelKitService {

	private final CharacterQuestRepository characterQuestRepository;
	private final QuestCatalog questCatalog;
	private final InventoryApplicationService inventoryApplicationService;
	private final ItemCatalogService itemCatalogService;
	private final ItemInstanceRepository itemInstanceRepository;
	private final QuestProgressSink questProgressSink;
	private final GameTelemetryRecorder gameTelemetryRecorder;
	private final LocationRepository locationRepository;

	public IssuedSteelKitService(
			CharacterQuestRepository characterQuestRepository,
			QuestCatalog questCatalog,
			InventoryApplicationService inventoryApplicationService,
			ItemCatalogService itemCatalogService,
			ItemInstanceRepository itemInstanceRepository,
			QuestProgressSink questProgressSink,
			GameTelemetryRecorder gameTelemetryRecorder,
			LocationRepository locationRepository) {
		this.characterQuestRepository = characterQuestRepository;
		this.questCatalog = questCatalog;
		this.inventoryApplicationService = inventoryApplicationService;
		this.itemCatalogService = itemCatalogService;
		this.itemInstanceRepository = itemInstanceRepository;
		this.questProgressSink = questProgressSink;
		this.gameTelemetryRecorder = gameTelemetryRecorder;
		this.locationRepository = locationRepository;
	}

	@Transactional
	public IssuedSteelKitFamily grantKit(UUID characterId, IssuedSteelKitFamily family) {
		QuestDefinitionEntity definition = questCatalog.requireByCode(QuestCodes.MILITIA_NOTICE);
		CharacterQuestEntity quest = characterQuestRepository
				.findWithLockByCharacterIdAndQuestId(characterId, definition.getId())
				.orElseThrow(QuestErrors::questNotActive);
		if (quest.getKitFamily() != null) {
			return quest.getKitFamily();
		}
		boolean shield = family != IssuedSteelKitFamily.DAGGERS;
		int slots = shield ? 2 : 1;
		if (!InventoryBalance.hasRoom(inventoryApplicationService.usedCapacity(characterId), slots)) {
			throw new com.example.game.inventory.application.InventoryFullException("Your pack is full.");
		}
		String weaponCode = IssuedSteelCopy.weaponCode(family);
		inventoryApplicationService.grantCatalogExact(characterId, weaponCode, 1);
		equipByCode(characterId, weaponCode);
		GameTelemetry.itemCreated(
				gameTelemetryRecorder,
				characterId,
				weaponCode,
				itemCatalogService.findByCode(weaponCode).orElseThrow().rarity(),
				1,
				ItemCreateSource.QUEST);
		if (shield) {
			inventoryApplicationService.grantCatalogExact(characterId, ItemCodes.RUSTY_SHIELD, 1);
			equipByCode(characterId, ItemCodes.RUSTY_SHIELD);
			GameTelemetry.itemCreated(
					gameTelemetryRecorder,
					characterId,
					ItemCodes.RUSTY_SHIELD,
					itemCatalogService.findByCode(ItemCodes.RUSTY_SHIELD).orElseThrow().rarity(),
					1,
					ItemCreateSource.QUEST);
		}
		quest.grantKit(family);
		characterQuestRepository.saveAndFlush(quest);
		questProgressSink.onTalk(characterId, com.example.game.world.domain.NpcCodes.MILITIA_OFFICER);
		return family;
	}

	@Transactional
	public void recordCombat(UUID characterId, UUID locationId, boolean victory) {
		if (locationId == null) {
			return;
		}
		String locationCode = locationRepository.findById(locationId)
				.map(LocationEntity::getCode)
				.orElse(null);
		if (locationCode == null) {
			return;
		}
		if (victory) {
			recordVictory(characterId, locationCode);
		}
		else {
			recordRetreat(characterId, locationCode);
		}
	}

	@Transactional
	public void recordSearch(UUID characterId, String locationCode, boolean foundEncounter) {
		if (!com.example.game.world.domain.LocationCodes.OLD_TOWN.equals(locationCode)) {
			return;
		}
		questProgressSink.onLocationSearched(characterId, locationCode);
		if (!foundEncounter) {
			recordOutcome(characterId, IssuedSteelSearchOutcome.NO_COMBAT);
		}
	}

	@Transactional
	public void recordIgnoredEncounter(UUID characterId, String locationCode) {
		if (!com.example.game.world.domain.LocationCodes.OLD_TOWN.equals(locationCode)) {
			return;
		}
		recordOutcome(characterId, IssuedSteelSearchOutcome.NO_COMBAT);
	}

	@Transactional
	public void recordVictory(UUID characterId, String locationCode) {
		if (!com.example.game.world.domain.LocationCodes.OLD_TOWN.equals(locationCode)) {
			return;
		}
		recordOutcome(characterId, IssuedSteelSearchOutcome.VICTORY);
	}

	@Transactional
	public void recordRetreat(UUID characterId, String locationCode) {
		if (!com.example.game.world.domain.LocationCodes.OLD_TOWN.equals(locationCode)) {
			return;
		}
		recordOutcome(characterId, IssuedSteelSearchOutcome.RETREAT);
	}

	private void recordOutcome(UUID characterId, IssuedSteelSearchOutcome outcome) {
		QuestDefinitionEntity definition = questCatalog.requireByCode(QuestCodes.MILITIA_NOTICE);
		characterQuestRepository.findWithLockByCharacterIdAndQuestId(characterId, definition.getId())
				.ifPresent(quest -> {
					quest.recordSearchOutcome(outcome);
					characterQuestRepository.saveAndFlush(quest);
				});
	}

	private void equipByCode(UUID characterId, String itemCode) {
		UUID definitionId = itemCatalogService.findByCode(itemCode)
				.orElseThrow(() -> new IllegalStateException("item missing: " + itemCode))
				.id();
		ItemInstanceEntity instance = itemInstanceRepository
				.findByOwnerCharacterIdAndItemDefinitionId(characterId, definitionId)
				.orElseThrow(() -> new IllegalStateException("granted item missing: " + itemCode));
		inventoryApplicationService.equipOwnedItem(characterId, instance.getId());
	}
}
