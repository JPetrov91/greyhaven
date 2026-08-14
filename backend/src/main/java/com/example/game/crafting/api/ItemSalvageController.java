package com.example.game.crafting.api;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.game.account.infrastructure.AccountPrincipal;
import com.example.game.crafting.application.CraftingApplicationService;

@RestController
@RequestMapping("/api/v1/items")
public class ItemSalvageController {

	private final CraftingApplicationService craftingApplicationService;

	public ItemSalvageController(CraftingApplicationService craftingApplicationService) {
		this.craftingApplicationService = craftingApplicationService;
	}

	@PostMapping("/{id}/salvage")
	public SalvageResponse salvage(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("id") UUID itemInstanceId) {
		return CraftingController.toSalvage(craftingApplicationService.salvage(principal.getAccountId(), itemInstanceId));
	}
}
