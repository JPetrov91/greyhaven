package com.example.game.combat.domain;

import java.util.List;

import com.example.game.shared.domain.RandomProvider;

/**
 * Pure weighted selection used by encounter tables.
 */
public final class WeightedPicker {

	public record WeightedOption<T>(T value, int weight) {
	}

	private WeightedPicker() {
	}

	public static <T> T pick(List<WeightedOption<T>> options, RandomProvider random) {
		if (options == null || options.isEmpty()) {
			throw new IllegalArgumentException("options must not be empty");
		}
		int total = 0;
		for (WeightedOption<T> option : options) {
			if (option.weight() < 1) {
				throw new IllegalArgumentException("weight must be >= 1");
			}
			total = Math.addExact(total, option.weight());
		}
		int roll = random.nextInt(1, total);
		int cursor = 0;
		for (WeightedOption<T> option : options) {
			cursor += option.weight();
			if (roll <= cursor) {
				return option.value();
			}
		}
		return options.get(options.size() - 1).value();
	}
}
