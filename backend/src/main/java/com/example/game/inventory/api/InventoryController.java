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
				EquipmentResponse.from(inventory.equipment()),
				toDerivedStats(inventory.derivedStats()));
	}

	private static InventoryItemResponse toItemResponse(InventoryItemView item) {
		return new InventoryItemResponse(
				item.id(),
				item.definitionId(),
				item.code(),
				item.name(),
				item.displayName(),
				item.description(),
				item.type().name(),
				item.rarity().name(),
				item.quantity(),
				item.requiredLevel(),
				item.requiredStrength(),
				item.requiredAgility(),
				item.requiredEndurance(),
				item.requiredPerception(),
				item.baseValue(),
				item.merchantBuyPrice(),
				item.equipped(),
				item.canEquip(),
				item.twoHanded(),
				item.legacy(),
				item.equipmentSlot() == null ? null : item.equipmentSlot().name(),
				item.weaponFamily() == null ? null : item.weaponFamily().name(),
				item.armorCategory() == null ? null : item.armorCategory().name(),
				item.usable(),
				item.listedQuantity(),
				item.rolledWeaponDamage(),
				item.rolledArmorValue(),
				item.weaponDamage(),
				item.armorValue(),
				item.healAmount(),
				item.accuracy(),
				item.criticalChance(),
				item.dodge(),
				item.strength(),
				item.agility(),
				item.endurance(),
				item.perception(),
				item.staminaCostReduction(),
				item.affixes().stream()
						.map(affix -> new ItemAffixResponse(
								affix.code(),
								affix.kind().name(),
								affix.displayName(),
								affix.stat().name(),
								affix.magnitude()))
						.toList(),
				item.comparison() == null
						? null
						: new ItemComparisonResponse(
								item.comparison().slot().name(),
								item.comparison().equippedItemId(),
								item.comparison().verdict().name(),
								item.comparison().deltas().stream()
										.map(delta -> new StatDeltaResponse(
												delta.stat(),
												delta.equippedValue(),
												delta.candidateValue(),
												delta.delta()))
										.toList()));
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
