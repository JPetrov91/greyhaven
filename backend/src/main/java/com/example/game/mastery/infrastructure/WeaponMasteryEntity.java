package com.example.game.mastery.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

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
@Table(name = "weapon_masteries")
public class WeaponMasteryEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "character_id", nullable = false)
	private UUID characterId;

	@Enumerated(EnumType.STRING)
	@Column(name = "weapon_family", nullable = false, length = 16)
	private WeaponFamily weaponFamily;

	@Column(name = "total_experience", nullable = false)
	private int totalExperience;

	@Column(nullable = false)
	private int level;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Transient
	private boolean unsaved;

	protected WeaponMasteryEntity() {
	}

	public WeaponMasteryEntity(
			UUID id,
			UUID characterId,
			WeaponFamily weaponFamily,
			int totalExperience,
			int level,
			Instant updatedAt) {
		this.id = id;
		this.characterId = characterId;
		this.weaponFamily = weaponFamily;
		this.totalExperience = totalExperience;
		this.level = level;
		this.updatedAt = updatedAt;
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

	public UUID getCharacterId() {
		return characterId;
	}

	public WeaponFamily getWeaponFamily() {
		return weaponFamily;
	}

	public int getTotalExperience() {
		return totalExperience;
	}

	public int getLevel() {
		return level;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void apply(int level, int totalExperience, Instant updatedAt) {
		this.level = level;
		this.totalExperience = totalExperience;
		this.updatedAt = updatedAt;
	}
}
