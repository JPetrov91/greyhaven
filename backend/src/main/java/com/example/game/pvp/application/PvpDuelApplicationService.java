package com.example.game.pvp.application;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.game.character.application.CharacterCombatGuard;
import com.example.game.character.application.CharacterPublicCore;
import com.example.game.character.application.CharacterApplicationService;
import com.example.game.character.application.CharacterVitalsService;
import com.example.game.character.application.CharacterVitalsView;
import com.example.game.combat.domain.CombatAction;
import com.example.game.combat.domain.CombatRuleViolation;
import com.example.game.pvp.domain.ArenaDefenseStrategy;
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
import com.example.game.pvp.infrastructure.PvpMatchEntity;
import com.example.game.pvp.infrastructure.PvpMatchRepository;
import com.example.game.shared.domain.RandomProvider;

@Service
public class PvpDuelApplicationService {

	private final CharacterVitalsService characterVitalsService;
	private final CharacterApplicationService characterApplicationService;
	private final CharacterCombatGuard characterCombatGuard;
	private final ArenaDefenseProfileRepository defenseRepository;
	private final PvpMatchRepository matchRepository;
	private final PvpSnapshotFactory snapshotFactory;
	private final PvpMatchSupport matchSupport;
	private final RandomProvider randomProvider;
	private final TransactionTemplate transactionTemplate;
	private final Clock clock;

	public PvpDuelApplicationService(
			CharacterVitalsService characterVitalsService,
			CharacterApplicationService characterApplicationService,
			CharacterCombatGuard characterCombatGuard,
			ArenaDefenseProfileRepository defenseRepository,
			PvpMatchRepository matchRepository,
			PvpSnapshotFactory snapshotFactory,
			PvpMatchSupport matchSupport,
			RandomProvider randomProvider,
			PlatformTransactionManager transactionManager,
			Clock clock) {
		this.characterVitalsService = characterVitalsService;
		this.characterApplicationService = characterApplicationService;
		this.characterCombatGuard = characterCombatGuard;
		this.defenseRepository = defenseRepository;
		this.matchRepository = matchRepository;
		this.snapshotFactory = snapshotFactory;
		this.matchSupport = matchSupport;
		this.randomProvider = randomProvider;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
		this.clock = clock;
	}

	@Transactional
	public PvpMatchView challenge(UUID accountId, UUID defenderId) {
		matchSupport.requireAtSparringYard(accountId);
		CharacterVitalsView attacker = characterVitalsService.lockVitalsOf(accountId);
		if (attacker.characterId().equals(defenderId)) {
			throw PvpErrors.selfChallenge();
		}
		if (attacker.level() > SparringBots.MAX_PLAYER_LEVEL) {
			throw PvpErrors.sparringLevelRequired();
		}
		characterCombatGuard.assertNotInActiveCombat(attacker.characterId());
		CharacterPublicCore defender = characterApplicationService.requirePublic(defenderId);
		if (defender.level() > SparringBots.MAX_PLAYER_LEVEL) {
			throw PvpErrors.sparringLevelRequired();
		}
		matchSupport.requireCharacterAtSparringYard(defenderId);
		characterCombatGuard.assertNotInActiveCombat(defenderId);
		if (matchRepository.findOpenDuelFor(attacker.characterId()).isPresent()
				|| matchRepository.findOpenDuelFor(defenderId).isPresent()) {
			throw PvpErrors.opponentBusy();
		}
		Instant now = Instant.now(clock);
		PvpMatchEntity match = new PvpMatchEntity(
				UUID.randomUUID(),
				PvpMatchKind.DUEL,
				PvpMatchStatus.PENDING,
				attacker.characterId(),
				defenderId,
				0,
				0,
				0,
				0,
				0,
				0,
				0,
				now.plus(Duration.ofMinutes(PvPBalance.DUEL_CHALLENGE_TTL_MINUTES)),
				0,
				0,
				0,
				0,
				attacker.arenaRating(),
				attacker.arenaRating(),
				BigDecimal.ZERO,
				true,
				now);
		try {
			matchRepository.saveAndFlush(match);
		}
		catch (DataIntegrityViolationException exception) {
			throw PvpErrors.opponentBusy();
		}
		return matchSupport.toView(match, true);
	}

	public PvpMatchView accept(UUID accountId, UUID matchId) {
		return java.util.Objects.requireNonNull(
				transactionTemplate.execute(status -> acceptInternal(accountId, matchId)));
	}

