package com.example.game.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.example.game.item.domain.ItemDefinitionData;
import com.example.game.item.domain.ItemRarity;
import com.example.game.item.domain.ItemType;
import com.example.game.item.domain.WeaponFamily;

class EquipmentValidatorTest {

	private static final EquipmentValidator.CharacterRequirements STARTER =
			new EquipmentValidator.CharacterRequirements(1, 5, 5, 5, 5);

	@Test
	void acceptsMatchingSlotWhenRequirementsMet() {
		assertThat(EquipmentValidator.validate(sword(), false, STARTER, false)).isNull();
	}

	@Test
	void rejectsConsumables() {
		ItemDefinitionData potion = new ItemDefinitionData(
				"HEALING_POTION",
				"Healing Potion",
				ItemType.CONSUMABLE,
				ItemRarity.COMMON,
				false,
				null,
				false,
				null,
				null,
				null,
				null,
				1,
				0,
				0,
				0,
				0);

		assertThat(EquipmentValidator.validate(potion, false, STARTER, false))
				.isEqualTo(EquipmentValidator.Failure.NOT_EQUIPPABLE);
	}

	@Test
	void rejectsListedItems() {
		assertThat(EquipmentValidator.validate(sword(), true, STARTER, false))
				.isEqualTo(EquipmentValidator.Failure.LISTED);
	}

	@Test
	void twoHandedWeaponMustUseMainHand() {
		ItemDefinitionData bow = new ItemDefinitionData(
				"HUNTING_BOW",
				"Hunting Bow",
				ItemType.WEAPON,
				ItemRarity.COMMON,
				false,
				EquipmentSlot.OFF_HAND,
				true,
				WeaponFamily.BOW,
				null,
				8,
				null,
				1,
				0,
				0,
				0,
				0);

		assertThat(EquipmentValidator.validate(bow, false, STARTER, false))
				.isEqualTo(EquipmentValidator.Failure.TWO_HANDED_WRONG_SLOT);
	}

	@Test
	void offHandFailsWhileTwoHandedWeaponIsEquipped() {
		ItemDefinitionData shield = new ItemDefinitionData(
				"WOODEN_BUCKLER",
				"Wooden Buckler",
				ItemType.ARMOR,
				ItemRarity.COMMON,
				false,
				EquipmentSlot.OFF_HAND,
				false,
				null,
				com.example.game.item.domain.ArmorCategory.LIGHT,
				null,
				2,
				1,
				0,
				0,
				0,
				0);

		assertThat(EquipmentValidator.validate(shield, false, STARTER, true))
				.isEqualTo(EquipmentValidator.Failure.TWO_HANDED_BLOCKS_OFF_HAND);
	}

	@Test
	void rejectsUnmetAttributeRequirements() {
		ItemDefinitionData helm = new ItemDefinitionData(
				"IRON_HELM",
				"Iron Helm",
				ItemType.ARMOR,
				ItemRarity.UNCOMMON,
				false,
				EquipmentSlot.HEAD,
				false,
				null,
				com.example.game.item.domain.ArmorCategory.HEAVY,
				null,
				4,
				1,
				8,
				0,
				6,
				0);

		assertThat(EquipmentValidator.validate(helm, false, STARTER, false))
				.isEqualTo(EquipmentValidator.Failure.REQUIREMENTS_NOT_MET);
		assertThat(EquipmentValidator.canEquip(helm, false, STARTER, false)).isFalse();
	}

	private static ItemDefinitionData sword() {
		return new ItemDefinitionData(
				"RUSTY_SWORD",
				"Rusty Sword",
				ItemType.WEAPON,
				ItemRarity.COMMON,
				true,
				EquipmentSlot.MAIN_HAND,
				false,
				WeaponFamily.SWORD,
				null,
				6,
				null,
				1,
				0,
				0,
				0,
				0);
	}
}
