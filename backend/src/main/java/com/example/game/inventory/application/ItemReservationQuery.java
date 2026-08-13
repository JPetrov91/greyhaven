package com.example.game.inventory.application;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Quantity of an owned item currently reserved by an active marketplace listing.
 */
public interface ItemReservationQuery {

	int reservedQuantity(UUID itemInstanceId);

	/**
	 * Reserved quantities for several instances at once, so rendering an inventory does not cost
	 * one query per slot. Instances without a reservation are absent from the result.
	 */
	Map<UUID, Integer> reservedQuantities(Collection<UUID> itemInstanceIds);
}
