package com.example.game.mastery.infrastructure;

import com.example.game.item.domain.WeaponFamily;
import com.example.game.mastery.domain.TechniqueKind;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "combat_technique_definitions")
public class CombatTechniqueDefinitionEntity {

	@Id
	@Column(length = 64)
	private String code;

	@Column(name = "display_name", nullable = false, length = 64)
	private String displayName;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "weapon_family", nullable = false, length = 16)
	private WeaponFamily weaponFamily;

	@Column(name = "unlock_mastery_level", nullable = false)
	private int unlockMasteryLevel;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private TechniqueKind kind;

	@Column(name = "effect_code", nullable = false, length = 64)
	private String effectCode;

	@Column(name = "stamina_cost", nullable = false)
	private int staminaCost;

	@Column(name = "accuracy_modifier", nullable = false)
	private int accuracyModifier;

	@Column(name = "damage_percent_modifier", nullable = false)
	private int damagePercentModifier;

	@Column(name = "applies_status", length = 32)
	private String appliesStatus;

	@Column(name = "status_stacks", nullable = false)
	private int statusStacks;

	@Column(name = "status_duration_rounds", nullable = false)
	private int statusDurationRounds;

	@Column(nullable = false, length = 128)
	private String tags;

	protected CombatTechniqueDefinitionEntity() {
	}

	public String getCode() {
		return code;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getDescription() {
		return description;
	}

	public WeaponFamily getWeaponFamily() {
		return weaponFamily;
	}

	public int getUnlockMasteryLevel() {
		return unlockMasteryLevel;
	}

	public TechniqueKind getKind() {
		return kind;
	}

	public String getEffectCode() {
		return effectCode;
	}

	public int getStaminaCost() {
		return staminaCost;
	}

	public int getAccuracyModifier() {
		return accuracyModifier;
	}

	public int getDamagePercentModifier() {
		return damagePercentModifier;
	}

	public String getAppliesStatus() {
		return appliesStatus;
	}

	public int getStatusStacks() {
		return statusStacks;
	}

	public int getStatusDurationRounds() {
		return statusDurationRounds;
	}

	public String getTags() {
		return tags;
	}
}
