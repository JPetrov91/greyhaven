package com.example.game.world.api;

public record NpcTalkActionResponse(
		String type,
		String questCode,
		String merchantCode,
		String label
) {
}
