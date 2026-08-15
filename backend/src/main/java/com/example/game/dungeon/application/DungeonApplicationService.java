package com.example.game.dungeon.application;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.character.application.CharacterLocationService;
import com.example.game.character.application.CharacterLocationView;
import com.example.game.character.application.CharacterVitalsService;
import com.example.game.character.application.CharacterVitalsView;
import com.example.game.combat.application.EncounterApplicationService;
import com.example.game.combat.application.EncounterCloseReason;
import com.example.game.combat.application.EncounterClosedEvent;
import com.example.game.combat.application.EncounterSearchView;
import com.example.game.combat.domain.EncounterStatus;
import com.example.game.combat.infrastructure.EncounterEntity;
import com.example.game.combat.infrastructure.EncounterRepository;
import com.example.game.combat.infrastructure.MonsterDefinitionEntity;
import com.example.game.combat.infrastructure.MonsterDefinitionRepository;
import com.example.game.quest.application.QuestProgressSink;
import com.example.game.quest.domain.DungeonCompletedFact;
import com.example.game.dungeon.domain.DungeonCodes;
import com.example.game.dungeon.domain.DungeonConnectivity;
import com.example.game.dungeon.domain.DungeonConnectivity.DungeonEdge;
import com.example.game.dungeon.domain.DungeonRoomKind;
import com.example.game.dungeon.domain.DungeonRoomState;
import com.example.game.dungeon.domain.DungeonRunStatus;
import com.example.game.dungeon.infrastructure.DungeonDefinitionEntity;
import com.example.game.dungeon.infrastructure.DungeonDefinitionRepository;
import com.example.game.dungeon.infrastructure.DungeonRoomEdgeEntity;
import com.example.game.dungeon.infrastructure.DungeonRoomEdgeRepository;
import com.example.game.dungeon.infrastructure.DungeonRoomEntity;
import com.example.game.dungeon.infrastructure.DungeonRoomRepository;
import com.example.game.dungeon.infrastructure.DungeonRunEntity;
import com.example.game.dungeon.infrastructure.DungeonRunRepository;
import com.example.game.dungeon.infrastructure.DungeonRunRoomEntity;
import com.example.game.dungeon.infrastructure.DungeonRunRoomRepository;

@Service
public class DungeonApplicationService {

	private static final EnumSet<EncounterStatus> UNRESOLVED = EnumSet.of(
			EncounterStatus.AVAILABLE,
			EncounterStatus.COMBAT_STARTED);

	private final CharacterLocationService characterLocationService;
	private final CharacterVitalsService characterVitalsService;
	private final DungeonDefinitionRepository dungeonDefinitionRepository;
	private final DungeonRoomRepository dungeonRoomRepository;
	private final DungeonRoomEdgeRepository dungeonRoomEdgeRepository;
	private final DungeonRunRepository dungeonRunRepository;
	private final DungeonRunRoomRepository dungeonRunRoomRepository;
	private final EncounterRepository encounterRepository;
	private final MonsterDefinitionRepository monsterDefinitionRepository;
	private final EncounterApplicationService encounterApplicationService;
	private final Clock clock;
	private final QuestProgressSink questProgressSink;

	public DungeonApplicationService(
			CharacterLocationService characterLocationService,
			CharacterVitalsService characterVitalsService,
			DungeonDefinitionRepository dungeonDefinitionRepository,
			DungeonRoomRepository dungeonRoomRepository,
			DungeonRoomEdgeRepository dungeonRoomEdgeRepository,
			DungeonRunRepository dungeonRunRepository,
			DungeonRunRoomRepository dungeonRunRoomRepository,
			EncounterRepository encounterRepository,
			MonsterDefinitionRepository monsterDefinitionRepository,
			EncounterApplicationService encounterApplicationService,
			Clock clock,
			QuestProgressSink questProgressSink) {
		this.characterLocationService = characterLocationService;
		this.characterVitalsService = characterVitalsService;
		this.dungeonDefinitionRepository = dungeonDefinitionRepository;
		this.dungeonRoomRepository = dungeonRoomRepository;
		this.dungeonRoomEdgeRepository = dungeonRoomEdgeRepository;
		this.dungeonRunRepository = dungeonRunRepository;
		this.dungeonRunRoomRepository = dungeonRunRoomRepository;
		this.encounterRepository = encounterRepository;
		this.monsterDefinitionRepository = monsterDefinitionRepository;
		this.encounterApplicationService = encounterApplicationService;
		this.clock = clock;
		this.questProgressSink = questProgressSink;
	}

