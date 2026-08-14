package com.example.game.inventory.application;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.game.character.application.EquippedBonusProvider;
import com.example.game.character.application.EquippedBonuses;

@Component
public class InventoryEquippedBonusProvider implements EquippedBonusProvider {

	private final InventoryApplicationService inventoryApplicationService;

	public InventoryEquippedBonusProvider(InventoryApplicationService inventoryApplicationService) {
		this.inventoryApplicationService = inventoryApplicationService;
	}

	@Override
	@Transactional(readOnly = true)
	public EquippedBonuses bonusesFor(UUID characterId) {
		InventoryApplicationService.EquippedBonusesSnapshot snapshot =
				inventoryApplicationService.equippedBonuses(characterId);
		return new EquippedBonuses(
				snapshot.weaponDamage(),
				snapshot.armorValue(),
				snapshot.accuracy(),
				snapshot.dodge(),
				snapshot.criticalChance(),
				snapshot.strength(),
				snapshot.agility(),
				snapshot.endurance(),
				snapshot.perception(),
				snapshot.staminaCostReduction());
	}
}
