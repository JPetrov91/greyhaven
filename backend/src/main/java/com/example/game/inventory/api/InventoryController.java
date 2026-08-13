package com.example.game.inventory.api;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.game.account.infrastructure.AccountPrincipal;
import com.example.game.character.api.DerivedStatsResponse;
import com.example.game.character.domain.DerivedCombatStats;
import com.example.game.inventory.application.InventoryItemView;
import com.example.game.inventory.application.InventoryApplicationService;
import com.example.game.inventory.application.InventoryView;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

	private final InventoryApplicationService inventoryApplicationService;

	public InventoryController(InventoryApplicationService inventoryApplicationService) {
		this.inventoryApplicationService = inventoryApplicationService;
	}

	@GetMapping
	public InventoryResponse getInventory(@AuthenticationPrincipal AccountPrincipal principal) {
		return toResponse(inventoryApplicationService.getInventory(principal.getAccountId()));
	}

	@PostMapping("/{itemId}/equip")
	public InventoryResponse equip(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("itemId") UUID itemId) {
		return toResponse(inventoryApplicationService.equip(principal.getAccountId(), itemId));
	}

	@PostMapping("/{itemId}/unequip")
	public InventoryResponse unequip(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("itemId") UUID itemId) {
		return toResponse(inventoryApplicationService.unequip(principal.getAccountId(), itemId));
	}

	@PostMapping("/{itemId}/use")
	public InventoryResponse use(
			@AuthenticationPrincipal AccountPrincipal principal,
			@PathVariable("itemId") UUID itemId) {
		return toResponse(inventoryApplicationService.use(principal.getAccountId(), itemId));
	}

	private static InventoryResponse toResponse(InventoryView inventory) {
		return new InventoryResponse(
				inventory.capacity(),
				inventory.usedSlots(),
				inventory.items().stream().map(InventoryController::toItemResponse).toList(),
				new EquipmentResponse(
						inventory.equipment().weaponItemId(),
						inventory.equipment().armorItemId()),
				toDerivedStats(inventory.derivedStats()));
	}

	private static InventoryItemResponse toItemResponse(InventoryItemView item) {
		return new InventoryItemResponse(
				item.id(),
				item.definitionId(),
				item.code(),
				item.name(),
				item.description(),
				item.type().name(),
				item.rarity().name(),
				item.quantity(),
				item.requiredLevel(),
				item.baseValue(),
				item.equipped(),
				item.equipmentSlot() == null ? null : item.equipmentSlot().name(),
				item.weaponDamage(),
				item.armorValue(),
				item.healAmount());
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
