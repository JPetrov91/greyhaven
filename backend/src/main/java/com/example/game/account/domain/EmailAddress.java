package com.example.game.account.domain;

import java.util.Locale;

/**
 * Canonical form of an account email. Every read and write must use the same normalization,
 * otherwise the case-insensitive uniqueness rule cannot be relied upon.
 */
public final class EmailAddress {

	private EmailAddress() {
	}

	public static String normalize(String email) {
		return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
	}
}
