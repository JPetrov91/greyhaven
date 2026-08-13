package com.example.game.account.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EmailAddressTest {

	@Test
	void normalizesCaseAndSurroundingWhitespace() {
		assertThat(EmailAddress.normalize("  Hero@Greyhaven.TEST ")).isEqualTo("hero@greyhaven.test");
		assertThat(EmailAddress.normalize("hero@greyhaven.test")).isEqualTo("hero@greyhaven.test");
	}

	@Test
	void toleratesNull() {
		assertThat(EmailAddress.normalize(null)).isNull();
	}
}
