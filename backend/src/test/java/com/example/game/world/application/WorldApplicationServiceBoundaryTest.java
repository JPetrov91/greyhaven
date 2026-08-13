package com.example.game.world.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

/**
 * Guards the modular boundary: world application returns views, not API DTOs.
 */
class WorldApplicationServiceBoundaryTest {

	@Test
	void applicationMethodsReturnViewsNotApiDtos() throws NoSuchMethodException {
		assertThat(WorldApplicationService.class
				.getMethod("currentLocation", UUID.class)
				.getReturnType())
				.isEqualTo(LocationView.class);
		assertThat(WorldApplicationService.class
				.getMethod("move", UUID.class, UUID.class)
				.getReturnType())
				.isEqualTo(LocationView.class);
		assertThat(WorldApplicationService.class
				.getMethod("destinations", UUID.class)
				.getReturnType())
				.isEqualTo(List.class);
		assertThat(WorldApplicationService.class
				.getMethod("nearbyCharacters", UUID.class)
				.getReturnType())
				.isEqualTo(NearbyCharactersView.class);
	}
}
