package com.example.game.world.application;

import java.util.List;

public record NpcView(
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
