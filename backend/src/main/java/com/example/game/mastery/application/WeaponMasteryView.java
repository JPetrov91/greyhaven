package com.example.game.mastery.application;

import java.util.List;

import com.example.game.item.domain.WeaponFamily;
import com.example.game.mastery.domain.MasteryProgress;

public record WeaponMasteryView(
		WeaponFamily weaponFamily,
		int level,
		int totalExperience,
		MasteryProgress progress,
		List<String> nextUnlockCodes
) {
}
