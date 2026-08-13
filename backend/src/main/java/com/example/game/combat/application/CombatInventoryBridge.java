package com.example.game.combat.application;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.example.game.combat.domain.CombatSessionStatus;
import com.example.game.combat.infrastructure.CombatSessionRepository;
import com.example.game.inventory.application.ActiveCombatInventoryBridge;

@Component
public class CombatInventoryBridge implements ActiveCombatInventoryBridge {

	private final CombatSessionRepository combatSessionRepository;
	private final Clock clock;

	public CombatInventoryBridge(CombatSessionRepository combatSessionRepository, Clock clock) {
		this.combatSessionRepository = combatSessionRepository;
		this.clock = clock;
	}

	@Override
	public void syncPlayerHealthIfInCombat(UUID characterId, int currentHealth) {
		combatSessionRepository
				.findWithLockByCharacterIdAndStatus(characterId, CombatSessionStatus.ACTIVE)
				.ifPresent(session -> {
					session.syncPlayerHealth(Math.max(0, currentHealth), Instant.now(clock));
					combatSessionRepository.saveAndFlush(session);
				});
	}
}
