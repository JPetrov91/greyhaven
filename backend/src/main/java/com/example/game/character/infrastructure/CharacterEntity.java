package com.example.game.character.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

/**
 * Uniqueness of {@code lower(name)} is enforced by the {@code uq_characters_name_lower} index.
 */
@Entity
@Table(name = "characters")
public class CharacterEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "account_id", nullable = false, unique = true)
	private UUID accountId;

	@Column(nullable = false, length = 64)
	private String name;

	@Column(nullable = false)
	private int level;

	@Column(nullable = false)
	private int experience;

	@Column(nullable = false)
	private int strength;

	@Column(nullable = false)
	private int agility;

	@Column(nullable = false)
	private int endurance;

	@Column(nullable = false)
	private int perception;

	@Column(name = "current_health", nullable = false)
	private int currentHealth;

	@Column(name = "max_health", nullable = false)
	private int maxHealth;

	@Column(name = "current_stamina", nullable = false)
	private int currentStamina;

	@Column(name = "max_stamina", nullable = false)
	private int maxStamina;

	@Column(nullable = false)
	private int gold;

	@Column(name = "current_location_id")
	private UUID currentLocationId;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	/**
	 * Identifiers are assigned by the application, so Spring Data cannot infer whether an
	 * instance is new. Without this flag every save would be a merge and issue a redundant select.
	 */
	@Transient
	private boolean unsaved;

	protected CharacterEntity() {
	}

	public CharacterEntity(
			UUID id,
			UUID accountId,
			String name,
			int level,
			int experience,
			int strength,
			int agility,
			int endurance,
			int perception,
			int currentHealth,
			int maxHealth,
			int currentStamina,
			int maxStamina,
			int gold,
			UUID currentLocationId,
			Instant createdAt,
			Instant updatedAt) {
		this.id = id;
		this.accountId = accountId;
		this.name = name;
		this.level = level;
		this.experience = experience;
		this.strength = strength;
		this.agility = agility;
		this.endurance = endurance;
		this.perception = perception;
		this.currentHealth = currentHealth;
		this.maxHealth = maxHealth;
		this.currentStamina = currentStamina;
		this.maxStamina = maxStamina;
		this.gold = gold;
		this.currentLocationId = currentLocationId;
		this.createdAt = createdAt;
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

	public UUID getAccountId() {
		return accountId;
	}

	public String getName() {
		return name;
	}

	public int getLevel() {
		return level;
	}

	public int getExperience() {
		return experience;
	}

	public int getStrength() {
		return strength;
	}

	public int getAgility() {
		return agility;
	}

	public int getEndurance() {
		return endurance;
	}

	public int getPerception() {
		return perception;
	}

	public int getCurrentHealth() {
		return currentHealth;
	}

	public int getMaxHealth() {
		return maxHealth;
	}

	public int getCurrentStamina() {
		return currentStamina;
	}

	public int getMaxStamina() {
		return maxStamina;
	}

	public int getGold() {
		return gold;
	}

	public UUID getCurrentLocationId() {
		return currentLocationId;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
