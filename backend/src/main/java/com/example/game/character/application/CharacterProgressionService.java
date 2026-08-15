package com.example.game.character.application;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.character.domain.ProgressionBalance;
import com.example.game.character.infrastructure.CharacterEntity;
import com.example.game.character.infrastructure.CharacterRepository;
import com.example.game.inventory.application.InventoryApplicationService;
import com.example.game.inventory.domain.EquipmentValidator;
import com.example.game.shared.api.ApiException;
import com.example.game.telemetry.application.GameTelemetry;
import com.example.game.telemetry.application.GameTelemetryRecorder;
import com.example.game.telemetry.domain.GoldDestroyReason;

@Service
public class CharacterProgressionService {

	private final CharacterRepository characterRepository;
	private final CharacterApplicationService characterApplicationService;
	private final CharacterStateSyncService characterStateSyncService;
	private final CharacterCombatGuard characterCombatGuard;
	private final InventoryApplicationService inventoryApplicationService;
	private final GameTelemetryRecorder gameTelemetryRecorder;
	private final Clock clock;

	public CharacterProgressionService(
			CharacterRepository characterRepository,
			CharacterApplicationService characterApplicationService,
			CharacterStateSyncService characterStateSyncService,
			CharacterCombatGuard characterCombatGuard,
			InventoryApplicationService inventoryApplicationService,
			GameTelemetryRecorder gameTelemetryRecorder,
			Clock clock) {
		this.characterRepository = characterRepository;
		this.characterApplicationService = characterApplicationService;
		this.characterStateSyncService = characterStateSyncService;
		this.characterCombatGuard = characterCombatGuard;
		this.inventoryApplicationService = inventoryApplicationService;
		this.gameTelemetryRecorder = gameTelemetryRecorder;
		this.clock = clock;
	}

	@Transactional
	public CharacterView allocateAttributes(
			UUID accountId,
			int strengthDelta,
			int agilityDelta,
			int enduranceDelta,
			int perceptionDelta) {
		CharacterEntity character = characterRepository.findWithLockByAccountId(accountId)
				.orElseThrow(CharacterErrors::characterNotFound);
		characterCombatGuard.assertNotInActiveCombat(character.getId());
		characterStateSyncService.sync(character);
		try {
			character.allocateAttributes(
					strengthDelta,
					agilityDelta,
					enduranceDelta,
					perceptionDelta,
					Instant.now(clock));
		}
		catch (IllegalArgumentException exception) {
			throw new ApiException(
					"INVALID_ATTRIBUTE_ALLOCATION",
					exception.getMessage(),
					HttpStatus.BAD_REQUEST);
		}
		characterRepository.saveAndFlush(character);
		GameTelemetry.attributesAllocated(
				gameTelemetryRecorder,
				character.getId(),
				strengthDelta,
				agilityDelta,
				enduranceDelta,
				perceptionDelta);
		return characterApplicationService.current(accountId);
	}

	@Transactional
	public CharacterView respec(UUID accountId) {
		CharacterEntity character = characterRepository.findWithLockByAccountId(accountId)
				.orElseThrow(CharacterErrors::characterNotFound);
		characterCombatGuard.assertNotInActiveCombat(character.getId());
		characterStateSyncService.sync(character);
		int cost = ProgressionBalance.respecGoldCost(character.getLevel());
		if (character.getGold() < cost) {
			throw CharacterErrors.insufficientGoldForRespec();
		}
		Instant now = Instant.now(clock);
		if (cost > 0) {
			character.spendGold(cost, now);
		}
		character.respec(now);
		characterRepository.saveAndFlush(character);
		GameTelemetry.goldDestroyed(gameTelemetryRecorder, character.getId(), cost, GoldDestroyReason.RESPEC);
		GameTelemetry.respec(gameTelemetryRecorder, character.getId(), cost, character.getLevel());
		inventoryApplicationService.unequipInvalidEquipment(
				character.getId(),
				new EquipmentValidator.CharacterRequirements(
						character.getLevel(),
						character.getStrength(),
						character.getAgility(),
						character.getEndurance(),
						character.getPerception()));
		return characterApplicationService.current(accountId);
	}
}
