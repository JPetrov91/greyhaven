package com.example.game.dungeon.application;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.example.game.dungeon.domain.DungeonRunStatus;
import com.example.game.dungeon.infrastructure.DungeonRunRepository;
import com.example.game.world.application.CharacterTravelGuard;

@Component
@Order(20)
public class DungeonTravelGuard implements CharacterTravelGuard {

	private final DungeonRunRepository dungeonRunRepository;
	private final Clock clock;

	public DungeonTravelGuard(DungeonRunRepository dungeonRunRepository, Clock clock) {
		this.dungeonRunRepository = dungeonRunRepository;
		this.clock = clock;
	}

	@Override
	public void assertCanTravel(UUID characterId) {
		dungeonRunRepository.findWithLockByCharacterIdAndStatus(characterId, DungeonRunStatus.ACTIVE)
				.ifPresent(run -> {
					if (!run.isPaused()) {
						run.pause(Instant.now(clock));
						dungeonRunRepository.saveAndFlush(run);
					}
				});
	}
}
