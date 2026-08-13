package com.example.game.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.example.game.item.domain.ItemType;

class EquipmentSlotTest {

	@Test
	void mapsEquippableItemTypesToSlots() {
		assertThat(EquipmentSlot.forItemType(ItemType.WEAPON)).isEqualTo(EquipmentSlot.MAIN_HAND);
		assertThat(EquipmentSlot.forItemType(ItemType.ARMOR)).isEqualTo(EquipmentSlot.CHEST);
	}

	@Test
	void prefersStoredSlotOverItemTypeFallback() {
		assertThat(EquipmentSlot.forDefinition(EquipmentSlot.HEAD, ItemType.ARMOR))
				.isEqualTo(EquipmentSlot.HEAD);
		assertThat(EquipmentSlot.forDefinition(null, ItemType.WEAPON)).isEqualTo(EquipmentSlot.MAIN_HAND);
		assertThat(EquipmentSlot.forDefinition(null, ItemType.ARMOR)).isEqualTo(EquipmentSlot.CHEST);
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
