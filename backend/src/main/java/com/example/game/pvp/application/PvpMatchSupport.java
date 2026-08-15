package com.example.game.pvp.application;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.activity.application.ActivityApplicationService;
import com.example.game.activity.domain.ActivityType;
import com.example.game.character.application.CharacterIdentityService;
import com.example.game.character.application.CharacterVitalsService;
import com.example.game.combat.application.CombatActionPreviewView;
import com.example.game.combat.application.CombatEventView;
import com.example.game.combat.application.CombatStatusView;
import com.example.game.combat.application.CombatTechniqueOptionView;
import com.example.game.combat.domain.ActionCombatBalance;
import com.example.game.combat.domain.CombatAction;
import com.example.game.combat.domain.CombatEvent;
import com.example.game.combat.domain.CombatRuleViolation;
import com.example.game.combat.domain.CombatV2Balance;
import com.example.game.combat.domain.CombatantSide;
import com.example.game.combat.domain.StatusInstance;
import com.example.game.mastery.application.CombatTechniqueCatalogService;
import com.example.game.pvp.domain.ArenaRatingCalculator;
import com.example.game.pvp.domain.PvpCombatEngine;
import com.example.game.pvp.domain.PvpCombatState;
import com.example.game.pvp.domain.PvpHistoryResult;
import com.example.game.pvp.domain.PvpMatchKind;
import com.example.game.pvp.domain.PvpMatchSnapshot;
import com.example.game.pvp.domain.PvpMatchStatus;
import com.example.game.pvp.domain.PvpRoundResult;
import com.example.game.pvp.domain.PvPBalance;
import com.example.game.pvp.infrastructure.PvpBattleHistoryEntity;
import com.example.game.pvp.infrastructure.PvpBattleHistoryRepository;
import com.example.game.pvp.infrastructure.PvpMatchEntity;
import com.example.game.pvp.infrastructure.PvpMatchEventEntity;
import com.example.game.pvp.infrastructure.PvpMatchEventRepository;
import com.example.game.pvp.infrastructure.PvpMatchSnapshotEntity;
import com.example.game.pvp.infrastructure.PvpMatchSnapshotRepository;
import com.example.game.pvp.infrastructure.PvpMatchStatusEntity;
import com.example.game.pvp.infrastructure.PvpMatchStatusRepository;
import com.example.game.quest.application.QuestProgressSink;
import com.example.game.quest.domain.ArenaWonFact;
import com.example.game.telemetry.application.GameTelemetry;
import com.example.game.telemetry.application.GameTelemetryRecorder;
import com.example.game.world.application.WorldApplicationService;
import com.example.game.world.domain.LocationCodes;

@Component
public class PvpMatchSupport {

	private final WorldApplicationService worldApplicationService;
	private final PvpSnapshotCodec snapshotCodec;
	private final PvpMatchSnapshotRepository snapshotRepository;
	private final PvpMatchEventRepository eventRepository;
	private final PvpMatchStatusRepository statusRepository;
	private final PvpBattleHistoryRepository historyRepository;
	private final CharacterVitalsService characterVitalsService;
	private final CharacterIdentityService characterIdentityService;
	private final ActivityApplicationService activityApplicationService;
	private final CombatTechniqueCatalogService combatTechniqueCatalogService;
	private final GameTelemetryRecorder gameTelemetryRecorder;
	private final Clock clock;
	private final QuestProgressSink questProgressSink;

