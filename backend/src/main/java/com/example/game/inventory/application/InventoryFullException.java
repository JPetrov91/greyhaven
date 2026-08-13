package com.example.game.inventory.application;

import org.springframework.http.HttpStatus;

import com.example.game.shared.api.ApiException;

/**
 * Raised when a grant would exceed inventory capacity. Typed so callers can react to a full
 * inventory without inspecting error codes.
 */
public class InventoryFullException extends ApiException {

	public InventoryFullException(String message) {
		super("INVENTORY_FULL", message, HttpStatus.CONFLICT);
	}
}
