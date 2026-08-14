package com.example.game.inventory.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class ComparisonVerdictTest {

	@Test
	void classifiesServerDeltasWithoutAPowerScore() {
		assertThat(ComparisonVerdict.fromDeltas(List.of(
				new StatDeltaView("Damage", 6, 13, 7)))).isEqualTo(ComparisonVerdict.UPGRADE);
		assertThat(ComparisonVerdict.fromDeltas(List.of(
				new StatDeltaView("Damage", 13, 6, -7)))).isEqualTo(ComparisonVerdict.DOWNGRADE);
		assertThat(ComparisonVerdict.fromDeltas(List.of(
				new StatDeltaView("Damage", 6, 13, 7),
				new StatDeltaView("Armor", 4, 2, -2)))).isEqualTo(ComparisonVerdict.MIXED);
		assertThat(ComparisonVerdict.fromDeltas(List.of())).isEqualTo(ComparisonVerdict.SAME);
	}
}
