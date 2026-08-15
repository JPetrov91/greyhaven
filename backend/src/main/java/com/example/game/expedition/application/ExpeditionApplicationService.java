package com.example.game.expedition.application;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.activity.application.ActivityApplicationService;
import com.example.game.character.application.CharacterExpeditionStartGuard;
import com.example.game.character.application.CharacterLocationService;
import com.example.game.character.application.CharacterLocationView;
import com.example.game.character.application.CharacterVitalsService;
import com.example.game.character.application.CharacterVitalsView;
import com.example.game.expedition.domain.ExpeditionBalance;
import com.example.game.expedition.domain.ExpeditionLootDrop;
import com.example.game.expedition.domain.ExpeditionResolver;
import com.example.game.expedition.domain.ExpeditionResult;
import com.example.game.expedition.domain.ExpeditionStatus;
import com.example.game.expedition.domain.ExpeditionStrategy;
import com.example.game.expedition.domain.ExpeditionType;
import com.example.game.expedition.infrastructure.ExpeditionEntity;
import com.example.game.expedition.infrastructure.ExpeditionRepository;
import com.example.game.expedition.infrastructure.ExpeditionRewardItemEntity;
import com.example.game.expedition.infrastructure.ExpeditionRewardItemRepository;
import com.example.game.inventory.application.InventoryApplicationService;
import com.example.game.inventory.application.InventoryFullException;
import com.example.game.item.application.ItemCatalogService;
import com.example.game.item.application.ItemDefinitionView;
import com.example.game.item.domain.GeneratedItem;
import com.example.game.quest.application.QuestProgressSink;
import com.example.game.quest.domain.ExpeditionCompletedFact;
import com.example.game.shared.domain.RandomProvider;
import com.example.game.telemetry.application.GameTelemetry;
import com.example.game.telemetry.application.GameTelemetryRecorder;
import com.example.game.telemetry.domain.GoldCreateReason;
import com.example.game.telemetry.domain.ItemCreateSource;
import com.example.game.character.domain.XpSource;
import com.example.game.world.application.LocationView;
import com.example.game.world.application.WorldApplicationService;
import com.example.game.world.domain.LocationAction;
import com.example.game.world.domain.LocationActions;

@Service
public class ExpeditionApplicationService {

	private final CharacterVitalsService characterVitalsService;
	private final CharacterLocationService characterLocationService;
	private final CharacterExpeditionStartGuard characterExpeditionStartGuard;
	private final WorldApplicationService worldApplicationService;
	private final InventoryApplicationService inventoryApplicationService;
	private final ItemCatalogService itemCatalogService;
	private final ActivityApplicationService activityApplicationService;
	private final ExpeditionRepository expeditionRepository;
	private final ExpeditionRewardItemRepository expeditionRewardItemRepository;
	private final GameTelemetryRecorder gameTelemetryRecorder;
	private final RandomProvider randomProvider;
	private final Clock clock;
	private final QuestProgressSink questProgressSink;

	public ExpeditionApplicationService(
			CharacterVitalsService characterVitalsService,
			CharacterLocationService characterLocationService,
			CharacterExpeditionStartGuard characterExpeditionStartGuard,
			WorldApplicationService worldApplicationService,
			InventoryApplicationService inventoryApplicationService,
			ItemCatalogService itemCatalogService,
			ActivityApplicationService activityApplicationService,
			ExpeditionRepository expeditionRepository,
			ExpeditionRewardItemRepository expeditionRewardItemRepository,
			GameTelemetryRecorder gameTelemetryRecorder,
			RandomProvider randomProvider,
			Clock clock,
			QuestProgressSink questProgressSink) {
		this.characterVitalsService = characterVitalsService;
		this.characterLocationService = characterLocationService;
		this.characterExpeditionStartGuard = characterExpeditionStartGuard;
		this.worldApplicationService = worldApplicationService;
		this.inventoryApplicationService = inventoryApplicationService;
		this.itemCatalogService = itemCatalogService;
		this.activityApplicationService = activityApplicationService;
		this.expeditionRepository = expeditionRepository;
		this.expeditionRewardItemRepository = expeditionRewardItemRepository;
		this.gameTelemetryRecorder = gameTelemetryRecorder;
		this.randomProvider = randomProvider;
		this.clock = clock;
		this.questProgressSink = questProgressSink;
	}

