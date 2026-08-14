package com.example.game.character.domain;

import java.time.Duration;
import java.time.Instant;

import com.example.game.shared.balance.GameBalance;

/**
 * Pure out-of-combat HP/stamina recovery from stored vitals, a recovery timestamp, and elapsed
 * real time. Does not touch persistence or Spring.
 */
public final class CharacterRecovery {

	private CharacterRecovery() {
	}

	public record Result(int currentHealth, int currentStamina) {
	}

	public static Result apply(
			int level,
			int currentHealth,
			int maxHealth,
			int currentStamina,
			int maxStamina,
			Instant lastRecoveryAt,
			Instant now) {
		return apply(
				currentHealth,
				maxHealth,
				currentStamina,
				maxStamina,
				lastRecoveryAt,
				now,
				CharacterRecoveryBalance.ratesForLevel(level));
	}

	public static Result apply(
			int currentHealth,
			int maxHealth,
			int currentStamina,
			int maxStamina,
			Instant lastRecoveryAt,
			Instant now,
			GameBalance.RecoveryBand rates) {
		if (maxHealth < 1 || maxStamina < 1) {
			throw new IllegalArgumentException("max vitals must be at least 1");
		}
		if (currentHealth < 0 || currentStamina < 0) {
			throw new IllegalArgumentException("current vitals must be non-negative");
		}
		if (lastRecoveryAt == null || now == null) {
			throw new IllegalArgumentException("recovery timestamps are required");
		}
		long elapsedMillis = Math.max(0, Duration.between(lastRecoveryAt, now).toMillis());
		double elapsedMinutes = elapsedMillis / 60_000.0;
		int recoveredHealth = recoveredAmount(maxHealth, rates.healthPercentPerMinute(), elapsedMinutes);
		int recoveredStamina = recoveredAmount(maxStamina, rates.staminaPercentPerMinute(), elapsedMinutes);
		int health = Math.min(maxHealth, currentHealth + recoveredHealth);
		int stamina = Math.min(maxStamina, currentStamina + recoveredStamina);
		return new Result(health, stamina);
	}

	private static int recoveredAmount(int maximum, double percentPerMinute, double elapsedMinutes) {
		if (percentPerMinute < 0 || elapsedMinutes <= 0) {
			return 0;
		}
		return (int) Math.floor(maximum * (percentPerMinute / 100.0) * elapsedMinutes);
	}
}
