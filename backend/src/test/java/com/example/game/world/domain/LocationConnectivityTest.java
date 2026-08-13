package com.example.game.world.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class LocationConnectivityTest {

	@Test
	void allowsMoveOnlyWhenConnectedAndDistinct() {
		UUID from = UUID.fromString("a0000000-0000-4000-8000-000000000001");
		UUID to = UUID.fromString("a0000000-0000-4000-8000-000000000005");

		assertThat(LocationConnectivity.canMove(from, to, true)).isTrue();
		assertThat(LocationConnectivity.canMove(from, to, false)).isFalse();
		assertThat(LocationConnectivity.canMove(from, from, true)).isFalse();
	}

	@Test
	void rejectsNullEndpoints() {
		UUID location = UUID.randomUUID();
		assertThatThrownBy(() -> LocationConnectivity.canMove(null, location, true))
				.isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> LocationConnectivity.canMove(location, null, true))
				.isInstanceOf(NullPointerException.class);
	}
}
