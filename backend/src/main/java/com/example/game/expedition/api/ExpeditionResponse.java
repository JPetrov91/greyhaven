package com.example.game.expedition.api;

import java.time.Instant;
import java.util.UUID;

import com.example.game.expedition.domain.ExpeditionStatus;
import com.example.game.expedition.domain.ExpeditionStrategy;
import com.example.game.expedition.domain.ExpeditionType;

public record ExpeditionResponse(
		UUID id,
		ExpeditionType expeditionType,
		String expeditionName,
		ExpeditionStrategy strategy,
		ExpeditionStatus status,
		Instant startedAt,
		Instant completesAt,
		Instant claimedAt,
		boolean resultReady,
		ExpeditionRewardsResponse rewards
) {
}
