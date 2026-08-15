package com.example.game.telemetry.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.example.game.TestcontainersConfiguration;
import com.example.game.telemetry.domain.TelemetryEventType;

import jakarta.servlet.http.Cookie;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TelemetryDiagnosticsIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private Cookie csrfCookie;

	@BeforeEach
	void loadCsrfCookie() throws Exception {
		MvcResult bootstrap = mockMvc.perform(get("/api/v1/bootstrap"))
				.andExpect(status().isOk())
				.andReturn();
		csrfCookie = bootstrap.getResponse().getCookie("XSRF-TOKEN");
		assertThat(csrfCookie).isNotNull();
	}

	@Test
	void characterCreationEmitsSafeEconomyEventsAndDiagnostics() throws Exception {
		String email = "tel-" + System.nanoTime() + "@greyhaven.test";
		String name = uniqueName("Tel");
		MockHttpSession session = register(email);

		mockMvc.perform(get("/api/v1/dev/diagnostics"))
				.andExpect(status().isUnauthorized());

		MvcResult created = mockMvc.perform(withCsrf(post("/api/v1/characters")).session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"name":"%s"}
								""".formatted(name)))
				.andExpect(status().isCreated())
				.andReturn();
		refreshCsrfCookie(created);

		UUID characterId = UUID.fromString(jdbcTemplate.queryForObject(
				"select id from characters where name = ?",
				String.class,
				name));

		List<Map<String, Object>> events = jdbcTemplate.queryForList(
				"""
						select event_type, payload::text as payload
						from game_telemetry_events
						where character_id = ?
						order by occurred_at
						""",
				characterId);
		assertThat(events).isNotEmpty();
		assertThat(events.stream().map(row -> row.get("event_type")))
				.contains(TelemetryEventType.GOLD_CREATED.name(), TelemetryEventType.ITEM_CREATED.name());
		for (Map<String, Object> event : events) {
			String payload = String.valueOf(event.get("payload")).toLowerCase();
			assertThat(payload).doesNotContain(email.toLowerCase());
			assertThat(payload).doesNotContain("password");
			assertThat(payload).doesNotContain("\"name\"");
			assertThat(payload).doesNotContain("account");
		}

		mockMvc.perform(get("/api/v1/dev/diagnostics").session(session))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.catalog.monsterCount").value(org.hamcrest.Matchers.greaterThan(0)))
				.andExpect(jsonPath("$.goldCreatedTotal").value(org.hamcrest.Matchers.greaterThanOrEqualTo(100)))
				.andExpect(jsonPath("$.xpProgression").exists())
				.andExpect(jsonPath("$.pveOutcomes").exists())
				.andExpect(jsonPath("$.marketVolume").exists())
				.andExpect(jsonPath("$.craftingOutput").isArray());

		mockMvc.perform(withCsrf(post("/api/v1/character/attributes")).session(session)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"strengthDelta":0,"agilityDelta":0,"enduranceDelta":0,"perceptionDelta":0}
								"""))
				.andExpect(status().isBadRequest());
	}

	private MockHttpSession register(String email) throws Exception {
		MvcResult result = mockMvc.perform(withCsrf(post("/api/v1/auth/register"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"%s","password":"password123"}
								""".formatted(email)))
				.andExpect(status().isCreated())
				.andReturn();
		refreshCsrfCookie(result);
		MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
		assertThat(session).isNotNull();
		return session;
	}

	private static String uniqueName(String prefix) {
		return prefix + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
	}

	private MockHttpServletRequestBuilder withCsrf(MockHttpServletRequestBuilder builder) {
		return builder.header("X-XSRF-TOKEN", csrfCookie.getValue()).cookie(csrfCookie);
	}

	private void refreshCsrfCookie(MvcResult result) {
		Cookie latest = null;
		for (Cookie cookie : result.getResponse().getCookies()) {
			if ("XSRF-TOKEN".equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
				latest = cookie;
			}
		}
		if (latest != null) {
			csrfCookie = latest;
		}
	}
}
