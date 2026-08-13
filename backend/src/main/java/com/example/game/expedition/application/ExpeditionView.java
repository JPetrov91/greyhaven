package com.example.game.expedition.application;

import java.time.Instant;
import java.util.UUID;

import com.example.game.expedition.domain.ExpeditionStatus;
import com.example.game.expedition.domain.ExpeditionStrategy;
import com.example.game.expedition.domain.ExpeditionType;

public record ExpeditionView(
		UUID id,
		ExpeditionType expeditionType,
		String expeditionName,
		ExpeditionStrategy strategy,
		ExpeditionStatus status,
		Instant startedAt,
		Instant completesAt,
		Instant claimedAt,
		boolean resultReady,
		ExpeditionRewardsView rewards
) {
}
