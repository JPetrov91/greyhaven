package com.example.game.shared.api;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class BootstrapController {

	private final Clock clock;

	public BootstrapController(Clock clock) {
		this.clock = clock;
	}

	@GetMapping("/bootstrap")
	public Map<String, Object> bootstrap() {
		return Map.of(
				"application", "greyhaven",
				"status", "ready",
				"timestamp", Instant.now(clock).toString());
	}
}
