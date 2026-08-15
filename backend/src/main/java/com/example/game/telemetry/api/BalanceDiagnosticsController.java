package com.example.game.telemetry.api;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.game.telemetry.application.BalanceDiagnosticsService;
import com.example.game.telemetry.application.BalanceDiagnosticsView;

@RestController
@RequestMapping("/api/v1/dev/diagnostics")
@ConditionalOnProperty(name = "greyhaven.diagnostics.enabled", havingValue = "true")
public class BalanceDiagnosticsController {

	private final BalanceDiagnosticsService balanceDiagnosticsService;

	public BalanceDiagnosticsController(BalanceDiagnosticsService balanceDiagnosticsService) {
		this.balanceDiagnosticsService = balanceDiagnosticsService;
	}

	@GetMapping
	public BalanceDiagnosticsView diagnostics() {
		return balanceDiagnosticsService.snapshot();
	}
}
