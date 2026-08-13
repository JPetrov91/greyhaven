package com.example.game.shared.infrastructure;

import java.util.Locale;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Identifies which database constraint rejected a write.
 *
 * <p>Application-level pre-checks such as "does this email already exist" are read-then-write
 * races. The database constraint is the real guarantee, and this class lets a service turn the
 * resulting violation into the same domain error the pre-check would have produced.
 */
public final class ConstraintViolations {

	private ConstraintViolations() {
	}

	public static boolean caused(DataIntegrityViolationException exception, String constraintName) {
		String violated = violatedConstraintName(exception);
		if (violated != null) {
			return violated.equalsIgnoreCase(constraintName);
		}
		String message = exception.getMostSpecificCause().getMessage();
		return message != null && message.toLowerCase(Locale.ROOT).contains(constraintName.toLowerCase(Locale.ROOT));
	}

	public static String violatedConstraintName(DataIntegrityViolationException exception) {
		Throwable cause = exception;
		while (cause != null) {
			if (cause instanceof ConstraintViolationException violation) {
				return violation.getConstraintName();
			}
			cause = (cause.getCause() == cause) ? null : cause.getCause();
		}
		return null;
	}
}
