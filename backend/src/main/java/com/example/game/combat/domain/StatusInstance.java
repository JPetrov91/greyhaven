package com.example.game.combat.domain;

public record StatusInstance(StatusType type, int stacks, int remainingRounds) {

	public StatusInstance {
		if (type == null) {
			throw new IllegalArgumentException("type is required");
		}
		if (stacks < 0) {
			throw new IllegalArgumentException("stacks must be non-negative");
		}
		if (remainingRounds < 0) {
			throw new IllegalArgumentException("remainingRounds must be non-negative");
		}
	}
}
