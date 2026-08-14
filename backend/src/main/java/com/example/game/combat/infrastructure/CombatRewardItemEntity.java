package com.example.game.combat.infrastructure;

import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.item.domain.GeneratedItem;
import com.example.game.item.domain.ItemRarity;
import com.example.game.item.domain.RolledAffixCodec;

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
@Table(name = "combat_reward_items")
public class CombatRewardItemEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "session_id", nullable = false)
	private UUID sessionId;

	@Column(name = "item_definition_id", nullable = false)
	private UUID itemDefinitionId;

	@Column(nullable = false)
	private int quantity;

	@Enumerated(EnumType.STRING)
	@Column(length = 32)
	private ItemRarity rarity;

	@Column(name = "rolled_weapon_damage")
	private Integer rolledWeaponDamage;

	@Column(name = "rolled_armor_value")
	private Integer rolledArmorValue;

	@Column(name = "rolled_affixes", nullable = false)
	private String rolledAffixes;

	@Transient
	private boolean unsaved;

	protected CombatRewardItemEntity() {
	}

	public CombatRewardItemEntity(
			UUID id,
			UUID sessionId,
			UUID itemDefinitionId,
			int quantity,
			GeneratedItem generated) {
		this.id = id;
		this.sessionId = sessionId;
		this.itemDefinitionId = itemDefinitionId;
		this.quantity = quantity;
		this.rarity = generated.rarity();
		this.rolledWeaponDamage = generated.rolledWeaponDamage();
		this.rolledArmorValue = generated.rolledArmorValue();
		this.rolledAffixes = RolledAffixCodec.encode(generated.affixes());
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

	public UUID getSessionId() {
		return sessionId;
	}

	public UUID getItemDefinitionId() {
		return itemDefinitionId;
	}

	public int getQuantity() {
		return quantity;
	}

	public GeneratedItem toGenerated() {
		return new GeneratedItem(
				rarity,
				rolledWeaponDamage,
				rolledArmorValue,
				RolledAffixCodec.decode(rolledAffixes));
	}

	public boolean hasPlannedRoll() {
		return rarity != null;
	}
}