	public PvpMatchSupport(
			WorldApplicationService worldApplicationService,
			PvpSnapshotCodec snapshotCodec,
			PvpMatchSnapshotRepository snapshotRepository,
			PvpMatchEventRepository eventRepository,
			PvpMatchStatusRepository statusRepository,
			PvpBattleHistoryRepository historyRepository,
			CharacterVitalsService characterVitalsService,
			CharacterIdentityService characterIdentityService,
			ActivityApplicationService activityApplicationService,
			CombatTechniqueCatalogService combatTechniqueCatalogService,
			GameTelemetryRecorder gameTelemetryRecorder,
			Clock clock,
			QuestProgressSink questProgressSink) {
		this.worldApplicationService = worldApplicationService;
		this.snapshotCodec = snapshotCodec;
		this.snapshotRepository = snapshotRepository;
		this.eventRepository = eventRepository;
		this.statusRepository = statusRepository;
		this.historyRepository = historyRepository;
		this.characterVitalsService = characterVitalsService;
		this.characterIdentityService = characterIdentityService;
		this.activityApplicationService = activityApplicationService;
		this.combatTechniqueCatalogService = combatTechniqueCatalogService;
		this.gameTelemetryRecorder = gameTelemetryRecorder;
		this.clock = clock;
		this.questProgressSink = questProgressSink;
	}

	void requireAtArena(UUID accountId) {
		if (!LocationCodes.ARENA.equals(worldApplicationService.currentLocation(accountId).code())) {
			throw PvpErrors.notAtArena();
		}
	}

	void saveSnapshot(UUID matchId, PvpMatchSnapshot snapshot, Instant now) {
		snapshotRepository.saveAndFlush(new PvpMatchSnapshotEntity(
				matchId,
				snapshot.version(),
				snapshotCodec.write(snapshot),
				now));
	}

	PvpMatchSnapshot loadSnapshot(UUID matchId) {
		PvpMatchSnapshotEntity row = snapshotRepository.findById(matchId)
				.orElseThrow(PvpErrors::matchNotFound);
		return snapshotCodec.read(row.getPayload());
	}

	PvpCombatState toState(PvpMatchEntity match, PvpMatchSnapshot snapshot) {
		return new PvpCombatState(
				match.getRoundNumber(),
				match.getStatus(),
				snapshot.attacker(),
				snapshot.defender(),
				match.getAttackerHealth(),
				match.getAttackerStamina(),
				match.getDefenderHealth(),
				match.getDefenderStamina(),
				match.getAttackerPotionCharges(),
				match.getDefenderPotionCharges(),
				loadStatuses(match.getId(), CombatantSide.PLAYER),
				loadStatuses(match.getId(), CombatantSide.ENEMY),
				match.isLastDefenderMissed(),
				match.isLastAttackerGuarded(),
				snapshot.defense());
	}

	void replaceStatuses(UUID matchId, List<StatusInstance> attacker, List<StatusInstance> defender) {
		statusRepository.deleteByMatchId(matchId);
		statusRepository.flush();
		for (StatusInstance status : attacker) {
			statusRepository.save(new PvpMatchStatusEntity(
					UUID.randomUUID(), matchId, CombatantSide.PLAYER, status.type(), status.stacks(), status.remainingRounds()));
		}
		for (StatusInstance status : defender) {
			statusRepository.save(new PvpMatchStatusEntity(
					UUID.randomUUID(), matchId, CombatantSide.ENEMY, status.type(), status.stacks(), status.remainingRounds()));
		}
		statusRepository.flush();
	}

	void appendEvents(UUID matchId, int roundNumber, List<CombatEvent> events, Instant now) {
		int sequence = eventRepository.countByMatchId(matchId);
		for (CombatEvent event : events) {
			eventRepository.save(new PvpMatchEventEntity(
					UUID.randomUUID(),
					matchId,
					roundNumber,
					sequence++,
					event.type(),
					event.message(),
					now));
		}
		eventRepository.flush();
	}

