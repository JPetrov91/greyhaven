package com.example.game.item.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.item.domain.ItemRarity;
import com.example.game.item.domain.ItemType;

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

	@Column(name = "armor_value")
	private Integer armorValue;

	@Column(name = "heal_amount")
	private Integer healAmount;

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

	public Integer getArmorValue() {
		return armorValue;
	}

	public Integer getHealAmount() {
		return healAmount;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
