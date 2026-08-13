package com.example.game.shared.domain;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Test random provider: drains scripted values that fit the requested range, then falls back to
 * {@link ThreadLocalRandom}.
 */
public final class MutableRandomProvider implements RandomProvider {

	private final Deque<Integer> scripted = new ArrayDeque<>();

	public synchronized void queue(Integer... values) {
		scripted.addAll(Arrays.asList(values));
	}

	public synchronized void clear() {
		scripted.clear();
	}

	@Override
	public synchronized int nextInt(int minInclusive, int maxInclusive) {
		while (!scripted.isEmpty()) {
			int value = scripted.removeFirst();
			if (value >= minInclusive && value <= maxInclusive) {
				return value;
			}
		}
		return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
	}
}
