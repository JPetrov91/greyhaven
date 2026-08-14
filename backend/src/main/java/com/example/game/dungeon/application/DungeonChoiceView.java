package com.example.game.dungeon.application;

public record DungeonChoiceView(
		String edgeCode,
		String roomCode,
		String roomName,
		boolean optional
) {
}
