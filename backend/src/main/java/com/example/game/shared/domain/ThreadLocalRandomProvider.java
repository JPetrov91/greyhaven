package com.example.game.shared.domain;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Production {@link RandomProvider} backed by {@link ThreadLocalRandom}.
 */
public final class ThreadLocalRandomProvider implements RandomProvider {

	@Override
	public int nextInt(int minInclusive, int maxInclusive) {
		if (maxInclusive < minInclusive) {
			throw new IllegalArgumentException("maxInclusive must be >= minInclusive");
		}
		return ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
	}
}
