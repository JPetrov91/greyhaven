package com.example.game.world.api;

import java.util.List;

public record NpcResponse(
		String code,
		String name,
		String title,
		String description,
		String greeting,
		String portraitCode,
		String locationCode,
		String merchantCode,
		List<String> interactions,
		List<String> questBadges
) {
}