	@Transactional(propagation = Propagation.MANDATORY)
	public void settle(PvpMatchEntity match) {
		if (match.isSettlementApplied()) {
			return;
		}
		Instant now = Instant.now(clock);
		boolean ranked = match.getMatchKind() == PvpMatchKind.ARENA
				&& (match.getStatus() == PvpMatchStatus.ATTACKER_WON
						|| match.getStatus() == PvpMatchStatus.DEFENDER_WON
						|| match.getStatus() == PvpMatchStatus.ATTACKER_FORFEIT);
		int attackerDelta = 0;
		int defenderDelta = 0;
		int attackerMarks = 0;
		int defenderMarks = 0;
		boolean attackerWon = match.getStatus() == PvpMatchStatus.ATTACKER_WON;
		boolean forfeit = match.getStatus() == PvpMatchStatus.ATTACKER_FORFEIT;
		if (ranked) {
			double multiplier = match.getRatingRewardMultiplier().doubleValue();
			ArenaRatingCalculator.RatingChange change = ArenaRatingCalculator.of(
					match.getAttackerRatingAtStart(),
					match.getDefenderRatingAtStart(),
					attackerWon,
					multiplier);
			attackerDelta = change.attackerDelta();
			defenderDelta = change.defenderDelta();
			attackerMarks = PvPBalance.marksAwarded(attackerWon, forfeit, multiplier);
			defenderMarks = PvPBalance.marksAwarded(!attackerWon, forfeit, multiplier);
		}
		UUID first = match.getAttackerId().compareTo(match.getDefenderId()) < 0
				? match.getAttackerId()
				: match.getDefenderId();
		if (first.equals(match.getAttackerId())) {
			characterVitalsService.applyArenaSettlement(
					match.getAttackerId(),
					attackerDelta,
					PvPBalance.RATING_FLOOR,
					attackerMarks);
			characterVitalsService.applyArenaSettlement(
					match.getDefenderId(),
					defenderDelta,
					PvPBalance.RATING_FLOOR,
					defenderMarks);
		}
		else {
			characterVitalsService.applyArenaSettlement(
					match.getDefenderId(),
					defenderDelta,
					PvPBalance.RATING_FLOOR,
					defenderMarks);
			characterVitalsService.applyArenaSettlement(
					match.getAttackerId(),
					attackerDelta,
					PvPBalance.RATING_FLOOR,
					attackerMarks);
		}
		String attackerName = characterIdentityService.requireName(match.getAttackerId());
		String defenderName = characterIdentityService.requireName(match.getDefenderId());
		PvpHistoryResult attackerResult = historyResult(match.getStatus(), true);
		PvpHistoryResult defenderResult = historyResult(match.getStatus(), false);
		historyRepository.save(new PvpBattleHistoryEntity(
				UUID.randomUUID(), match.getId(), match.getAttackerId(), match.getDefenderId(), defenderName,
				match.getMatchKind(), attackerResult, attackerDelta, attackerMarks, now));
		historyRepository.save(new PvpBattleHistoryEntity(
				UUID.randomUUID(), match.getId(), match.getDefenderId(), match.getAttackerId(), attackerName,
				match.getMatchKind(), defenderResult, defenderDelta, defenderMarks, now));
		if (ranked) {
			activityApplicationService.record(
					match.getAttackerId(),
					attackerWon ? ActivityType.ARENA_VICTORY : ActivityType.ARENA_DEFEAT,
					(attackerWon ? "Arena victory vs " : "Arena defeat vs ") + defenderName
							+ ratingSuffix(attackerDelta));
			activityApplicationService.record(
					match.getDefenderId(),
					attackerWon ? ActivityType.ARENA_DEFEAT : ActivityType.ARENA_VICTORY,
					(attackerWon ? "Your Arena defense lost to " : "Your Arena defense defeated ") + attackerName
							+ ratingSuffix(defenderDelta));
		}
		else {
			activityApplicationService.record(
					match.getAttackerId(),
					ActivityType.DUEL_RESULT,
					"Duel vs " + defenderName + " ended (" + match.getStatus().name().toLowerCase() + ").");
			activityApplicationService.record(
					match.getDefenderId(),
					ActivityType.DUEL_RESULT,
					"Duel vs " + attackerName + " ended (" + match.getStatus().name().toLowerCase() + ").");
		}
		match.markSettlementApplied(now);
		recordPvpTelemetry(match, attackerWon, attackerDelta, defenderDelta, attackerMarks, defenderMarks);
		if (match.getStatus() == PvpMatchStatus.ATTACKER_WON) {
			questProgressSink.notify(
					match.getAttackerId(),
					new ArenaWonFact(match.getMatchKind().name(), match.getId()));
		}
		else if (match.getStatus() == PvpMatchStatus.DEFENDER_WON) {
			questProgressSink.notify(
					match.getDefenderId(),
					new ArenaWonFact(match.getMatchKind().name(), match.getId()));
		}
	}

