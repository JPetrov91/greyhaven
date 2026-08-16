package com.example.game.combat.application;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.game.activity.application.ActivityApplicationService;
import com.example.game.character.application.CharacterVitalsService;
import com.example.game.character.application.CharacterVitalsView;
import com.example.game.character.domain.ProgressionBalance;
import com.example.game.combat.domain.LootDrop;
import com.example.game.combat.domain.LootGenerator;
import com.example.game.combat.domain.LootTableEntry;
import com.example.game.combat.domain.UniqueLoot;
import com.example.game.combat.infrastructure.CharacterUniqueDropEntity;
import com.example.game.combat.infrastructure.CharacterUniqueDropRepository;
import com.example.game.combat.infrastructure.CombatRewardItemEntity;
import com.example.game.combat.infrastructure.CombatRewardItemRepository;
import com.example.game.combat.infrastructure.CombatSessionEntity;
import com.example.game.combat.infrastructure.CombatSessionRepository;
import com.example.game.combat.infrastructure.MonsterDefinitionEntity;
import com.example.game.combat.infrastructure.MonsterLootEntryEntity;
import com.example.game.combat.infrastructure.MonsterLootEntryRepository;
import com.example.game.inventory.application.InventoryApplicationService;
import com.example.game.item.application.ItemCatalogService;
import com.example.game.item.application.ItemDefinitionView;
import com.example.game.item.domain.GeneratedItem;
import com.example.game.mastery.application.MasteryApplicationService;
import com.example.game.quest.application.QuestProgressSink;
import com.example.game.shared.domain.RandomProvider;
import com.example.game.sparring.domain.SparringBots;
import com.example.game.telemetry.application.GameTelemetry;
import com.example.game.telemetry.application.GameTelemetryRecorder;
import com.example.game.telemetry.domain.ItemCreateSource;

/**
 * Pre-rolls and applies PvE combat rewards exactly once. Isolated from turn resolution so combat
 * refinement can change the engine without retouching loot/XP settlement.
 */
@Service
public class CombatRewardService {

	private final CharacterVitalsService characterVitalsService;
	private final InventoryApplicationService inventoryApplicationService;
	private final ActivityApplicationService activityApplicationService;
	private final CombatSessionRepository combatSessionRepository;
	private final CombatRewardItemRepository combatRewardItemRepository;
	private final MonsterLootEntryRepository monsterLootEntryRepository;
	private final CharacterUniqueDropRepository characterUniqueDropRepository;
	private final ItemCatalogService itemCatalogService;
	private final MasteryApplicationService masteryApplicationService;
	private final GameTelemetryRecorder gameTelemetryRecorder;
	private final RandomProvider randomProvider;
	private final QuestProgressSink questProgressSink;

	public CombatRewardService(
			CharacterVitalsService characterVitalsService,
			InventoryApplicationService inventoryApplicationService,
			ActivityApplicationService activityApplicationService,
			CombatSessionRepository combatSessionRepository,
			CombatRewardItemRepository combatRewardItemRepository,
			MonsterLootEntryRepository monsterLootEntryRepository,
			CharacterUniqueDropRepository characterUniqueDropRepository,
			ItemCatalogService itemCatalogService,
			MasteryApplicationService masteryApplicationService,
			GameTelemetryRecorder gameTelemetryRecorder,
			RandomProvider randomProvider,
			QuestProgressSink questProgressSink) {
		this.characterVitalsService = characterVitalsService;
		this.inventoryApplicationService = inventoryApplicationService;
		this.activityApplicationService = activityApplicationService;
		this.combatSessionRepository = combatSessionRepository;
		this.combatRewardItemRepository = combatRewardItemRepository;
		this.monsterLootEntryRepository = monsterLootEntryRepository;
		this.characterUniqueDropRepository = characterUniqueDropRepository;
		this.itemCatalogService = itemCatalogService;
		this.masteryApplicationService = masteryApplicationService;
		this.gameTelemetryRecorder = gameTelemetryRecorder;
		this.randomProvider = randomProvider;
		this.questProgressSink = questProgressSink;
	}

	void createRewardPlan(CombatSessionEntity session, MonsterDefinitionEntity monster, Instant now) {
		if (session.isRewardPlanCreated()) {
			return;
		}
		if (SparringBots.isBotCode(monster.getCode())) {
			session.markRewardPlan(0, 0, now);
			combatSessionRepository.saveAndFlush(session);
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

	/**
	 * Idempotent reward application. Safe under concurrent completion attempts because the session
	 * row is locked and {@code rewards_applied} flips in the same transaction.
	 */
	void applyRewardsExactlyOnce(CombatSessionEntity session, MonsterDefinitionEntity monster, Instant now) {
		if (session.isRewardsApplied()) {
			return;
		}
		if (SparringBots.isBotCode(monster.getCode())) {
			CharacterVitalsView vitals = characterVitalsService.lockVitalsByCharacterId(session.getCharacterId());
			session.markRewards(0, 0, vitals.level(), vitals.level(), now);
			combatSessionRepository.saveAndFlush(session);
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
		questProgressSink.onCombatVictory(session.getCharacterId(), monster.getCode(), session.getId());
	}

	CombatRewardsView loadRewards(CombatSessionEntity session) {
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

	List<CombatLootPreviewView> lootPreview(UUID monsterDefinitionId) {
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

	private static int attributePointsGained(CombatSessionEntity session) {
		Integer previous = session.getRewardPreviousLevel();
		Integer next = session.getRewardNewLevel();
		if (previous == null || next == null || next <= previous) {
			return 0;
		}
		return (next - previous) * ProgressionBalance.ATTRIBUTE_POINTS_PER_LEVEL;
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

	private static ItemDefinitionView requireItem(Map<UUID, ItemDefinitionView> definitions, UUID itemDefinitionId) {
		ItemDefinitionView item = definitions.get(itemDefinitionId);
		if (item == null) {
			throw new IllegalStateException("item definition missing: " + itemDefinitionId);
		}
		return item;
	}
}
