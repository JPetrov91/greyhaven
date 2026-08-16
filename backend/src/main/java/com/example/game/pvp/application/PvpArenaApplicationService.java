package com.example.game.pvp.application;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.game.character.application.ArenaOpponentCore;
import com.example.game.character.application.CharacterApplicationService;
import com.example.game.character.application.CharacterCombatGuard;
import com.example.game.character.application.CharacterVitalsService;
import com.example.game.character.application.CharacterVitalsView;
import com.example.game.combat.domain.CombatAction;
import com.example.game.combat.domain.CombatRuleViolation;
import com.example.game.mastery.application.TechniqueLoadoutQuery;
import com.example.game.pvp.domain.ArenaDefenseStrategy;
import com.example.game.pvp.domain.ChosenPvpAction;
import com.example.game.pvp.domain.PvpCombatEngine;
import com.example.game.pvp.domain.PvpCombatState;
import com.example.game.pvp.domain.PvpMatchKind;
import com.example.game.pvp.domain.PvpMatchSnapshot;
import com.example.game.pvp.domain.PvpMatchStatus;
import com.example.game.pvp.domain.PvpRoundResult;
import com.example.game.pvp.domain.PvPBalance;
import com.example.game.sparring.domain.SparringBots;
import com.example.game.pvp.infrastructure.ArenaDefenseProfileEntity;
import com.example.game.pvp.infrastructure.ArenaDefenseProfileRepository;
import com.example.game.pvp.infrastructure.PvpBattleHistoryEntity;
import com.example.game.pvp.infrastructure.PvpBattleHistoryRepository;
import com.example.game.pvp.infrastructure.PvpMatchEntity;
import com.example.game.pvp.infrastructure.PvpMatchRepository;
import com.example.game.shared.domain.RandomProvider;

@Service
public class PvpArenaApplicationService {

	private final CharacterVitalsService characterVitalsService;
	private final CharacterApplicationService characterApplicationService;
	private final CharacterCombatGuard characterCombatGuard;
	private final TechniqueLoadoutQuery techniqueLoadoutQuery;
	private final ArenaDefenseProfileRepository defenseRepository;
	private final PvpMatchRepository matchRepository;
	private final PvpBattleHistoryRepository historyRepository;
	private final PvpSnapshotFactory snapshotFactory;
	private final PvpMatchSupport matchSupport;
	private final RandomProvider randomProvider;
	private final TransactionTemplate transactionTemplate;
	private final Clock clock;

	public PvpArenaApplicationService(
			CharacterVitalsService characterVitalsService,
			CharacterApplicationService characterApplicationService,
			CharacterCombatGuard characterCombatGuard,
			TechniqueLoadoutQuery techniqueLoadoutQuery,
			ArenaDefenseProfileRepository defenseRepository,
			PvpMatchRepository matchRepository,
			PvpBattleHistoryRepository historyRepository,
			PvpSnapshotFactory snapshotFactory,
			PvpMatchSupport matchSupport,
			RandomProvider randomProvider,
			PlatformTransactionManager transactionManager,
			Clock clock) {
		this.characterVitalsService = characterVitalsService;
		this.characterApplicationService = characterApplicationService;
		this.characterCombatGuard = characterCombatGuard;
		this.techniqueLoadoutQuery = techniqueLoadoutQuery;
		this.defenseRepository = defenseRepository;
		this.matchRepository = matchRepository;
		this.historyRepository = historyRepository;
		this.snapshotFactory = snapshotFactory;
		this.matchSupport = matchSupport;
		this.randomProvider = randomProvider;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
		this.clock = clock;
	}

