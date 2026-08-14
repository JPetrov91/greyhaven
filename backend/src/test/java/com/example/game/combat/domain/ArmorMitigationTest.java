package com.example.game.combat.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ArmorMitigationTest {

	@Test
	void fiftyArmorMitigatesAboutHalf() {
		int damage = ArmorMitigation.apply(20, 50, 0, false);
		assertThat(damage).isEqualTo(10);
	}

	@Test
	void highArmorStillDealsMinimumDamage() {
		int damage = ArmorMitigation.apply(5, 400, 0, false);
		assertThat(damage).isEqualTo(CombatV2Balance.minDamageAfterArmor());
	}

	@Test
	void guardedReducesDamageAfterArmor() {
		int unguarded = ArmorMitigation.apply(20, 0, 0, false);
		int guarded = ArmorMitigation.apply(20, 0, 0, true);
		assertThat(guarded).isEqualTo((int) Math.round(unguarded * 0.5));
	}
}
