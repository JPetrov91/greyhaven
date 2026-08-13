package com.example.game.shared.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.example.game.TestcontainersConfiguration;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HealthAndBootstrapIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void healthEndpointIsAvailable() throws Exception {
		mockMvc.perform(get("/actuator/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"));
	}

	@Test
	void bootstrapEndpointIsAvailableAndSetsCsrfCookie() throws Exception {
		mockMvc.perform(get("/api/v1/bootstrap"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.application").value("greyhaven"))
				.andExpect(jsonPath("$.status").value("ready"))
				.andExpect(cookie().exists("XSRF-TOKEN"))
				.andExpect(cookie().httpOnly("XSRF-TOKEN", false));
	}

	@Test
	void unknownApiPathsAreDenied() throws Exception {
		mockMvc.perform(get("/api/v1/unknown"))
				.andExpect(status().isForbidden());
	}

	@Test
	void flywayBaselineMigrationWasApplied() {
		Integer flywayCount = jdbcTemplate.queryForObject(
				"select count(*) from flyway_schema_history where version = '1' and success = true",
				Integer.class);
		assertThat(flywayCount).isEqualTo(1);

		String bootstrapVersion = jdbcTemplate.queryForObject(
				"select value from schema_meta where key = 'bootstrap_version'",
				String.class);
		assertThat(bootstrapVersion).isEqualTo("1");
	}
}
