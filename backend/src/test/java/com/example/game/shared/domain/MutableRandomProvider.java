package com.example.game.shared.domain;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Random;

/**
 * Test random provider for flows that span many rolls. Queued values force specific outcomes; any
 * remaining roll comes from a fixed-seed generator, so a run never depends on wall-clock
 * randomness. {@link #clear()} restores the seed, giving every test the same starting sequence.
 */
public final class MutableRandomProvider implements RandomProvider {

	private static final long SEED = 20260813L;

	private final Deque<Integer> scripted = new ArrayDeque<>();
	private Random fallback = new Random(SEED);

	public synchronized void queue(Integer... values) {
		scripted.addAll(Arrays.asList(values));
	}

	public synchronized void clear() {
		scripted.clear();
		fallback = new Random(SEED);
	}

	public synchronized int remainingScripted() {
		return scripted.size();
	}

	@Override
	public synchronized int nextInt(int minInclusive, int maxInclusive) {
		if (maxInclusive < minInclusive) {
			throw new IllegalArgumentException("maxInclusive must be >= minInclusive");
		}
		if (!scripted.isEmpty()) {
			int value = scripted.removeFirst();
			if (value < minInclusive || value > maxInclusive) {
				throw new IllegalStateException(
						"scripted value " + value + " outside [" + minInclusive + "," + maxInclusive + "]");
			}
			return value;
		}
		return fallback.nextInt(minInclusive, maxInclusive + 1);
	}
}
