package com.example.game.combat.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.game.account.infrastructure.AccountPrincipal;
import com.example.game.combat.application.CombatView;
import com.example.game.combat.application.EncounterApplicationService;
import com.example.game.combat.application.EncounterSearchView;
import com.example.game.combat.application.EncounterView;
import com.example.game.combat.application.MonsterView;

@RestController
@RequestMapping("/api/v1/encounters")
public class EncounterController {

	private final EncounterApplicationService encounterApplicationService;

	public EncounterController(EncounterApplicationService encounterApplicationService) {
		this.encounterApplicationService = encounterApplicationService;
	}

	@GetMapping("/current")
	public ResponseEntity<EncounterSearchResponse> current(
			@AuthenticationPrincipal AccountPrincipal principal) {
		EncounterSearchView view = encounterApplicationService.current(principal.getAccountId());
		if (!view.found()) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(toSearchResponse(view));
	}

	@PostMapping("/search")
	public EncounterSearchResponse search(@AuthenticationPrincipal AccountPrincipal principal) {
		return toSearchResponse(encounterApplicationService.search(principal.getAccountId()));
	}

	private static EncounterSearchResponse toSearchResponse(EncounterSearchView view) {
		return new EncounterSearchResponse(
				view.found(),
				view.encounterId(),
				toMonster(view.monster()));
	}

	@PostMapping("/{id}/fight")
	public CombatResponse fight(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("id") UUID encounterId) {
		return CombatController.toResponse(encounterApplicationService.fight(principal.getAccountId(), encounterId));
	}

	@PostMapping("/{id}/ignore")
	public EncounterResponse ignore(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("id") UUID encounterId) {
		EncounterView view = encounterApplicationService.ignore(principal.getAccountId(), encounterId);
		return new EncounterResponse(view.id(), view.status(), toMonster(view.monster()));
	}

	private static MonsterResponse toMonster(MonsterView monster) {
		if (monster == null) {
			return null;
		}
		return new MonsterResponse(
				monster.id(),
				monster.code(),
				monster.name(),
				monster.level(),
				monster.maxHealth(),
				monster.archetype(),
				monster.tier());
	}
}