	@Transactional
	public ArenaProfileView profile(UUID accountId) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		ArenaDefenseStrategy defense = defenseOf(vitals.characterId());
		return new ArenaProfileView(
				vitals.characterId(),
				vitals.arenaRating(),
				vitals.arenaMarks(),
				defense,
				new CombatAction[] {
						CombatAction.QUICK_ATTACK,
						CombatAction.HEAVY_ATTACK,
						CombatAction.PRECISE_ATTACK,
						CombatAction.USE_TECHNIQUE
				});
	}

	@Transactional
	public ArenaProfileView updateDefense(UUID accountId, ArenaDefenseStrategy requested) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		List<String> loadout = techniqueLoadoutQuery.activeTechniqueCodes(vitals.characterId());
		try {
			requested.validateAgainstLoadout(loadout);
		}
		catch (IllegalArgumentException exception) {
			throw PvpErrors.invalidDefense();
		}
		Instant now = Instant.now(clock);
		ArenaDefenseProfileEntity profile = defenseRepository.findById(vitals.characterId())
				.orElseGet(() -> new ArenaDefenseProfileEntity(vitals.characterId(), requested, now));
		profile.apply(requested, now);
		defenseRepository.saveAndFlush(profile);
		return profile(accountId);
	}

	@Transactional(readOnly = true)
	public ArenaOpponentListView opponents(UUID accountId, int page) {
		matchSupport.requireAtArena(accountId);
		CharacterVitalsView vitals = characterVitalsService.vitalsOf(accountId);
		int size = PvPBalance.OPPONENTS_PAGE_SIZE;
		int safePage = Math.max(0, page);
		List<ArenaOpponentCore> found = characterApplicationService.arenaOpponents(
				vitals.characterId(),
				vitals.arenaRating(),
				PvPBalance.OPPONENT_RATING_BAND,
				safePage,
				size + 1);
		boolean hasMore = found.size() > size;
		List<ArenaOpponentListView.ArenaOpponentView> pageItems = found.stream()
				.limit(size)
				.map(row -> new ArenaOpponentListView.ArenaOpponentView(
						row.id(), row.name(), row.level(), row.arenaRating()))
				.toList();
		return new ArenaOpponentListView(pageItems, safePage, size, hasMore);
	}

	public PvpMatchView challenge(UUID accountId, UUID defenderId) {
		return java.util.Objects.requireNonNull(
				transactionTemplate.execute(status -> startChallenge(accountId, defenderId)));
	}

	public PvpMatchView submitAction(
			UUID accountId,
			UUID matchId,
			CombatAction action,
			String techniqueCode,
			int expectedRoundNumber) {
		PvpMatchView persisted = transactionTemplate.execute(status -> persistArenaTurn(
				accountId, matchId, action, techniqueCode, expectedRoundNumber));
		if (persisted.status() != PvpMatchStatus.ACTIVE && persisted.settlement() != null && !persisted.settlement().applied()) {
			return transactionTemplate.execute(status -> settleAndView(accountId, matchId));
		}
		return persisted;
	}

	@Transactional
	public PvpMatchView current(UUID accountId) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		return matchRepository.findByAttackerIdAndMatchKindAndStatus(
						vitals.characterId(), PvpMatchKind.ARENA, PvpMatchStatus.ACTIVE)
				.or(() -> matchRepository.findByAttackerIdAndMatchKindAndOutcomeAcknowledgedFalse(
						vitals.characterId(), PvpMatchKind.ARENA))
				.map(found -> {
					PvpMatchEntity match = matchRepository.findWithLockById(found.getId())
							.orElseThrow(PvpErrors::matchNotFound);
					if (match.getStatus() != PvpMatchStatus.ACTIVE && !match.isSettlementApplied()) {
						matchSupport.settle(match);
						matchRepository.saveAndFlush(match);
					}
					return matchSupport.toView(match, true);
				})
				.orElse(null);
	}

	@Transactional
	public void acknowledge(UUID accountId, UUID matchId) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		PvpMatchEntity match = matchRepository.findWithLockById(matchId).orElseThrow(PvpErrors::matchNotFound);
		if (!match.getAttackerId().equals(vitals.characterId()) || match.getMatchKind() != PvpMatchKind.ARENA) {
			throw PvpErrors.matchNotFound();
		}
		if (match.getStatus() == PvpMatchStatus.ACTIVE) {
			throw PvpErrors.matchNotActive();
		}
		if (!match.isSettlementApplied()) {
			matchSupport.settle(match);
		}
		if (!match.isOutcomeAcknowledged()) {
			match.acknowledgeOutcome(Instant.now(clock));
		}
		matchRepository.saveAndFlush(match);
	}

	@Transactional(readOnly = true)
	public PvpHistoryPageView history(UUID accountId, int page) {
		CharacterVitalsView vitals = characterVitalsService.vitalsOf(accountId);
		int size = PvPBalance.HISTORY_PAGE_SIZE;
		int safePage = Math.max(0, page);
		List<PvpBattleHistoryEntity> rows = historyRepository.findByCharacterIdOrderByCreatedAtDescIdDesc(
				vitals.characterId(), PageRequest.of(safePage, size + 1));
		boolean hasMore = rows.size() > size;
		return new PvpHistoryPageView(
				rows.stream().limit(size)
						.map(row -> new PvpHistoryPageView.PvpHistoryEntryView(
								row.getMatchId(),
								row.getMatchKind(),
								row.getOpponentName(),
								row.getOpponentId(),
								row.getResult(),
								row.getRatingDelta(),
								row.getMarksAwarded(),
								row.getCreatedAt()))
						.toList(),
				safePage,
				size,
				hasMore);
	}

	private PvpMatchView startChallenge(UUID accountId, UUID defenderId) {
		matchSupport.requireAtArena(accountId);
		CharacterVitalsView attackerPeek = characterVitalsService.vitalsOf(accountId);
		if (attackerPeek.level() < SparringBots.RANKED_ARENA_MIN_LEVEL) {
			throw PvpErrors.arenaLevelRequired();
		}
		if (attackerPeek.characterId().equals(defenderId)) {
			throw PvpErrors.selfChallenge();
		}
		CharacterVitalsView attacker;
		CharacterVitalsView defenderVitals;
		if (attackerPeek.characterId().compareTo(defenderId) < 0) {
			attacker = characterVitalsService.lockVitalsOf(accountId);
			defenderVitals = characterVitalsService.lockVitalsByCharacterId(defenderId);
		}
		else {
			defenderVitals = characterVitalsService.lockVitalsByCharacterId(defenderId);
			attacker = characterVitalsService.lockVitalsOf(accountId);
		}
		characterCombatGuard.assertNotInActiveCombat(attacker.characterId());
		if (matchRepository.findOpenDuelFor(attacker.characterId()).isPresent()) {
			throw PvpErrors.occupied();
		}
		if (matchRepository.findByAttackerIdAndMatchKindAndOutcomeAcknowledgedFalse(
				attacker.characterId(), PvpMatchKind.ARENA).isPresent()) {
			throw PvpErrors.outcomePending();
		}
		if (Math.abs(attacker.arenaRating() - defenderVitals.arenaRating()) > PvPBalance.OPPONENT_RATING_BAND) {
			throw PvpErrors.opponentOutOfRange();
		}
		Instant now = Instant.now(clock);
		Instant dayAgo = now.minus(Duration.ofHours(24));
		if (matchRepository.countByAttackerIdAndMatchKindAndCreatedAtGreaterThanEqual(
				attacker.characterId(), PvpMatchKind.ARENA, dayAgo) >= PvPBalance.MAX_ARENA_CHALLENGES_PER_DAY) {
			throw PvpErrors.dailyChallengeLimit();
		}
		Instant since = now.minus(Duration.ofHours(PvPBalance.REPEAT_WINDOW_HOURS));
		long repeats = matchRepository.countCompletedArenaBetweenSince(attacker.characterId(), defenderId, since);
		double multiplier = repeats > 0 ? PvPBalance.REPEAT_RATING_MULTIPLIER : 1.0;
		ArenaDefenseStrategy defense = defenseOf(defenderId);
		PvpMatchSnapshot snapshot = snapshotFactory.capture(attacker.characterId(), defenderId, defense);
		PvpMatchEntity match = new PvpMatchEntity(
				UUID.randomUUID(),
				PvpMatchKind.ARENA,
				PvpMatchStatus.ACTIVE,
				attacker.characterId(),
				defenderId,
				0,
				snapshot.attacker().maxHealth(),
				snapshot.attacker().maxStamina(),
				snapshot.defender().maxHealth(),
				snapshot.defender().maxStamina(),
				snapshot.attacker().potionCharges(),
				snapshot.defender().potionCharges(),
				null,
				0,
				0,
				0,
				0,
				attacker.arenaRating(),
				defenderVitals.arenaRating(),
				BigDecimal.valueOf(multiplier),
				true,
				now);
		try {
			matchRepository.saveAndFlush(match);
		}
		catch (DataIntegrityViolationException exception) {
			throw PvpErrors.occupied();
		}
		matchSupport.saveSnapshot(match.getId(), snapshot, now);
		return matchSupport.toView(match, true);
	}

	private PvpMatchView persistArenaTurn(
			UUID accountId,
			UUID matchId,
			CombatAction action,
			String techniqueCode,
			int expectedRoundNumber) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		PvpMatchEntity match = matchRepository.findWithLockById(matchId).orElseThrow(PvpErrors::matchNotFound);
		if (!match.getAttackerId().equals(vitals.characterId()) || match.getMatchKind() != PvpMatchKind.ARENA) {
			throw PvpErrors.matchNotFound();
		}
		if (match.getStatus() != PvpMatchStatus.ACTIVE) {
			return matchSupport.toView(match, true);
		}
		if (match.getRoundNumber() != expectedRoundNumber) {
			throw PvpErrors.staleMatchState();
		}
		Instant now = Instant.now(clock);
		if (action == CombatAction.RETREAT) {
			match.markForfeit(now);
			matchRepository.saveAndFlush(match);
			return matchSupport.toView(match, true);
		}
		PvpMatchSnapshot snapshot = matchSupport.loadSnapshot(match.getId());
		PvpCombatState state = matchSupport.toState(match, snapshot);
		ChosenPvpAction defender = PvpCombatEngine.previewDefenderIntent(state);
		PvpRoundResult result;
		try {
			result = PvpCombatEngine.resolve(
					state,
					action,
					techniqueCode,
					defender.action(),
					defender.techniqueCode(),
					randomProvider);
		}
		catch (CombatRuleViolation violation) {
			throw matchSupport.mapRuleViolation(violation);
		}
		matchSupport.applyRound(match, result, now);
		matchRepository.saveAndFlush(match);
		return matchSupport.toView(match, true);
	}

	private PvpMatchView settleAndView(UUID accountId, UUID matchId) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		PvpMatchEntity match = matchRepository.findWithLockById(matchId).orElseThrow(PvpErrors::matchNotFound);
		if (!match.getAttackerId().equals(vitals.characterId())) {
			throw PvpErrors.matchNotFound();
		}
		matchSupport.settle(match);
		matchRepository.saveAndFlush(match);
		return matchSupport.toView(match, true);
	}

	private ArenaDefenseStrategy defenseOf(UUID characterId) {
		return defenseRepository.findById(characterId)
				.map(ArenaDefenseProfileEntity::toStrategy)
				.orElseGet(ArenaDefenseStrategy::defaults);
	}
}
