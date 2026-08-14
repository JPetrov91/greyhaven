package com.example.game.dungeon.application;

import com.example.game.dungeon.domain.DungeonRoomKind;
import com.example.game.dungeon.domain.DungeonRoomState;

public record DungeonRoomView(
		String code,
		String name,
		DungeonRoomKind kind,
		DungeonRoomState state
) {
}