	@Transactional
	public ExpeditionView start(UUID accountId, ExpeditionStrategy strategy) {
		if (strategy == null) {
			throw new IllegalArgumentException("strategy is required");
		}
		CharacterLocationView locationView = characterLocationService.lockLocationOf(accountId);
		CharacterVitalsView vitals = characterVitalsService.lockVitalsByCharacterId(locationView.characterId());

		characterExpeditionStartGuard.assertCanStartExpedition(vitals.characterId());

		if (expeditionRepository.existsByCharacterIdAndStatus(vitals.characterId(), ExpeditionStatus.ACTIVE)
				|| expeditionRepository.existsByCharacterIdAndStatus(
						vitals.characterId(),
						ExpeditionStatus.COMPLETED)) {
			throw ExpeditionErrors.expeditionInProgress();
		}

		LocationView location = worldApplicationService.currentLocation(accountId);
		if (!LocationActions.forCode(location.code()).contains(LocationAction.START_EXPEDITION)) {
			throw ExpeditionErrors.locationCannotStartExpedition();
		}

		Instant now = Instant.now(clock);
		ExpeditionEntity expedition = new ExpeditionEntity(
				UUID.randomUUID(),
				vitals.characterId(),
				ExpeditionType.FOREST_PATROL,
				strategy,
				now,
				now.plus(ExpeditionBalance.FOREST_PATROL_DURATION),
				now,
				now);
		expeditionRepository.saveAndFlush(expedition);
		persistResultOnce(expedition, now);
		return toView(expedition, null);
	}

	@Transactional
	public ExpeditionView current(UUID accountId) {
		CharacterVitalsView vitals = characterVitalsService.vitalsOf(accountId);
		ExpeditionEntity expedition = findCurrentForCharacterWithLock(vitals.characterId());
		if (expedition == null) {
			return null;
		}
		Instant now = Instant.now(clock);
		if (expedition.getStatus() == ExpeditionStatus.ACTIVE && expedition.isDue(now)) {
			completeIfDue(expedition, now);
		}
		ExpeditionRewardsView rewards = expedition.getStatus() == ExpeditionStatus.ACTIVE
				? null
				: loadRewards(expedition);
		return toView(expedition, rewards);
	}

	@Transactional
	public ExpeditionView claim(UUID accountId, UUID expeditionId) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		ExpeditionEntity expedition = expeditionRepository.findWithLockById(expeditionId)
				.orElseThrow(ExpeditionErrors::expeditionNotFound);
		if (!expedition.getCharacterId().equals(vitals.characterId())) {
			throw ExpeditionErrors.expeditionNotFound();
		}

		completeIfDue(expedition, Instant.now(clock));

		if (expedition.getStatus() == ExpeditionStatus.CLAIMED) {
			throw ExpeditionErrors.expeditionAlreadyClaimed();
		}
		if (expedition.getStatus() != ExpeditionStatus.COMPLETED) {
			throw ExpeditionErrors.expeditionNotReady();
		}

