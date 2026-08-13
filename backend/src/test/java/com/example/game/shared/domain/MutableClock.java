package com.example.game.shared.domain;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Mutable clock for integration tests that advance real-world game time without sleeping.
 */
public final class MutableClock extends Clock {

	private final AtomicReference<Instant> instant;
	private final ZoneId zone;

	public MutableClock(Instant start) {
		this(start, ZoneOffset.UTC);
	}

	public MutableClock(Instant start, ZoneId zone) {
		this.instant = new AtomicReference<>(start);
		this.zone = zone;
	}

	public void setInstant(Instant instant) {
		this.instant.set(instant);
	}

	public void advanceSeconds(long seconds) {
		instant.updateAndGet(current -> current.plusSeconds(seconds));
	}

	@Override
	public ZoneId getZone() {
		return zone;
	}

	@Override
	public Clock withZone(ZoneId zone) {
		return new MutableClock(instant.get(), zone);
	}

	@Override
	public Instant instant() {
		return instant.get();
	}
}