	private void recordPvpTelemetry(
			PvpMatchEntity match,
			boolean attackerWon,
			int attackerDelta,
			int defenderDelta,
			int attackerMarks,
			int defenderMarks) {
		com.example.game.item.domain.WeaponFamily attackerWeapon = null;
		com.example.game.item.domain.WeaponFamily defenderWeapon = null;
		String attackerBuild = "NONE";
		String defenderBuild = "NONE";
		if (snapshotRepository.existsById(match.getId())) {
			PvpMatchSnapshot snapshot = loadSnapshot(match.getId());
			attackerWeapon = snapshot.attacker().weaponFamily();
			defenderWeapon = snapshot.defender().weaponFamily();
			attackerBuild = dominantAttribute(snapshot.attacker());
			defenderBuild = dominantAttribute(snapshot.defender());
		}
		int attackerAfter = Math.max(
				PvPBalance.RATING_FLOOR,
				match.getAttackerRatingAtStart() + attackerDelta);
		int defenderAfter = Math.max(
				PvPBalance.RATING_FLOOR,
				match.getDefenderRatingAtStart() + defenderDelta);
		boolean repeatOpponent = match.getRatingRewardMultiplier().doubleValue() < 1.0;
		GameTelemetry.pvpMatchSettled(
				gameTelemetryRecorder,
				match.getAttackerId(),
				match.getDefenderId(),
				match.getMatchKind().name(),
				attackerWon,
				attackerWeapon,
				defenderWeapon,
				attackerBuild,
				defenderBuild,
				match.getAttackerRatingAtStart(),
				attackerAfter,
				match.getDefenderRatingAtStart(),
				defenderAfter,
				attackerMarks,
				defenderMarks,
				repeatOpponent);
	}

	private static String dominantAttribute(com.example.game.pvp.domain.PvpCombatantSnapshot combatant) {
		int strength = combatant.strength();
		int agility = combatant.agility();
		int endurance = combatant.endurance();
		int perception = combatant.perception();
		int max = Math.max(Math.max(strength, agility), Math.max(endurance, perception));
		if (max == strength) {
			return "STR";
		}
		if (max == agility) {
			return "AGI";
		}
		if (max == endurance) {
			return "END";
		}
		return "PER";
	}

	PvpMatchView toView(PvpMatchEntity match, boolean viewerIsAttacker) {
		List<CombatEventView> events = eventRepository
				.findByMatchIdOrderByRoundNumberAscSequenceNumberAsc(match.getId())
				.stream()
				.map(event -> new CombatEventView(
						event.getRoundNumber(),
						event.getSequenceNumber(),
						event.getEventType(),
						event.getMessage()))
				.toList();
		if (match.getStatus() == PvpMatchStatus.PENDING
				|| !snapshotRepository.existsById(match.getId())) {
			return pendingView(match, events);
		}
		PvpMatchSnapshot snapshot = loadSnapshot(match.getId());
		PvpCombatState state = toState(match, snapshot);
		List<CombatActionPreviewView> previews = actionPreviews(state, viewerIsAttacker);
		boolean potionAvailable = viewerIsAttacker
				? match.getAttackerPotionCharges() > 0
				: match.getDefenderPotionCharges() > 0;
		PvpSettlementView settlement = match.getStatus() == PvpMatchStatus.ACTIVE
				? null
				: settlementView(match);
		boolean waiting = match.getMatchKind() == PvpMatchKind.DUEL
				&& match.getStatus() == PvpMatchStatus.ACTIVE
				&& (viewerIsAttacker
						? match.getPendingAttackerAction() != null
						: match.getPendingDefenderAction() != null);
		CombatAction pending = viewerIsAttacker ? match.getPendingAttackerAction() : match.getPendingDefenderAction();
		return new PvpMatchView(
				match.getId(),
				match.getMatchKind(),
				match.getStatus(),
				match.getRoundNumber(),
				snapshot.attacker().name(),
				snapshot.defender().name(),
				match.getAttackerId(),
				match.getDefenderId(),
				match.getAttackerHealth(),
				snapshot.attacker().maxHealth(),
				match.getAttackerStamina(),
				snapshot.attacker().maxStamina(),
				match.getDefenderHealth(),
				snapshot.defender().maxHealth(),
				match.getDefenderStamina(),
				snapshot.defender().maxStamina(),
				potionAvailable,
				toStatusViews(state.attackerStatuses()),
				toStatusViews(state.defenderStatuses()),
				techniqueOptions(state, viewerIsAttacker),
				events,
				null,
				previews,
				settlement,
				waiting,
				pending,
				match.isOutcomeAcknowledged());
	}

