package com.example.game.item.infrastructure;

import com.example.game.item.domain.AffixKind;
import com.example.game.item.domain.AffixStat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "affix_definitions")
public class AffixDefinitionEntity {

	@Id
	@Column(length = 64)
	private String code;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private AffixKind kind;

	@Column(name = "display_name", nullable = false, length = 64)
	private String displayName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private AffixStat stat;

	@Column(name = "magnitude_min", nullable = false)
	private int magnitudeMin;

	@Column(name = "magnitude_max", nullable = false)
	private int magnitudeMax;

	@Column(name = "allowed_item_types", nullable = false, length = 128)
	private String allowedItemTypes;

	@Column(name = "allowed_equipment_slots", nullable = false, length = 256)
	private String allowedEquipmentSlots;

	@Column(name = "allowed_weapon_families", nullable = false, length = 128)
	private String allowedWeaponFamilies;

	@Column(name = "allowed_armor_categories", nullable = false, length = 64)
	private String allowedArmorCategories;

	protected AffixDefinitionEntity() {
	}

	public String getCode() {
		return code;
	}

	public AffixKind getKind() {
		return kind;
	}

	public String getDisplayName() {
		return displayName;
	}

	public AffixStat getStat() {
		return stat;
	}

	public int getMagnitudeMin() {
		return magnitudeMin;
	}

	public int getMagnitudeMax() {
		return magnitudeMax;
	}

	public String getAllowedItemTypes() {
		return allowedItemTypes;
	}

	public String getAllowedEquipmentSlots() {
		return allowedEquipmentSlots;
	}

	public String getAllowedWeaponFamilies() {
		return allowedWeaponFamilies;
	}

	public String getAllowedArmorCategories() {
		return allowedArmorCategories;
	}
}
