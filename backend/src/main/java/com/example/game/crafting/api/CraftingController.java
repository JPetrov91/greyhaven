package com.example.game.crafting.api;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.game.account.infrastructure.AccountPrincipal;
import com.example.game.crafting.application.CraftingApplicationService;
import com.example.game.crafting.application.CraftingJobView;
import com.example.game.crafting.application.ProfessionView;
import com.example.game.crafting.application.RecipeView;
import com.example.game.crafting.application.SalvageView;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/crafting")
public class CraftingController {

	private final CraftingApplicationService craftingApplicationService;

	public CraftingController(CraftingApplicationService craftingApplicationService) {
		this.craftingApplicationService = craftingApplicationService;
	}

	@GetMapping("/professions")
	public List<ProfessionResponse> professions(@AuthenticationPrincipal AccountPrincipal principal) {
		return craftingApplicationService.professions(principal.getAccountId()).stream()
				.map(CraftingController::toProfession)
				.toList();
	}

	@GetMapping("/recipes")
	public List<RecipeResponse> recipes(@AuthenticationPrincipal AccountPrincipal principal) {
		return craftingApplicationService.recipes(principal.getAccountId()).stream()
				.map(CraftingController::toRecipe)
				.toList();
	}

	@GetMapping("/jobs/current")
	public ResponseEntity<CraftingJobResponse> current(@AuthenticationPrincipal AccountPrincipal principal) {
		CraftingJobView view = craftingApplicationService.current(principal.getAccountId());
		if (view == null) {
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok(toJob(view));
	}

	@PostMapping("/jobs")
	public CraftingJobResponse start(
			@AuthenticationPrincipal AccountPrincipal principal,
			@Valid @RequestBody StartCraftingJobRequest request) {
		return toJob(craftingApplicationService.start(principal.getAccountId(), request.recipeCode()));
	}

	@PostMapping("/jobs/{id}/claim")
	public CraftingJobResponse claim(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("id") UUID jobId) {
		return toJob(craftingApplicationService.claim(principal.getAccountId(), jobId));
	}

	static ProfessionResponse toProfession(ProfessionView view) {
		return new ProfessionResponse(view.profession(), view.rank(), view.xp(), view.xpToNextRank(), view.maxRank());
	}

	static RecipeResponse toRecipe(RecipeView view) {
		return new RecipeResponse(
				view.code(),
				view.name(),
				view.profession(),
				view.requiredProfessionRank(),
				view.requiredCharacterLevel(),
				view.goldCost(),
				view.durationSeconds(),
				view.outputItemCode(),
				view.outputItemName(),
				view.outputQuantity(),
				view.minRarity(),
				view.maxRarity(),
				view.professionXp(),
				view.available(),
				view.unavailableReason(),
				view.inputs().stream()
						.map(input -> new RecipeInputResponse(
								input.itemCode(),
								input.itemName(),
								input.quantity(),
								input.availableQuantity()))
						.toList());
	}

	static CraftingJobResponse toJob(CraftingJobView view) {
		return new CraftingJobResponse(
				view.id(),
				view.profession(),
				view.recipeCode(),
				view.recipeName(),
				view.status(),
				view.startedAt(),
				view.completesAt(),
				view.claimedAt(),
				view.resultReady(),
				view.outputItemCode(),
				view.outputItemName(),
				view.outputQuantity(),
				view.rarity(),
				view.professionXp());
	}

	static SalvageResponse toSalvage(SalvageView view) {
		return new SalvageResponse(
				view.sourceItemCode(),
				view.sourceItemName(),
				view.results().stream()
						.map(result -> new SalvageResultResponse(result.itemCode(), result.itemName(), result.quantity()))
						.toList());
	}
}