	RuntimeException mapRuleViolation(CombatRuleViolation violation) {
		return switch (violation.getReason()) {
			case INSUFFICIENT_STAMINA -> PvpErrors.insufficientStamina();
			case NO_POTION -> PvpErrors.noPotion();
			case INVALID_TECHNIQUE -> PvpErrors.invalidTechnique();
			case COMBAT_NOT_ACTIVE -> PvpErrors.matchNotActive();
		};
	}

	void applyRound(PvpMatchEntity match, PvpRoundResult result, Instant now) {
		match.applyRound(
				result.roundNumber(),
				result.attackerHealth(),
				result.attackerStamina(),
				result.defenderHealth(),
				result.defenderStamina(),
				result.attackerPotionCharges(),
				result.defenderPotionCharges(),
				result.lastDefenderMissed(),
				result.lastAttackerGuarded(),
				result.status(),
				now);
		replaceStatuses(match.getId(), result.attackerStatuses(), result.defenderStatuses());
		appendEvents(match.getId(), result.roundNumber(), result.events(), now);
	}

	private PvpMatchView pendingView(PvpMatchEntity match, List<CombatEventView> events) {
		String attackerName = characterIdentityService.requireName(match.getAttackerId());
		String defenderName = characterIdentityService.requireName(match.getDefenderId());
		return new PvpMatchView(
				match.getId(),
				match.getMatchKind(),
				match.getStatus(),
				0,
				attackerName,
				defenderName,
				match.getAttackerId(),
				match.getDefenderId(),
				0, 0, 0, 0, 0, 0, 0, 0,
				false,
				List.of(),
				List.of(),
				List.of(),
				events,
				null,
				List.of(),
				match.getStatus() == PvpMatchStatus.PENDING ? null : settlementView(match),
				true,
				null,
				match.isOutcomeAcknowledged());
	}

	private PvpSettlementView settlementView(PvpMatchEntity match) {
		boolean attackerWon = match.getStatus() == PvpMatchStatus.ATTACKER_WON;
		boolean forfeit = match.getStatus() == PvpMatchStatus.ATTACKER_FORFEIT;
		boolean ranked = match.getMatchKind() == PvpMatchKind.ARENA
				&& match.getStatus() != PvpMatchStatus.EXPIRED
				&& match.getStatus() != PvpMatchStatus.DECLINED;
		int attackerDelta = 0;
		int defenderDelta = 0;
		int attackerMarks = 0;
		int defenderMarks = 0;
		if (ranked) {
			double multiplier = match.getRatingRewardMultiplier().doubleValue();
			ArenaRatingCalculator.RatingChange change = ArenaRatingCalculator.of(
					match.getAttackerRatingAtStart(),
					match.getDefenderRatingAtStart(),
					attackerWon,
					multiplier);
			attackerDelta = change.attackerDelta();
			defenderDelta = change.defenderDelta();
			attackerMarks = PvPBalance.marksAwarded(attackerWon, forfeit, multiplier);
			defenderMarks = PvPBalance.marksAwarded(!attackerWon, forfeit, multiplier);
		}
		return new PvpSettlementView(
				attackerDelta,
				defenderDelta,
				attackerMarks,
				defenderMarks,
				match.isSettlementApplied());
	}