	@Transactional
	public PvpMatchView decline(UUID accountId, UUID matchId) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		PvpMatchEntity match = matchRepository.findWithLockById(matchId).orElseThrow(PvpErrors::matchNotFound);
		expireIfNeeded(match);
		if (match.getMatchKind() != PvpMatchKind.DUEL || !match.getDefenderId().equals(vitals.characterId())) {
			throw PvpErrors.matchNotFound();
		}
		if (match.getStatus() != PvpMatchStatus.PENDING) {
			throw PvpErrors.duelNotPending();
		}
		match.markDeclined(Instant.now(clock));
		matchSupport.settle(match);
		matchRepository.saveAndFlush(match);
		return matchSupport.toView(match, false);
	}

	public PvpMatchView current(UUID accountId) {
		return transactionTemplate.execute(status -> currentInternal(accountId));
	}

	public PvpMatchView submitAction(
			UUID accountId,
			UUID matchId,
			CombatAction action,
			String techniqueCode,
			int expectedRoundNumber) {
		PvpMatchView view = transactionTemplate.execute(status -> persistDuelTurn(
				accountId, matchId, action, techniqueCode, expectedRoundNumber));
		if (view.status() != PvpMatchStatus.ACTIVE && view.status() != PvpMatchStatus.PENDING
				&& view.settlement() != null && !view.settlement().applied()) {
			return transactionTemplate.execute(status -> settleCurrent(accountId, matchId));
		}
		return view;
	}

	private PvpMatchView acceptInternal(UUID accountId, UUID matchId) {
		matchSupport.requireAtSparringYard(accountId);
		CharacterVitalsView defender = characterVitalsService.lockVitalsOf(accountId);
		if (defender.level() > SparringBots.MAX_PLAYER_LEVEL) {
			throw PvpErrors.sparringLevelRequired();
		}
		characterCombatGuard.assertNotInActiveCombat(defender.characterId());
		PvpMatchEntity match = matchRepository.findWithLockById(matchId).orElseThrow(PvpErrors::matchNotFound);
		expireIfNeeded(match);
		if (match.getMatchKind() != PvpMatchKind.DUEL || !match.getDefenderId().equals(defender.characterId())) {
			throw PvpErrors.matchNotFound();
		}
		if (match.getStatus() != PvpMatchStatus.PENDING) {
			throw PvpErrors.duelNotPending();
		}
		characterVitalsService.lockVitalsByCharacterId(match.getAttackerId());
		characterCombatGuard.assertNotInActiveCombat(match.getAttackerId());
		CharacterPublicCore attacker = characterApplicationService.requirePublic(match.getAttackerId());
		Instant now = Instant.now(clock);
		ArenaDefenseStrategy defense = defenseRepository.findById(defender.characterId())
				.map(ArenaDefenseProfileEntity::toStrategy)
				.orElseGet(ArenaDefenseStrategy::defaults);
		PvpMatchSnapshot snapshot = snapshotFactory.capture(match.getAttackerId(), defender.characterId(), defense);
		match.activateFromPending(
				snapshot.attacker().maxHealth(),
				snapshot.attacker().maxStamina(),
				snapshot.defender().maxHealth(),
				snapshot.defender().maxStamina(),
				snapshot.attacker().potionCharges(),
				snapshot.defender().potionCharges(),
				0,
				0,
				0,
				0,
				attacker.arenaRating(),
				defender.arenaRating(),
				now.plus(Duration.ofMinutes(PvPBalance.DUEL_ACTION_TIMEOUT_MINUTES)),
				now);
		matchRepository.saveAndFlush(match);
		matchSupport.saveSnapshot(match.getId(), snapshot, now);
		return matchSupport.toView(match, false);
	}

	private PvpMatchView currentInternal(UUID accountId) {
		CharacterVitalsView vitals = characterVitalsService.vitalsOf(accountId);
		PvpMatchEntity match = matchRepository.findOpenDuelFor(vitals.characterId()).orElse(null);
		if (match == null) {
			return null;
		}
		match = matchRepository.findWithLockById(match.getId()).orElse(null);
		if (match == null) {
			return null;
		}
		if (advanceTimeouts(match)) {
			matchSupport.settle(match);
			matchRepository.saveAndFlush(match);
		}
		boolean attacker = match.getAttackerId().equals(vitals.characterId());
		return matchSupport.toView(match, attacker);
	}

	private PvpMatchView persistDuelTurn(
			UUID accountId,
			UUID matchId,
			CombatAction action,
			String techniqueCode,
			int expectedRoundNumber) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		PvpMatchEntity match = matchRepository.findWithLockById(matchId).orElseThrow(PvpErrors::matchNotFound);
		if (match.getMatchKind() != PvpMatchKind.DUEL
				|| (!match.getAttackerId().equals(vitals.characterId())
						&& !match.getDefenderId().equals(vitals.characterId()))) {
			throw PvpErrors.matchNotFound();
		}
		advanceTimeouts(match);
		if (match.getStatus() != PvpMatchStatus.ACTIVE) {
			return matchSupport.toView(match, match.getAttackerId().equals(vitals.characterId()));
		}
		if (match.getRoundNumber() != expectedRoundNumber) {
			throw PvpErrors.staleMatchState();
		}
		boolean attacker = match.getAttackerId().equals(vitals.characterId());
		Instant now = Instant.now(clock);
		Instant deadline = now.plus(Duration.ofMinutes(PvPBalance.DUEL_ACTION_TIMEOUT_MINUTES));
		if (attacker) {
			if (match.getPendingAttackerAction() != null) {
				throw PvpErrors.alreadyPendingAction();
			}
			match.setPendingAttacker(action, techniqueCode, deadline, now);
		}
		else {
			if (match.getPendingDefenderAction() != null) {
				throw PvpErrors.alreadyPendingAction();
			}
			match.setPendingDefender(action, techniqueCode, deadline, now);
		}
		if (match.getPendingAttackerAction() != null && match.getPendingDefenderAction() != null) {
			resolvePendingRound(match, now);
		}
		matchRepository.saveAndFlush(match);
		return matchSupport.toView(match, attacker);
	}

	private PvpMatchView settleCurrent(UUID accountId, UUID matchId) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		PvpMatchEntity match = matchRepository.findWithLockById(matchId).orElseThrow(PvpErrors::matchNotFound);
		matchSupport.settle(match);
		matchRepository.saveAndFlush(match);
		return matchSupport.toView(match, match.getAttackerId().equals(vitals.characterId()));
	}

	private void resolvePendingRound(PvpMatchEntity match, Instant now) {
		PvpMatchSnapshot snapshot = matchSupport.loadSnapshot(match.getId());
		PvpCombatState state = matchSupport.toState(match, snapshot);
		PvpRoundResult result;
		try {
			result = PvpCombatEngine.resolve(
					state,
					match.getPendingAttackerAction(),
					match.getPendingAttackerTechnique(),
					match.getPendingDefenderAction(),
					match.getPendingDefenderTechnique(),
					randomProvider);
		}
		catch (CombatRuleViolation violation) {
			throw matchSupport.mapRuleViolation(violation);
		}
		match.clearPending(now);
		matchSupport.applyRound(match, result, now);
	}

	private boolean advanceTimeouts(PvpMatchEntity match) {
		Instant now = Instant.now(clock);
		if (match.getStatus() == PvpMatchStatus.PENDING
				&& match.getExpiresAt() != null
				&& !now.isBefore(match.getExpiresAt())) {
			match.markExpired(now);
			return true;
		}
		if (match.getStatus() != PvpMatchStatus.ACTIVE) {
			return false;
		}
		if (match.getActionDeadlineAt() != null && !now.isBefore(match.getActionDeadlineAt())) {
			if (match.getPendingAttackerAction() == null && match.getPendingDefenderAction() == null) {
				if (match.getExpiresAt() != null && !now.isBefore(match.getExpiresAt().plus(Duration.ofMinutes(
						PvPBalance.DUEL_EXPIRE_MINUTES - PvPBalance.DUEL_CHALLENGE_TTL_MINUTES)))) {
					match.markExpired(now);
					return true;
				}
				match.markExpired(now);
				return true;
			}
			if (match.getPendingAttackerAction() == null) {
				match.setPendingAttacker(CombatAction.DEFEND, null, match.getActionDeadlineAt(), now);
			}
			if (match.getPendingDefenderAction() == null) {
				match.setPendingDefender(CombatAction.DEFEND, null, match.getActionDeadlineAt(), now);
			}
			resolvePendingRound(match, now);
			return match.getStatus() != PvpMatchStatus.ACTIVE;
		}
		if (match.getCreatedAt().plus(Duration.ofMinutes(PvPBalance.DUEL_EXPIRE_MINUTES)).isBefore(now)
				&& match.getPendingAttackerAction() == null
				&& match.getPendingDefenderAction() == null) {
			match.markExpired(now);
			return true;
		}
		return false;
	}

	private void expireIfNeeded(PvpMatchEntity match) {
		advanceTimeouts(match);
	}
}
