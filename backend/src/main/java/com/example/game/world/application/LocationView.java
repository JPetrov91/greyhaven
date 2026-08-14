package com.example.game.world.application;

import java.util.List;
import java.util.UUID;

import com.example.game.world.domain.LocationAction;
import com.example.game.world.domain.LocationSafety;

public record LocationView(
		UUID id,
		String code,
		String name,
		String description,
		LocationSafety safety,
		String region,
		Integer recommendedLevelMin,
		Integer recommendedLevelMax,
		List<LocationAction> actions
) {
}
