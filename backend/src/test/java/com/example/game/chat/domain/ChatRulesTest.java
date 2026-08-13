package com.example.game.chat.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class ChatRulesTest {

	@Test
	void normalizeTrimsAndUnifiesNewlines() {
		assertThat(ChatRules.normalizeBody("  hello \r\nthere \r ")).isEqualTo("hello \nthere");
		assertThat(ChatRules.normalizeBody(null)).isEmpty();
		assertThat(ChatRules.normalizeBody("   ")).isEmpty();
	}

	@Test
	void acceptableBodiesArePlainTextWithinTheLimit() {
		assertThat(ChatRules.isAcceptableBody("Looking for a wolf pelt.")).isTrue();
		assertThat(ChatRules.isAcceptableBody("HP < 50 and STR > 8")).isTrue();
		assertThat(ChatRules.isAcceptableBody("")).isFalse();
		assertThat(ChatRules.isAcceptableBody("a".repeat(ChatRules.MAX_BODY_LENGTH + 1))).isFalse();
	}

	@Test
	void markupAndControlCharactersAreRejected() {
		assertThat(ChatRules.isPlainText("<script>alert(1)</script>")).isFalse();
		assertThat(ChatRules.isPlainText("hello <b>there</b>")).isFalse();
		assertThat(ChatRules.isPlainText("ok\u0000no")).isFalse();
		assertThat(ChatRules.isPlainText("line\nbreak")).isTrue();
	}

	@Test
	void rateLimitUsesTheInjectableClockInstant() {
		Instant last = Instant.parse("2026-08-14T00:00:00Z");
		assertThat(ChatRules.isRateLimited(null, last)).isFalse();
		assertThat(ChatRules.isRateLimited(last, last)).isTrue();
		assertThat(ChatRules.isRateLimited(last, last.plusSeconds(1))).isTrue();
		assertThat(ChatRules.isRateLimited(last, last.plus(ChatRules.MIN_INTERVAL_BETWEEN_MESSAGES))).isFalse();
	}
}
