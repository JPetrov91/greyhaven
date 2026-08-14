package com.example.game.inventory.application;

import java.util.Optional;
import java.util.UUID;

import com.example.game.item.domain.WeaponFamily;

/**
 * Main-hand weapon family for other modules. Mastery does not reach inventory persistence.
 */
public interface EquippedWeaponQuery {

	Optional<WeaponFamily> mainHandFamily(UUID characterId);
}
