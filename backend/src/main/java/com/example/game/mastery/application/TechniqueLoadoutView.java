package com.example.game.mastery.application;

import java.util.List;

import com.example.game.item.domain.WeaponFamily;

public record TechniqueLoadoutView(
		List<String> slots,
		WeaponFamily loadoutFamily,
		boolean compatibleWithEquippedWeapon
) {
}
