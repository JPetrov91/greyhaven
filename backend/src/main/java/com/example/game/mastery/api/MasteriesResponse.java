package com.example.game.mastery.api;

import java.util.List;

public record MasteriesResponse(
		String equippedWeaponFamily,
		List<WeaponMasteryResponse> masteries
) {
}
