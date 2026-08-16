package com.example.game.sparring.api;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.game.account.infrastructure.AccountPrincipal;
import com.example.game.combat.api.CombatController;
import com.example.game.combat.api.CombatResponse;
import com.example.game.sparring.application.SparringApplicationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/sparring")
public class SparringController {

	private final SparringApplicationService sparringApplicationService;

	public SparringController(SparringApplicationService sparringApplicationService) {
		this.sparringApplicationService = sparringApplicationService;
	}

	@GetMapping("/bots")
	public List<SparringBotResponse> bots() {
		return sparringApplicationService.catalog().stream()
				.map(entry -> new SparringBotResponse(entry.level(), entry.name(), entry.code()))
				.toList();
	}

	@PostMapping("/drills")
	public CombatResponse startDrill(
			@AuthenticationPrincipal AccountPrincipal principal,
			@Valid @RequestBody SparringDrillRequest request) {
		return CombatController.toResponse(
				sparringApplicationService.startDrill(principal.getAccountId(), request.botLevel()));
	}
}
