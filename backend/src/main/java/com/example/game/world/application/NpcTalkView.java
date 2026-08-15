package com.example.game.world.application;

import java.util.List;

public record NpcTalkView(
		String code,
		String name,
		String title,
		String portraitCode,
		String text,
		String merchantCode,
		List<NpcTalkActionView> actions
) {
}
