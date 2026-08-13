package com.example.game.world.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Pure movement rule: travel is allowed only when an explicit connection exists.
 */
public final class LocationConnectivity {

	private LocationConnectivity() {
	}

	public static boolean canMove(UUID fromLocationId, UUID toLocationId, boolean connectionExists) {
		Objects.requireNonNull(fromLocationId, "fromLocationId");
		Objects.requireNonNull(toLocationId, "toLocationId");
		if (fromLocationId.equals(toLocationId)) {
			return false;
		}
		return connectionExists;
	}
}
