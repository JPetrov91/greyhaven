package com.example.game.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.example.game.item.domain.ItemType;

class EquipmentSlotTest {

	@Test
	void mapsEquippableItemTypesToSlots() {
		assertThat(EquipmentSlot.forItemType(ItemType.WEAPON)).isEqualTo(EquipmentSlot.WEAPON);
		assertThat(EquipmentSlot.forItemType(ItemType.ARMOR)).isEqualTo(EquipmentSlot.ARMOR);
	}

	@Test
	void rejectsItemTypesThatHaveNoSlot() {
		assertThatThrownBy(() -> EquipmentSlot.forItemType(ItemType.CONSUMABLE))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("CONSUMABLE");
		assertThatThrownBy(() -> EquipmentSlot.forItemType(ItemType.MATERIAL))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("MATERIAL");
	}
}
