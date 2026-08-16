package com.example.game.item.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.inventory.domain.EquipmentSlot;
import com.example.game.item.domain.ArmorCategory;
import com.example.game.item.domain.ItemDefinitionData;
import com.example.game.item.domain.ItemRarity;
import com.example.game.item.domain.ItemType;
import com.example.game.item.domain.WeaponFamily;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "item_definitions")
public class ItemDefinitionEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true, length = 64)
	private String code;

	@Column(nullable = false, length = 128)
	private String name;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private ItemType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private ItemRarity rarity;

	@Column(name = "base_value", nullable = false)
	private int baseValue;

	@Column(name = "required_level", nullable = false)
	private int requiredLevel;

	@Column(name = "weapon_damage")
	private Integer weaponDamage;

	@Column(name = "weapon_damage_min")
	private Integer weaponDamageMin;

	@Column(name = "weapon_damage_max")
	private Integer weaponDamageMax;

	@Column(name = "block_soak_min")
	private Integer blockSoakMin;

	@Column(name = "block_soak_max")
	private Integer blockSoakMax;

	@Column(name = "armor_value")
	private Integer armorValue;

	@Column(name = "heal_amount")
	private Integer healAmount;

	@Enumerated(EnumType.STRING)
	@Column(name = "equipment_slot", length = 16)
	private EquipmentSlot equipmentSlot;

	@Column(name = "two_handed", nullable = false)
	private boolean twoHanded;

	@Enumerated(EnumType.STRING)
	@Column(name = "weapon_family", length = 32)
	private WeaponFamily weaponFamily;

	@Enumerated(EnumType.STRING)
	@Column(name = "armor_category", length = 32)
	private ArmorCategory armorCategory;

	@Column(name = "required_strength", nullable = false)
	private int requiredStrength;

	@Column(name = "required_agility", nullable = false)
	private int requiredAgility;

	@Column(name = "required_endurance", nullable = false)
	private int requiredEndurance;

	@Column(name = "required_perception", nullable = false)
	private int requiredPerception;

	@Column(nullable = false)
	private boolean legacy;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Transient
	private boolean unsaved;

	protected ItemDefinitionEntity() {
	}

	public ItemDefinitionEntity(
			UUID id,
			String code,
			String name,
			String description,
			ItemType type,
			ItemRarity rarity,
			int baseValue,
			int requiredLevel,
			Integer weaponDamage,
			Integer armorValue,
			Integer healAmount,
			Instant createdAt) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.description = description;
		this.type = type;
		this.rarity = rarity;
		this.baseValue = baseValue;
		this.requiredLevel = requiredLevel;
		this.weaponDamage = weaponDamage;
		this.armorValue = armorValue;
		this.healAmount = healAmount;
		this.twoHanded = false;
		this.legacy = false;
		this.equipmentSlot = type.isEquippable() ? EquipmentSlot.forItemType(type) : null;
		this.createdAt = createdAt;
		this.unsaved = true;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public boolean isNew() {
		return unsaved;
	}

	@PostPersist
	@PostLoad
	void markStored() {
		this.unsaved = false;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public ItemType getType() {
		return type;
	}

	public ItemRarity getRarity() {
		return rarity;
	}

	public int getBaseValue() {
		return baseValue;
	}

	public int getRequiredLevel() {
		return requiredLevel;
	}

	public Integer getWeaponDamage() {
		return weaponDamage;
	}

	public Integer getWeaponDamageMin() {
		return weaponDamageMin;
	}

	public Integer getWeaponDamageMax() {
		return weaponDamageMax;
	}

	public Integer getBlockSoakMin() {
		return blockSoakMin;
	}

	public Integer getBlockSoakMax() {
		return blockSoakMax;
	}

	public Integer getArmorValue() {
		return armorValue;
	}

	public Integer getHealAmount() {
		return healAmount;
	}

	public EquipmentSlot getEquipmentSlot() {
		return equipmentSlot;
	}

	public boolean isTwoHanded() {
		return twoHanded;
	}

	public WeaponFamily getWeaponFamily() {
		return weaponFamily;
	}

	public ArmorCategory getArmorCategory() {
		return armorCategory;
	}

	public int getRequiredStrength() {
		return requiredStrength;
	}

	public int getRequiredAgility() {
		return requiredAgility;
	}

	public int getRequiredEndurance() {
		return requiredEndurance;
	}

	public int getRequiredPerception() {
		return requiredPerception;
	}

	public boolean isLegacy() {
		return legacy;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public ItemDefinitionData toData() {
		return new ItemDefinitionData(
				code,
				name,
				type,
				rarity,
				legacy,
				equipmentSlot,
				twoHanded,
				weaponFamily,
				armorCategory,
				weaponDamage,
				armorValue,
				requiredLevel,
				requiredStrength,
				requiredAgility,
				requiredEndurance,
				requiredPerception,
				weaponDamageMin,
				weaponDamageMax,
				blockSoakMin,
				blockSoakMax);
	}
}
