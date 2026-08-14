package com.example.game.mastery.api;

import java.util.List;

public record TechniqueLoadoutResponse(
		List<String> slots,
		String loadoutFamily,
		boolean compatibleWithEquippedWeapon
) {
}
