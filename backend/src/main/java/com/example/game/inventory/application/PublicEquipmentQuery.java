package com.example.game.inventory.application;

import java.util.List;
import java.util.UUID;

/**
 * Public equipped-item projection for inspection and PvP snapshots.
 */
public interface PublicEquipmentQuery {

	List<PublicEquippedItemView> equippedItems(UUID characterId);

	HealingPotionStock healingPotionStock(UUID characterId);
}
