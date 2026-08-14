package com.example.game.dungeon.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class DungeonConnectivityTest {

	@Test
	void courtyardBranchMustMatchAnEdge() {
		List<DungeonConnectivity.DungeonEdge> edges = List.of(
				new DungeonConnectivity.DungeonEdge("COURTYARD", "ARMORY", "ARMORY"),
				new DungeonConnectivity.DungeonEdge("COURTYARD", "PRISON", "PRISON"));
		assertThat(DungeonConnectivity.canAdvance("COURTYARD", "ARMORY", "ARMORY", edges)).isTrue();
		assertThat(DungeonConnectivity.canAdvance("COURTYARD", "PRISON", "ARMORY", edges)).isFalse();
		assertThat(DungeonConnectivity.canAdvance("COURTYARD", "THRONE", "CONTINUE", edges)).isFalse();
	}
}
