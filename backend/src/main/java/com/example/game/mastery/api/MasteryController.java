package com.example.game.mastery.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.game.account.infrastructure.AccountPrincipal;
import com.example.game.mastery.application.MasteriesView;
import com.example.game.mastery.application.MasteryApplicationService;
import com.example.game.mastery.application.TechniqueDefinitionView;
import com.example.game.mastery.application.TechniqueLoadoutView;
import com.example.game.mastery.application.TechniquesView;
import com.example.game.mastery.application.WeaponMasteryView;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/character")
public class MasteryController {

	private final MasteryApplicationService masteryApplicationService;

	public MasteryController(MasteryApplicationService masteryApplicationService) {
		this.masteryApplicationService = masteryApplicationService;
	}

	@GetMapping("/masteries")
	public MasteriesResponse masteries(@AuthenticationPrincipal AccountPrincipal principal) {
		return toMasteries(masteryApplicationService.masteries(principal.getAccountId()));
	}

	@GetMapping("/techniques")
	public TechniquesResponse techniques(@AuthenticationPrincipal AccountPrincipal principal) {
		return toTechniques(masteryApplicationService.techniques(principal.getAccountId()));
	}

	@PutMapping("/technique-loadout")
	public TechniquesResponse replaceLoadout(
			@AuthenticationPrincipal AccountPrincipal principal,
			@Valid @RequestBody UpdateTechniqueLoadoutRequest request) {
		return toTechniques(masteryApplicationService.replaceLoadout(principal.getAccountId(), request.slots()));
	}

	private static MasteriesResponse toMasteries(MasteriesView view) {
		return new MasteriesResponse(
				view.equippedWeaponFamily() == null ? null : view.equippedWeaponFamily().name(),
				view.masteries().stream().map(MasteryController::toMastery).toList());
	}

	private static WeaponMasteryResponse toMastery(WeaponMasteryView view) {
		return new WeaponMasteryResponse(
				view.weaponFamily().name(),
				view.level(),
				view.totalExperience(),
				MasteryProgressResponse.from(view.progress()),
				view.nextUnlockCodes());
	}

	private static TechniquesResponse toTechniques(TechniquesView view) {
		return new TechniquesResponse(
				view.equippedWeaponFamily() == null ? null : view.equippedWeaponFamily().name(),
				view.techniques().stream().map(MasteryController::toTechnique).toList(),
				toLoadout(view.loadout()));
	}

	private static TechniqueDefinitionResponse toTechnique(TechniqueDefinitionView view) {
		return new TechniqueDefinitionResponse(
				view.code(),
				view.displayName(),
				view.description(),
				view.weaponFamily().name(),
				view.unlockMasteryLevel(),
				view.kind().name(),
				view.unlocked(),
				view.staminaCost(),
				view.accuracyModifier(),
				view.damagePercentModifier(),
				view.appliesStatus(),
				view.tags());
	}

	private static TechniqueLoadoutResponse toLoadout(TechniqueLoadoutView view) {
		return new TechniqueLoadoutResponse(
				view.slots(),
				view.loadoutFamily() == null ? null : view.loadoutFamily().name(),
				view.compatibleWithEquippedWeapon());
	}
}