	@Transactional
	public DungeonRunView current(UUID accountId) {
		CharacterLocationView location = characterLocationService.locationOf(accountId);
		CharacterVitalsView vitals = characterVitalsService.vitalsOf(accountId);
		return dungeonRunRepository
				.findByCharacterIdAndStatus(vitals.characterId(), DungeonRunStatus.ACTIVE)
				.map(run -> {
					DungeonDefinitionEntity dungeon = dungeonDefinitionRepository.findById(run.getDungeonId())
							.orElseThrow(() -> new IllegalStateException("dungeon missing"));
					boolean atGate = dungeon.getEntranceLocationId().equals(location.currentLocationId())
							&& !run.isPaused();
					return toView(run, atGate);
				})
				.orElse(null);
	}

	@Transactional
	public DungeonRunView enter(UUID accountId) {
		CharacterLocationView location = characterLocationService.lockLocationOf(accountId);
		CharacterVitalsView vitals = characterVitalsService.lockVitalsByCharacterId(location.characterId());
		DungeonDefinitionEntity dungeon = requireKeep();
		if (!dungeon.getEntranceLocationId().equals(location.currentLocationId())) {
			throw DungeonErrors.notAtEntrance();
		}
		Instant now = Instant.now(clock);
		DungeonRunEntity existing = dungeonRunRepository
				.findWithLockByCharacterIdAndStatus(vitals.characterId(), DungeonRunStatus.ACTIVE)
				.orElse(null);
		if (existing != null) {
			existing.resume(now);
			dungeonRunRepository.saveAndFlush(existing);
			ensureEncounter(existing, dungeon);
			return toView(existing, true);
		}
		List<DungeonRoomEntity> rooms = dungeonRoomRepository.findByDungeonIdOrderBySortOrderAsc(dungeon.getId());
		DungeonRoomEntity entrance = rooms.stream()
				.filter(room -> room.getRoomKind() == DungeonRoomKind.ENTRANCE)
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("dungeon missing entrance"));
		DungeonRunEntity run = new DungeonRunEntity(
				UUID.randomUUID(),
				vitals.characterId(),
				dungeon.getId(),
				entrance.getCode(),
				now);
		dungeonRunRepository.saveAndFlush(run);
		List<DungeonRunRoomEntity> progress = new ArrayList<>();
		for (DungeonRoomEntity room : rooms) {
			DungeonRoomState state = room.getCode().equals(entrance.getCode())
					? DungeonRoomState.CLEARED
					: DungeonRoomState.LOCKED;
			progress.add(new DungeonRunRoomEntity(UUID.randomUUID(), run.getId(), room.getCode(), state));
		}
		dungeonRunRoomRepository.saveAll(progress);
		dungeonRunRoomRepository.flush();
		unlockOutgoing(run, rooms, entrance, now, false);
		return toView(run, true);
	}

	@Transactional
	public DungeonRunView leave(UUID accountId) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		if (encounterRepository.existsByCharacterIdAndStatusIn(
				vitals.characterId(), EnumSet.of(EncounterStatus.COMBAT_STARTED))) {
			throw DungeonErrors.encounterOutstanding();
		}
		DungeonRunEntity run = dungeonRunRepository
				.findWithLockByCharacterIdAndStatus(vitals.characterId(), DungeonRunStatus.ACTIVE)
				.orElseThrow(DungeonErrors::noActiveRun);
		Instant now = Instant.now(clock);
		encounterRepository.findWithLockByCharacterIdAndStatusIn(
				vitals.characterId(), EnumSet.of(EncounterStatus.AVAILABLE))
				.filter(EncounterEntity::isDungeonEncounter)
				.ifPresent(encounter -> {
					encounter.expire(now);
					encounterRepository.saveAndFlush(encounter);
				});
		run.pause(now);
		dungeonRunRepository.saveAndFlush(run);
		return toView(run, false);
	}

	@Transactional
	public DungeonRunView abandon(UUID accountId) {
		CharacterVitalsView vitals = characterVitalsService.lockVitalsOf(accountId);
		if (encounterRepository.existsByCharacterIdAndStatusIn(vitals.characterId(), UNRESOLVED)) {
			throw DungeonErrors.encounterOutstanding();
		}
		DungeonRunEntity run = dungeonRunRepository
				.findWithLockByCharacterIdAndStatus(vitals.characterId(), DungeonRunStatus.ACTIVE)
				.orElseThrow(DungeonErrors::noActiveRun);
		run.abandon(Instant.now(clock));
		dungeonRunRepository.saveAndFlush(run);
		return toView(run, false);
	}

	@Transactional
	public DungeonRunView advance(UUID accountId, String edgeCode) {
		if (edgeCode == null || edgeCode.isBlank()) {
			throw DungeonErrors.invalidAdvance();
		}
		CharacterLocationView location = characterLocationService.lockLocationOf(accountId);
		CharacterVitalsView vitals = characterVitalsService.lockVitalsByCharacterId(location.characterId());
		DungeonDefinitionEntity dungeon = requireKeep();
		if (!dungeon.getEntranceLocationId().equals(location.currentLocationId())) {
			throw DungeonErrors.notAtEntrance();
		}
		if (encounterRepository.existsByCharacterIdAndStatusIn(vitals.characterId(), UNRESOLVED)) {
			throw DungeonErrors.encounterOutstanding();
		}
		DungeonRunEntity run = dungeonRunRepository
				.findWithLockByCharacterIdAndStatus(vitals.characterId(), DungeonRunStatus.ACTIVE)
				.orElseThrow(DungeonErrors::noActiveRun);
		if (run.getStatus() != DungeonRunStatus.ACTIVE) {
			throw DungeonErrors.alreadyComplete();
		}
		Instant now = Instant.now(clock);
		run.resume(now);
		List<DungeonRoomEntity> rooms = dungeonRoomRepository.findByDungeonIdOrderBySortOrderAsc(dungeon.getId());
		Map<UUID, DungeonRoomEntity> roomsById = rooms.stream()
				.collect(Collectors.toMap(DungeonRoomEntity::getId, Function.identity()));
		Map<String, DungeonRoomEntity> roomsByCode = rooms.stream()
				.collect(Collectors.toMap(DungeonRoomEntity::getCode, Function.identity()));
		DungeonRoomEntity current = roomsByCode.get(run.getCurrentRoomCode());
		List<DungeonRoomEdgeEntity> outgoing = dungeonRoomEdgeRepository.findByFromRoomId(current.getId());
		DungeonRoomEdgeEntity chosen = outgoing.stream()
				.filter(edge -> edge.getEdgeCode().equals(edgeCode))
				.findFirst()
				.orElse(null);
		if (chosen == null) {
			throw DungeonErrors.invalidAdvance();
		}
		DungeonRoomEntity destination = roomsById.get(chosen.getToRoomId());
		List<DungeonEdge> domainEdges = outgoing.stream()
				.map(edge -> new DungeonEdge(
						current.getCode(),
						roomsById.get(edge.getToRoomId()).getCode(),
						edge.getEdgeCode()))
				.toList();
		if (!DungeonConnectivity.canAdvance(current.getCode(), destination.getCode(), edgeCode, domainEdges)) {
			throw DungeonErrors.invalidAdvance();
		}
		DungeonRunRoomEntity destProgress = dungeonRunRoomRepository
				.findByRunIdAndRoomCode(run.getId(), destination.getCode())
				.orElseThrow(() -> new IllegalStateException("missing dungeon room progress"));
		if (destProgress.getState() == DungeonRoomState.LOCKED) {
			throw DungeonErrors.roomLocked();
		}
		if (destProgress.getState() == DungeonRoomState.SKIPPED) {
			throw DungeonErrors.invalidAdvance();
		}
		if (current.getRoomKind() == DungeonRoomKind.CHOICE) {
			run.chooseBranch(edgeCode, now);
		}
		if (chosen.getSkipRoomCode() != null && !chosen.getSkipRoomCode().isBlank()) {
			skipRoom(run, chosen.getSkipRoomCode());
		}
		run.moveTo(destination.getCode(), now);
		dungeonRunRepository.saveAndFlush(run);
		if (destination.getRoomKind() == DungeonRoomKind.CHOICE
				|| destination.getRoomKind() == DungeonRoomKind.ENTRANCE) {
			markCleared(run, destination.getCode());
			unlockOutgoing(run, rooms, destination, now, false);
		}
		ensureEncounter(run, dungeon);
		return toView(run, true);
	}

	@EventListener
	public void onEncounterClosed(EncounterClosedEvent event) {
		EncounterEntity encounter = encounterRepository.findById(event.encounterId()).orElse(null);
		if (encounter == null || encounter.getDungeonRunId() == null) {
			return;
		}
		DungeonRunEntity run = dungeonRunRepository.findWithLockById(encounter.getDungeonRunId()).orElse(null);
		if (run == null || run.getStatus() != DungeonRunStatus.ACTIVE) {
			return;
		}
		Instant now = Instant.now(clock);
		DungeonDefinitionEntity dungeon = dungeonDefinitionRepository.findById(run.getDungeonId())
				.orElseThrow(() -> new IllegalStateException("dungeon missing"));
		List<DungeonRoomEntity> rooms = dungeonRoomRepository.findByDungeonIdOrderBySortOrderAsc(dungeon.getId());
		Map<String, DungeonRoomEntity> roomsByCode = rooms.stream()
				.collect(Collectors.toMap(DungeonRoomEntity::getCode, Function.identity()));
		DungeonRoomEntity room = roomsByCode.get(encounter.getDungeonRoomCode());
		if (event.reason() == EncounterCloseReason.WON) {
			markCleared(run, room.getCode());
			if (room.getRoomKind() == DungeonRoomKind.BOSS) {
				if (!run.isUniqueRewardGranted()) {
					run.grantUniqueReward();
				}
				run.complete(now);
				dungeonRunRepository.saveAndFlush(run);
				questProgressSink.notify(
						run.getCharacterId(),
						new DungeonCompletedFact(dungeon.getCode(), run.getId()));
				return;
			}
			unlockOutgoing(run, rooms, room, now, false);
			dungeonRunRepository.saveAndFlush(run);
			return;
		}
		if (event.reason() == EncounterCloseReason.IGNORED && room.getRoomKind() == DungeonRoomKind.OPTIONAL) {
			skipRoom(run, room.getCode());
			unlockOutgoing(run, rooms, room, now, true);
			dungeonRunRepository.saveAndFlush(run);
		}
	}

	private void ensureEncounter(DungeonRunEntity run, DungeonDefinitionEntity dungeon) {
		List<DungeonRoomEntity> rooms = dungeonRoomRepository.findByDungeonIdOrderBySortOrderAsc(dungeon.getId());
		DungeonRoomEntity current = rooms.stream()
				.filter(room -> room.getCode().equals(run.getCurrentRoomCode()))
				.findFirst()
				.orElseThrow();
		if (current.getMonsterDefinitionId() == null) {
			return;
		}
		DungeonRunRoomEntity progress = dungeonRunRoomRepository
				.findByRunIdAndRoomCode(run.getId(), current.getCode())
				.orElseThrow();
		if (progress.getState() == DungeonRoomState.CLEARED || progress.getState() == DungeonRoomState.SKIPPED) {
			return;
		}
		if (encounterRepository.existsByCharacterIdAndStatusIn(run.getCharacterId(), UNRESOLVED)) {
			return;
		}
		boolean optional = current.getRoomKind() == DungeonRoomKind.OPTIONAL;
		encounterApplicationService.createScriptedEncounter(
				run.getCharacterId(),
				dungeon.getEntranceLocationId(),
				current.getMonsterDefinitionId(),
				run.getId(),
				current.getCode(),
				optional);
	}

	private void unlockOutgoing(
			DungeonRunEntity run,
			List<DungeonRoomEntity> rooms,
			DungeonRoomEntity from,
			Instant now,
			boolean followContinueOnly) {
		Map<UUID, DungeonRoomEntity> roomsById = rooms.stream()
				.collect(Collectors.toMap(DungeonRoomEntity::getId, Function.identity()));
		List<DungeonRoomEdgeEntity> outgoing = dungeonRoomEdgeRepository.findByFromRoomId(from.getId());
		for (DungeonRoomEdgeEntity edge : outgoing) {
			if (followContinueOnly && !"CONTINUE".equals(edge.getEdgeCode())) {
				continue;
			}
			DungeonRoomEntity dest = roomsById.get(edge.getToRoomId());
			DungeonRunRoomEntity progress = dungeonRunRoomRepository
					.findByRunIdAndRoomCode(run.getId(), dest.getCode())
					.orElseThrow();
			if (progress.getState() == DungeonRoomState.LOCKED) {
				progress.setState(DungeonRoomState.AVAILABLE);
				dungeonRunRoomRepository.save(progress);
			}
		}
		dungeonRunRoomRepository.flush();
	}

	private void skipRoom(DungeonRunEntity run, String roomCode) {
		dungeonRunRoomRepository.findByRunIdAndRoomCode(run.getId(), roomCode).ifPresent(progress -> {
			if (progress.getState() != DungeonRoomState.CLEARED) {
				progress.setState(DungeonRoomState.SKIPPED);
				dungeonRunRoomRepository.save(progress);
			}
		});
	}

	private void markCleared(DungeonRunEntity run, String roomCode) {
		dungeonRunRoomRepository.findByRunIdAndRoomCode(run.getId(), roomCode).ifPresent(progress -> {
			progress.setState(DungeonRoomState.CLEARED);
			dungeonRunRoomRepository.saveAndFlush(progress);
		});
	}

	private DungeonDefinitionEntity requireKeep() {
		return dungeonDefinitionRepository.findByCode(DungeonCodes.RUINED_KEEP)
				.orElseThrow(() -> new IllegalStateException("Ruined Keep definition missing"));
	}

	private DungeonRunView toView(DungeonRunEntity run, boolean spawnIfNeeded) {
		DungeonDefinitionEntity dungeon = dungeonDefinitionRepository.findById(run.getDungeonId())
				.orElseThrow(() -> new IllegalStateException("dungeon missing"));
		if (spawnIfNeeded && run.getStatus() == DungeonRunStatus.ACTIVE && !run.isPaused()) {
			ensureEncounter(run, dungeon);
		}
		List<DungeonRoomEntity> rooms = dungeonRoomRepository.findByDungeonIdOrderBySortOrderAsc(dungeon.getId());
		Map<String, DungeonRunRoomEntity> progressByCode = dungeonRunRoomRepository.findByRunId(run.getId()).stream()
				.collect(Collectors.toMap(DungeonRunRoomEntity::getRoomCode, Function.identity()));
		Map<UUID, DungeonRoomEntity> roomsById = rooms.stream()
				.collect(Collectors.toMap(DungeonRoomEntity::getId, Function.identity()));
		DungeonRoomEntity current = rooms.stream()
				.filter(room -> room.getCode().equals(run.getCurrentRoomCode()))
				.findFirst()
				.orElseThrow();
		List<DungeonRoomView> roomViews = rooms.stream()
				.map(room -> new DungeonRoomView(
						room.getCode(),
						room.getName(),
						room.getRoomKind(),
						progressByCode.get(room.getCode()).getState()))
				.toList();
		List<DungeonChoiceView> choices = new ArrayList<>();
		if (run.getStatus() == DungeonRunStatus.ACTIVE) {
			DungeonRunRoomEntity currentProgress = progressByCode.get(current.getCode());
			boolean currentBlockingFight = current.getMonsterDefinitionId() != null
					&& currentProgress.getState() != DungeonRoomState.CLEARED
					&& currentProgress.getState() != DungeonRoomState.SKIPPED;
			if (!currentBlockingFight) {
				for (DungeonRoomEdgeEntity edge : dungeonRoomEdgeRepository.findByFromRoomId(current.getId())) {
					DungeonRoomEntity dest = roomsById.get(edge.getToRoomId());
					DungeonRoomState destState = progressByCode.get(dest.getCode()).getState();
					if (destState == DungeonRoomState.LOCKED || destState == DungeonRoomState.SKIPPED) {
						continue;
					}
					choices.add(new DungeonChoiceView(
							edge.getEdgeCode(),
							dest.getCode(),
							dest.getName(),
							dest.getRoomKind() == DungeonRoomKind.OPTIONAL || "OPTIONAL".equals(edge.getEdgeCode())));
				}
			}
		}
		EncounterSearchView encounter = encounterRepository
				.findByCharacterIdAndStatusIn(run.getCharacterId(), EnumSet.of(EncounterStatus.AVAILABLE))
				.filter(EncounterEntity::isDungeonEncounter)
				.map(row -> {
					MonsterDefinitionEntity monster = monsterDefinitionRepository.findById(row.getMonsterDefinitionId())
							.orElseThrow(() -> new IllegalStateException("monster missing"));
					return EncounterSearchView.found(row.getId(), EncounterApplicationService.toMonsterView(monster));
				})
				.orElse(null);
		return new DungeonRunView(
				run.getId(),
				dungeon.getCode(),
				dungeon.getName(),
				run.getStatus(),
				run.isPaused(),
				current.getCode(),
				current.getName(),
				current.getDescription(),
				current.getRoomKind(),
				run.getChosenBranch(),
				run.isUniqueRewardGranted()
						|| dungeonRunRepository.existsByCharacterIdAndUniqueRewardGrantedTrue(run.getCharacterId()),
				roomViews,
				choices,
				encounter);
	}
}
