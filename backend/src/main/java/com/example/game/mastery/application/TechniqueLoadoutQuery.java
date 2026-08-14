package com.example.game.mastery.application;

import java.util.List;
import java.util.UUID;

import com.example.game.item.domain.WeaponFamily;

/**
 * Combat-facing mastery reads. Callers receive already-filtered values; do not import this into
 * CombatEngine — pass the returned snapshot as a domain input.
 */
public interface TechniqueLoadoutQuery {

	/**
	 * Active loadout codes compatible with the equipped main-hand. Empty when unarmed or when the
	 * persisted loadout family does not match.
	 */
	List<String> activeTechniqueCodes(UUID characterId);

	int masteryLevel(UUID characterId, WeaponFamily family);
}
