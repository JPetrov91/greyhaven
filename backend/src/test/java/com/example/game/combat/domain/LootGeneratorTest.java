package com.example.game.combat.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.example.game.shared.domain.ScriptedRandomProvider;

class LootGeneratorTest {

	@Test
	void generatesDropWhenChanceSucceeds() {
		UUID itemId = UUID.fromString("c0000000-0000-4000-8000-000000000007");
		List<LootTableEntry> table = List.of(
				new LootTableEntry(itemId, "WOLF_PELT", 70, 1, 1, false));
		// chancePercent(70) uses nextInt(0,99); 10 < 70 => drop; quantity nextInt(1,1)=1
		ScriptedRandomProvider random = new ScriptedRandomProvider(10, 1);

		List<LootDrop> drops = LootGenerator.generate(table, random);

		assertThat(drops).containsExactly(new LootDrop(itemId, "WOLF_PELT", 1));
	}

	@Test
	void skipsDropWhenChanceFails() {
		UUID itemId = UUID.fromString("c0000000-0000-4000-8000-000000000007");
		List<LootTableEntry> table = List.of(
				new LootTableEntry(itemId, "WOLF_PELT", 70, 1, 1, false));
		ScriptedRandomProvider random = new ScriptedRandomProvider(80);

		assertThat(LootGenerator.generate(table, random)).isEmpty();
	}

	@Test
	void rollsGoldInRange() {
		ScriptedRandomProvider random = new ScriptedRandomProvider(12);

		assertThat(LootGenerator.rollGold(6, 14, random)).isEqualTo(12);
	}
}
