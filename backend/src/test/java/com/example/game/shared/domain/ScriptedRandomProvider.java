package com.example.game.shared.domain;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * Deterministic {@link RandomProvider} for unit tests. Values are consumed in order from
 * {@link #nextInt(int, int)}.
 */
public final class ScriptedRandomProvider implements RandomProvider {

	private final Deque<Integer> values;

	public ScriptedRandomProvider(Integer... values) {
		this.values = new ArrayDeque<>(Arrays.asList(values));
	}

	@Override
	public int nextInt(int minInclusive, int maxInclusive) {
		if (values.isEmpty()) {
			throw new IllegalStateException("no more scripted random values");
		}
		int value = values.removeFirst();
		if (value < minInclusive || value > maxInclusive) {
			throw new IllegalStateException(
					"scripted value " + value + " outside [" + minInclusive + "," + maxInclusive + "]");
		}
		return value;
	}
}
