package com.example.game.combat.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.example.game.shared.domain.ScriptedRandomProvider;

class WeightedPickerTest {

	@Test
	void selectsNothingBucket() {
		UUID wolf = UUID.fromString("d0000000-0000-4000-8000-000000000003");
		List<WeightedPicker.WeightedOption<UUID>> options = List.of(
				new WeightedPicker.WeightedOption<>(wolf, 70),
				new WeightedPicker.WeightedOption<>(null, 30));
		// total 100, roll 85 -> second option (null)
		ScriptedRandomProvider random = new ScriptedRandomProvider(85);

		assertThat(WeightedPicker.pick(options, random)).isNull();
	}

	@Test
	void selectsWeightedMonster() {
		UUID wolf = UUID.fromString("d0000000-0000-4000-8000-000000000003");
		UUID bandit = UUID.fromString("d0000000-0000-4000-8000-000000000004");
		List<WeightedPicker.WeightedOption<UUID>> options = List.of(
				new WeightedPicker.WeightedOption<>(wolf, 70),
				new WeightedPicker.WeightedOption<>(bandit, 20),
				new WeightedPicker.WeightedOption<>(null, 10));
		ScriptedRandomProvider random = new ScriptedRandomProvider(75);

		assertThat(WeightedPicker.pick(options, random)).isEqualTo(bandit);
	}
}
