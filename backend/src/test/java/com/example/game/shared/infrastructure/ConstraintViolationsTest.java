package com.example.game.shared.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.SQLException;

import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

class ConstraintViolationsTest {

	@Test
	void identifiesConstraintFromHibernateCause() {
		DataIntegrityViolationException exception = wrap(new ConstraintViolationException(
				"could not execute statement",
				new SQLException("duplicate key"),
				"uq_accounts_email_lower"));

		assertThat(ConstraintViolations.violatedConstraintName(exception)).isEqualTo("uq_accounts_email_lower");
		assertThat(ConstraintViolations.caused(exception, "uq_accounts_email_lower")).isTrue();
		assertThat(ConstraintViolations.caused(exception, "UQ_ACCOUNTS_EMAIL_LOWER")).isTrue();
		assertThat(ConstraintViolations.caused(exception, "uq_characters_name_lower")).isFalse();
	}

	@Test
	void findsConstraintNestedBelowOtherCauses() {
		DataIntegrityViolationException exception = new DataIntegrityViolationException(
				"could not execute statement",
				new IllegalStateException("wrapper", new ConstraintViolationException(
						"could not execute statement",
						new SQLException("duplicate key"),
						"uq_characters_account_id")));

		assertThat(ConstraintViolations.caused(exception, "uq_characters_account_id")).isTrue();
	}

	@Test
	void fallsBackToDriverMessageWhenConstraintNameIsUnavailable() {
		DataIntegrityViolationException exception = new DataIntegrityViolationException(
				"could not execute statement",
				new SQLException("duplicate key value violates unique constraint \"uq_characters_name_lower\""));

		assertThat(ConstraintViolations.violatedConstraintName(exception)).isNull();
		assertThat(ConstraintViolations.caused(exception, "uq_characters_name_lower")).isTrue();
		assertThat(ConstraintViolations.caused(exception, "uq_accounts_email_lower")).isFalse();
	}

	private static DataIntegrityViolationException wrap(Throwable cause) {
		return new DataIntegrityViolationException("could not execute statement", cause);
	}
}
