package com.example.game.inventory.domain;

import com.example.game.item.domain.ItemDefinitionData;

public final class EquipmentValidator {

	public enum Failure {
		NOT_EQUIPPABLE,
		LISTED,
		SLOT_MISMATCH,
		TWO_HANDED_WRONG_SLOT,
		TWO_HANDED_BLOCKS_OFF_HAND,
		REQUIREMENTS_NOT_MET
	}

	private EquipmentValidator() {
	}

	public static Failure validate(
			ItemDefinitionData definition,
			boolean listed,
			CharacterRequirements character,
			boolean mainHandIsTwoHanded) {
		if (definition == null || !definition.type().isEquippable()) {
			return Failure.NOT_EQUIPPABLE;
		}
		if (listed) {
			return Failure.LISTED;
		}
		if (definition.equipmentSlot() == null) {
			return Failure.SLOT_MISMATCH;
		}
		if (definition.twoHanded() && definition.equipmentSlot() != EquipmentSlot.MAIN_HAND) {
			return Failure.TWO_HANDED_WRONG_SLOT;
		}
		if (definition.equipmentSlot() == EquipmentSlot.OFF_HAND && mainHandIsTwoHanded) {
			return Failure.TWO_HANDED_BLOCKS_OFF_HAND;
		}
		if (character.level() < definition.requiredLevel()
				|| character.strength() < definition.requiredStrength()
				|| character.agility() < definition.requiredAgility()
				|| character.endurance() < definition.requiredEndurance()
				|| character.perception() < definition.requiredPerception()) {
			return Failure.REQUIREMENTS_NOT_MET;
		}
		return null;
	}

	public static boolean canEquip(
			ItemDefinitionData definition,
			boolean listed,
			CharacterRequirements character,
			boolean mainHandIsTwoHanded) {
		return validate(definition, listed, character, mainHandIsTwoHanded) == null;
	}

	public record CharacterRequirements(
			int level,
			int strength,
			int agility,
			int endurance,
			int perception
	) {
	}
}