		applyRewardsExactlyOnce(expedition, vitals.level());
		return toView(expedition, loadRewards(expedition));
	}

	private ExpeditionEntity findCurrentForCharacterWithLock(UUID characterId) {
		return expeditionRepository.findWithLockByCharacterIdAndStatus(characterId, ExpeditionStatus.ACTIVE)
				.or(() -> expeditionRepository.findWithLockByCharacterIdAndStatus(
						characterId,
						ExpeditionStatus.COMPLETED))
				.orElse(null);
	}

	/**
	 * Timestamp-driven transition for a row already locked by the caller.
	 */
	private void completeIfDue(ExpeditionEntity expedition, Instant now) {
		if (expedition.getStatus() != ExpeditionStatus.ACTIVE || !expedition.isDue(now)) {
			return;
		}
		expedition.markCompleted(now);
		expeditionRepository.saveAndFlush(expedition);
		activityApplicationService.recordExpeditionCompleted(
				expedition.getCharacterId(),
				expedition.getExpeditionType().displayName());
		questProgressSink.notify(
				expedition.getCharacterId(),
				new ExpeditionCompletedFact(expedition.getExpeditionType().name(), expedition.getId()));
	}

	/**
	 * The outcome is rolled once and stored with the expedition when it starts. Committing the plan
	 * before any claim attempt means a claim that fails (a full inventory, say) rolls back only the
	 * reward application, never the roll itself, so retries cannot reroll rewards.
	 */
	private void persistResultOnce(ExpeditionEntity expedition, Instant now) {
		if (expedition.isResultGenerated()) {
			return;
		}
		ExpeditionResult result = ExpeditionResolver.resolve(
				expedition.getExpeditionType(),
				expedition.getStrategy(),
				randomProvider);
		List<ExpeditionRewardItemEntity> rewardRows = toRewardRows(expedition.getId(), result.items());
		if (!rewardRows.isEmpty()) {
			expeditionRewardItemRepository.saveAll(rewardRows);
			expeditionRewardItemRepository.flush();
		}
		expedition.markResultPlan(result.xp(), result.gold(), result.injuryDamage(), now);
		expeditionRepository.saveAndFlush(expedition);
	}

	private List<ExpeditionRewardItemEntity> toRewardRows(UUID expeditionId, List<ExpeditionLootDrop> drops) {
		if (drops.isEmpty()) {
			return List.of();
		}
		Map<String, ItemDefinitionView> definitions = itemCatalogService.findByCodes(
				drops.stream().map(ExpeditionLootDrop::itemCode).toList());
		List<ExpeditionRewardItemEntity> rows = new ArrayList<>(drops.size());
		for (ExpeditionLootDrop drop : drops) {
			ItemDefinitionView item = definitions.get(drop.itemCode());
			if (item == null) {
				throw ExpeditionErrors.itemDefinitionMissing(drop.itemCode());
			}
			if (item.type().isStackable()) {
				rows.add(new ExpeditionRewardItemEntity(
						UUID.randomUUID(),
						expeditionId,
						item.id(),
						drop.quantity(),
						new GeneratedItem(item.rarity(), null, null, List.of())));
			}
			else {
				for (int i = 0; i < drop.quantity(); i++) {
					rows.add(new ExpeditionRewardItemEntity(
							UUID.randomUUID(),
							expeditionId,
							item.id(),
							1,
							inventoryApplicationService.rollItem(item.code())));
				}
			}
		}
		return rows;
	}

	private void applyRewardsExactlyOnce(ExpeditionEntity expedition, int previousLevel) {
		if (expedition.getStatus() == ExpeditionStatus.CLAIMED) {
			return;
		}
		int xp = nullToZero(expedition.getPlannedXp());
		int gold = nullToZero(expedition.getPlannedGold());
		int injury = nullToZero(expedition.getPlannedInjury());
		List<ExpeditionRewardItemEntity> rewardRows = expeditionRewardItemRepository
				.findByExpeditionId(expedition.getId());
		Map<UUID, ItemDefinitionView> definitions = itemCatalogService.findByIds(
				rewardRows.stream().map(ExpeditionRewardItemEntity::getItemDefinitionId).toList());

		CharacterVitalsView after = characterVitalsService.grantRewards(
				expedition.getCharacterId(),
				xp,
				gold,
				XpSource.EXPEDITION,
				GoldCreateReason.EXPEDITION);
		CharacterVitalsView afterInjury = characterVitalsService.applyInjury(expedition.getCharacterId(), injury);
		int injuryApplied = after.currentHealth() - afterInjury.currentHealth();

		for (ExpeditionRewardItemEntity reward : rewardRows) {
			ItemDefinitionView item = requireItem(definitions, reward.getItemDefinitionId());
			try {
				if (item.type().isStackable() || !reward.hasPlannedRoll()) {
					inventoryApplicationService.grantItems(
							expedition.getCharacterId(),
							item.code(),
							reward.getQuantity());
				}
				else {
					inventoryApplicationService.grantRolled(
							expedition.getCharacterId(),
							item.code(),
							reward.getQuantity(),
							reward.toGenerated());
				}
			}
			catch (InventoryFullException exception) {
				throw ExpeditionErrors.rewardsNeedInventorySpace();
			}
			activityApplicationService.recordItemFound(
					expedition.getCharacterId(),
					item.name(),
					reward.getQuantity());
			GameTelemetry.itemCreated(
					gameTelemetryRecorder,
					expedition.getCharacterId(),
					item.code(),
					reward.hasPlannedRoll() ? reward.toGenerated().rarity() : item.rarity(),
					reward.getQuantity(),
					ItemCreateSource.EXPEDITION);
		}

		activityApplicationService.recordLevelUps(expedition.getCharacterId(), previousLevel, after.level());
		activityApplicationService.recordExpeditionClaimed(
				expedition.getCharacterId(),
				expedition.getExpeditionType().displayName());

		Instant now = Instant.now(clock);
		expedition.markClaimed(xp, gold, injuryApplied, now);
		expeditionRepository.saveAndFlush(expedition);
	}

	private ExpeditionRewardsView loadRewards(ExpeditionEntity expedition) {
		List<ExpeditionRewardItemEntity> rows = expeditionRewardItemRepository.findByExpeditionId(expedition.getId());
		Map<UUID, ItemDefinitionView> definitions = itemCatalogService.findByIds(
				rows.stream().map(ExpeditionRewardItemEntity::getItemDefinitionId).toList());
		List<ExpeditionRewardItemView> items = rows.stream()
				.map(row -> {
					ItemDefinitionView item = requireItem(definitions, row.getItemDefinitionId());
					return new ExpeditionRewardItemView(item.code(), item.name(), row.getQuantity());
				})
				.toList();
		int xp = expedition.getStatus() == ExpeditionStatus.CLAIMED
				? nullToZero(expedition.getXpAwarded())
				: nullToZero(expedition.getPlannedXp());
		int gold = expedition.getStatus() == ExpeditionStatus.CLAIMED
				? nullToZero(expedition.getGoldAwarded())
				: nullToZero(expedition.getPlannedGold());
		int injury = expedition.getStatus() == ExpeditionStatus.CLAIMED
				? nullToZero(expedition.getInjuryApplied())
				: nullToZero(expedition.getPlannedInjury());
		return new ExpeditionRewardsView(xp, gold, injury, items);
	}

	private static int nullToZero(Integer value) {
		return value == null ? 0 : value;
	}

	private static ItemDefinitionView requireItem(Map<UUID, ItemDefinitionView> definitions, UUID itemDefinitionId) {
		ItemDefinitionView item = definitions.get(itemDefinitionId);
		if (item == null) {
			throw new IllegalStateException("item definition missing: " + itemDefinitionId);
		}
		return item;
	}

	private static ExpeditionView toView(ExpeditionEntity expedition, ExpeditionRewardsView rewards) {
		// Rewards exist from the start but stay hidden until the server clock says the patrol returned.
		boolean resultReady = expedition.getStatus() != ExpeditionStatus.ACTIVE;
		return new ExpeditionView(
				expedition.getId(),
				expedition.getExpeditionType(),
				expedition.getExpeditionType().displayName(),
				expedition.getStrategy(),
				expedition.getStatus(),
				expedition.getStartedAt(),
				expedition.getCompletesAt(),
				expedition.getClaimedAt(),
				resultReady,
				rewards);
	}
}
