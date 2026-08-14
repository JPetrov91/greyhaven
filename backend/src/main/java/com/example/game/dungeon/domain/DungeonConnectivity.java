package com.example.game.dungeon.domain;

import java.util.List;
import java.util.Objects;

/**
 * Pure dungeon movement: an edge must exist from the current room.
 */
public final class DungeonConnectivity {

	private DungeonConnectivity() {
	}

	public static boolean canAdvance(String fromRoomCode, String toRoomCode, String edgeCode, List<DungeonEdge> edges) {
		Objects.requireNonNull(fromRoomCode, "fromRoomCode");
		Objects.requireNonNull(toRoomCode, "toRoomCode");
		Objects.requireNonNull(edgeCode, "edgeCode");
		if (edges == null) {
			return false;
		}
		return edges.stream().anyMatch(edge -> edge.fromRoomCode().equals(fromRoomCode)
				&& edge.toRoomCode().equals(toRoomCode)
				&& edge.edgeCode().equals(edgeCode));
	}

	public record DungeonEdge(String fromRoomCode, String toRoomCode, String edgeCode) {
	}
}
