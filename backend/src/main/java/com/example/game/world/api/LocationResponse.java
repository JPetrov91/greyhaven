package com.example.game.world.api;

import java.util.List;
import java.util.UUID;

import com.example.game.world.domain.LocationAction;
import com.example.game.world.domain.LocationSafety;

public record LocationResponse(
		UUID id,
		String code,
		String name,
		String description,
		LocationSafety safety,
		String region,
		List<LocationAction> actions
) {
}
