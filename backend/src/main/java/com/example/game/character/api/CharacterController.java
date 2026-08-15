package com.example.game.character.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.game.account.infrastructure.AccountPrincipal;
import com.example.game.character.application.CharacterApplicationService;
import com.example.game.character.application.CharacterProgressionService;
import com.example.game.character.application.CharacterView;
import com.example.game.character.domain.CharacterNameRules;
import com.example.game.character.domain.DerivedCombatStats;
import com.example.game.character.domain.ExperienceProgress;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Validated
@RestController
@RequestMapping("/api/v1")
public class CharacterController {

	private final CharacterApplicationService characterApplicationService;
	private final CharacterProgressionService characterProgressionService;

	public CharacterController(
			CharacterApplicationService characterApplicationService,
			CharacterProgressionService characterProgressionService) {
		this.characterApplicationService = characterApplicationService;
		this.characterProgressionService = characterProgressionService;
	}

	@PostMapping("/characters")
	@ResponseStatus(HttpStatus.CREATED)
	public CharacterResponse create(
			@AuthenticationPrincipal AccountPrincipal principal,
			@Valid @RequestBody CreateCharacterRequest request) {
		return toResponse(characterApplicationService.create(
				principal.getAccountId(),
				request.name(),
				request.gender(),
				request.avatarCode()));
	}

	@GetMapping("/characters/name-available")
	public NameAvailableResponse nameAvailable(
			@RequestParam("name")
			@NotBlank
			@Size(min = 3, max = 24)
			@Pattern(regexp = CharacterNameRules.PATTERN)
			String name) {
		return new NameAvailableResponse(characterApplicationService.isNameAvailable(name));
	}

	@GetMapping("/character")
	public CharacterResponse current(@AuthenticationPrincipal AccountPrincipal principal) {
		return toResponse(characterApplicationService.current(principal.getAccountId()));
	}

	@PostMapping("/character/attributes")
	public CharacterResponse allocateAttributes(
			@AuthenticationPrincipal AccountPrincipal principal,
			@Valid @RequestBody AllocateAttributesRequest request) {
		return toResponse(characterProgressionService.allocateAttributes(
				principal.getAccountId(),
				request.strength(),
				request.agility(),
				request.endurance(),
				request.perception()));
	}

	@PostMapping("/character/respec")
	public CharacterResponse respec(@AuthenticationPrincipal AccountPrincipal principal) {
		return toResponse(characterProgressionService.respec(principal.getAccountId()));
	}

	private static CharacterResponse toResponse(CharacterView character) {
		return new CharacterResponse(
				character.id(),
				character.accountId(),
				character.name(),
				character.gender(),
				character.avatarCode(),
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
				character.arenaRating(),
				character.arenaMarks(),
				character.unspentAttributePoints(),
				character.currentLocationId(),
				toDerivedStats(character.derivedStats()),
				toProgression(character.progression()),
				character.createdAt(),
				character.updatedAt(),
				character.unlocks());
	}

	private static ProgressionResponse toProgression(ExperienceProgress progression) {
		return new ProgressionResponse(
				progression.level(),
				progression.totalExperience(),
				progression.experienceIntoCurrentLevel(),
				progression.experienceRequiredForNextLevel(),
				progression.experienceRemaining(),
				progression.progressPercent(),
				progression.maxLevel());
	}

	private static DerivedStatsResponse toDerivedStats(DerivedCombatStats stats) {
		return new DerivedStatsResponse(
				stats.physicalDamage(),
				stats.accuracy(),
				stats.dodge(),
				stats.criticalChance(),
				stats.armor());
	}
}