	private List<StatusInstance> loadStatuses(UUID matchId, CombatantSide side) {
		return statusRepository.findByMatchIdAndTarget(matchId, side).stream()
				.sorted(Comparator.comparing(row -> row.getStatusType().name()))
				.map(row -> new StatusInstance(row.getStatusType(), row.getStacks(), row.getRemainingRounds()))
				.toList();
	}

	private static List<CombatStatusView> toStatusViews(List<StatusInstance> statuses) {
		return statuses.stream()
				.map(status -> new CombatStatusView(status.type(), status.stacks(), status.remainingRounds()))
				.toList();
	}

	private List<CombatTechniqueOptionView> techniqueOptions(PvpCombatState state, boolean attacker) {
		var combatant = attacker ? state.attacker() : state.defender();
		int stamina = attacker ? state.attackerStamina() : state.defenderStamina();
		var catalog = combatTechniqueCatalogService.load();
		List<CombatTechniqueOptionView> options = new ArrayList<>();
		for (String code : combatant.techniqueCodes()) {
			var definition = catalog.require(code);
			int cost = CombatV2Balance.reducedStaminaCost(
					definition.effect().staminaCost(), combatant.staminaCostReduction());
			String disabled = stamina < cost ? "Not enough stamina" : null;
			options.add(new CombatTechniqueOptionView(
					code, definition.displayName(), definition.description(), cost, disabled));
		}
		return options;
	}

	private List<CombatActionPreviewView> actionPreviews(PvpCombatState state, boolean attacker) {
		if (!attacker || state.status() != PvpMatchStatus.ACTIVE) {
			return List.of();
		}
		List<CombatActionPreviewView> previews = new ArrayList<>();
		for (CombatAction action : List.of(
				CombatAction.QUICK_ATTACK,
				CombatAction.HEAVY_ATTACK,
				CombatAction.PRECISE_ATTACK,
				CombatAction.DEFEND,
				CombatAction.USE_POTION)) {
			int cost = CombatV2Balance.reducedStaminaCost(
					ActionCombatBalance.staminaCost(action), state.attacker().staminaCostReduction());
			String disabled = null;
			if (action == CombatAction.USE_POTION && state.attackerPotionCharges() < 1) {
				disabled = "No potion charges";
			}
			else if (state.attackerStamina() < cost) {
				disabled = "Not enough stamina";
			}
			Integer hit = ActionCombatBalance.isAttack(action)
					? PvpCombatEngine.previewAttackerHitChance(state, action, null)
					: null;
			previews.add(new CombatActionPreviewView(
					action, null, action.name(), action.name(), cost, hit, disabled));
		}
		return previews;
	}

	private static PvpHistoryResult historyResult(PvpMatchStatus status, boolean attacker) {
		return switch (status) {
			case ATTACKER_WON -> attacker ? PvpHistoryResult.WIN : PvpHistoryResult.LOSS;
			case DEFENDER_WON, ATTACKER_FORFEIT -> attacker ? PvpHistoryResult.LOSS : PvpHistoryResult.WIN;
			case EXPIRED -> PvpHistoryResult.EXPIRED;
			case DECLINED -> PvpHistoryResult.DECLINED;
			default -> PvpHistoryResult.EXPIRED;
		};
	}

	private static String ratingSuffix(int delta) {
		if (delta == 0) {
			return " (rating unchanged)";
		}
		return delta > 0 ? " (rating +" + delta + ")" : " (rating " + delta + ")";
	}
}
