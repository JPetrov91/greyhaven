package com.example.game.world.application;

import java.util.UUID;

import com.example.game.world.domain.LocationSafety;

public record DestinationView(
		UUID id,
		String code,
		String name,
		LocationSafety safety,
		Integer recommendedLevelMin,
		Integer recommendedLevelMax
) {
}
