package com.example.game.world.api;

import java.util.List;

public record NpcTalkResponse(
		String code,
		String name,
		String title,
		String portraitCode,
		String text,
		String merchantCode,
		List<NpcTalkActionResponse> actions
) {
}
