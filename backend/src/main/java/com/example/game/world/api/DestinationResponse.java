package com.example.game.world.api;

import java.util.UUID;

import com.example.game.world.domain.LocationSafety;

public record DestinationResponse(
		UUID id,
		String code,
		String name,
		LocationSafety safety,
		Integer recommendedLevelMin,
		Integer recommendedLevelMax
) {
}
