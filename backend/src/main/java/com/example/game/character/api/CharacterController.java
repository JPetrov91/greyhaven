package com.example.game.character.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.game.account.infrastructure.AccountPrincipal;
import com.example.game.character.application.CharacterApplicationService;
import com.example.game.character.application.CharacterView;
import com.example.game.character.domain.DerivedCombatStats;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class CharacterController {

	private final CharacterApplicationService characterApplicationService;

	public CharacterController(CharacterApplicationService characterApplicationService) {
		this.characterApplicationService = characterApplicationService;
	}

	@PostMapping("/characters")
	@ResponseStatus(HttpStatus.CREATED)
	public CharacterResponse create(
			@AuthenticationPrincipal AccountPrincipal principal,
			@Valid @RequestBody CreateCharacterRequest request) {
		return toResponse(characterApplicationService.create(principal.getAccountId(), request.name()));
	}

	@GetMapping("/character")
	public CharacterResponse current(@AuthenticationPrincipal AccountPrincipal principal) {
		return toResponse(characterApplicationService.current(principal.getAccountId()));
	}

	private static CharacterResponse toResponse(CharacterView character) {
		return new CharacterResponse(
				character.id(),
				character.accountId(),
				character.name(),
				character.level(),
				character.experience(),
				character.strength(),
				character.agility(),
				character.endurance(),
				character.perception(),
				character.currentHealth(),
				character.maxHealth(),
				character.currentStamina(),
				character.maxStamina(),
				character.gold(),
				character.currentLocationId(),
				toDerivedStats(character.derivedStats()),
				character.createdAt(),
				character.updatedAt());
	}

	private static DerivedStatsResponse toDerivedStats(DerivedCombatStats stats) {
		return new DerivedStatsResponse(
				stats.maxHealth(),
				stats.maxStamina(),
				stats.physicalDamage(),
				stats.accuracy(),
				stats.dodge(),
				stats.criticalChance(),
				stats.armor());
	}
}
