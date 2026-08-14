package com.example.game.combat.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class UniqueLootTest {

	@Test
	void keepsRepeatableDropsAndFirstUniqueGrant() {
		UUID potion = UUID.randomUUID();
		UUID signet = UUID.randomUUID();
		List<LootTableEntry> table = List.of(
				new LootTableEntry(potion, "HEALING_POTION", 60, 1, 2, false),
				new LootTableEntry(signet, "WARDENS_SIGNET", 100, 1, 1, true));

		assertThat(UniqueLoot.excludingGranted(table, Set.of()))
				.containsExactlyElementsOf(table);
		assertThat(UniqueLoot.excludingGranted(table, Set.of("WARDENS_SIGNET")))
				.extracting(LootTableEntry::itemCode)
				.containsExactly("HEALING_POTION");
	}
}
