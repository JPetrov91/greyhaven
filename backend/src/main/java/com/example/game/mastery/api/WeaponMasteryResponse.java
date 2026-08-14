package com.example.game.mastery.api;

import java.util.List;

public record WeaponMasteryResponse(
		String weaponFamily,
		int level,
		int totalExperience,
		MasteryProgressResponse progress,
		List<String> nextUnlockCodes
) {
}
