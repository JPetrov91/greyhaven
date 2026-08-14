package com.example.game.character.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.character.domain.CharacterBalance;
import com.example.game.character.domain.CharacterProgression;
import com.example.game.character.domain.ProgressionBalance;
import com.example.game.shared.balance.GameBalanceCatalog;

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

	@Column(name = "unspent_attribute_points", nullable = false)
	private int unspentAttributePoints;

	@Column(name = "current_location_id", nullable = false)
	private UUID currentLocationId;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "last_recovery_at", nullable = false)
	private Instant lastRecoveryAt;

	@Column(name = "arena_rating", nullable = false)
	private int arenaRating;

	@Column(name = "arena_marks", nullable = false)
	private int arenaMarks;

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
			int unspentAttributePoints,
			UUID currentLocationId,
			Instant createdAt,
			Instant updatedAt,
			Instant lastRecoveryAt) {
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
		this.unspentAttributePoints = unspentAttributePoints;
		this.currentLocationId = currentLocationId;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.lastRecoveryAt = lastRecoveryAt;
		this.arenaRating = GameBalanceCatalog.get().pvp().startingRating();
		this.arenaMarks = 0;
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

	public int getUnspentAttributePoints() {
		return unspentAttributePoints;
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

	public Instant getLastRecoveryAt() {
		return lastRecoveryAt;
	}

	public int getArenaRating() {
		return arenaRating;
	}

	public int getArenaMarks() {
		return arenaMarks;
	}

	public void moveTo(UUID locationId, Instant updatedAt) {
		this.currentLocationId = locationId;
		this.updatedAt = updatedAt;
	}

	public void applyHealth(int currentHealth, Instant updatedAt) {
		if (currentHealth < 0 || currentHealth > maxHealth) {
			throw new IllegalArgumentException("currentHealth must be between 0 and maxHealth");
		}
		this.currentHealth = currentHealth;
		this.updatedAt = updatedAt;
	}

	public void applyStamina(int currentStamina, Instant updatedAt) {
		if (currentStamina < 0 || currentStamina > maxStamina) {
			throw new IllegalArgumentException("currentStamina must be between 0 and maxStamina");
		}
		this.currentStamina = currentStamina;
		this.updatedAt = updatedAt;
	}

	public void syncCombatVitals(int currentHealth, int currentStamina, Instant updatedAt) {
		applyHealth(Math.min(currentHealth, maxHealth), updatedAt);
		applyStamina(Math.min(currentStamina, maxStamina), updatedAt);
	}

	public void addGold(int amount, Instant updatedAt) {
		if (amount < 0) {
			throw new IllegalArgumentException("gold amount must be non-negative");
		}
		this.gold = Math.addExact(this.gold, amount);
		this.updatedAt = updatedAt;
	}

	public void spendGold(int amount, Instant updatedAt) {
		if (amount < 0) {
			throw new IllegalArgumentException("gold amount must be non-negative");
		}
		if (amount > gold) {
			throw new IllegalArgumentException("insufficient gold");
		}
		this.gold -= amount;
		this.updatedAt = updatedAt;
	}

	public void applyArenaRating(int rating, Instant updatedAt) {
		if (rating < 0) {
			throw new IllegalArgumentException("arena rating must be non-negative");
		}
		this.arenaRating = rating;
		this.updatedAt = updatedAt;
	}

	public void addArenaMarks(int amount, Instant updatedAt) {
		if (amount < 0) {
			throw new IllegalArgumentException("arena marks amount must be non-negative");
		}
		this.arenaMarks = Math.addExact(this.arenaMarks, amount);
		this.updatedAt = updatedAt;
	}

	public int applyPendingLevelCatchUp(Instant updatedAt) {
		return grantExperience(0, updatedAt);
	}

	public void checkpointRecovery(int currentHealth, int currentStamina, Instant now) {
		applyHealth(Math.min(currentHealth, maxHealth), now);
		applyStamina(Math.min(currentStamina, maxStamina), now);
		this.lastRecoveryAt = now;
	}

	public void restartRecoveryBaseline(Instant now) {
		this.lastRecoveryAt = now;
		this.updatedAt = now;
	}

	public void respec(Instant updatedAt) {
		int allocated = (strength - CharacterBalance.STARTING_STRENGTH)
				+ (agility - CharacterBalance.STARTING_AGILITY)
				+ (endurance - CharacterBalance.STARTING_ENDURANCE)
				+ (perception - CharacterBalance.STARTING_PERCEPTION);
		if (allocated < 0) {
			throw new IllegalStateException("allocated attributes below starting values");
		}
		this.unspentAttributePoints = Math.addExact(this.unspentAttributePoints, allocated);
		this.strength = CharacterBalance.STARTING_STRENGTH;
		this.agility = CharacterBalance.STARTING_AGILITY;
		this.endurance = CharacterBalance.STARTING_ENDURANCE;
		this.perception = CharacterBalance.STARTING_PERCEPTION;
		recomputeMaxVitalsPreservingCurrentRatios(updatedAt);
	}

	public int grantExperience(int xpGain, Instant updatedAt) {
		CharacterProgression.ProgressionResult result = CharacterProgression.applyExperience(
				level,
				experience,
				xpGain);
		this.level = result.level();
		this.experience = result.experience();
		this.unspentAttributePoints = Math.addExact(
				this.unspentAttributePoints,
				result.unspentAttributePointsGained());
		recomputeMaxVitalsPreservingCurrentRatios(updatedAt);
		return result.unspentAttributePointsGained();
	}

	public void allocateAttributes(
			int strengthDelta,
			int agilityDelta,
			int enduranceDelta,
			int perceptionDelta,
			Instant updatedAt) {
		int total = strengthDelta + agilityDelta + enduranceDelta + perceptionDelta;
		if (total < 1) {
			throw new IllegalArgumentException("must allocate at least one point");
		}
		if (strengthDelta < 0 || agilityDelta < 0 || enduranceDelta < 0 || perceptionDelta < 0) {
			throw new IllegalArgumentException("attribute deltas must be non-negative");
		}
		if (total > unspentAttributePoints) {
			throw new IllegalArgumentException("not enough unspent attribute points");
		}

		int newStrength = strength + strengthDelta;
		int newAgility = agility + agilityDelta;
		int newEndurance = endurance + enduranceDelta;
		int newPerception = perception + perceptionDelta;
		if (newStrength > ProgressionBalance.MAX_ATTRIBUTE_VALUE
				|| newAgility > ProgressionBalance.MAX_ATTRIBUTE_VALUE
				|| newEndurance > ProgressionBalance.MAX_ATTRIBUTE_VALUE
				|| newPerception > ProgressionBalance.MAX_ATTRIBUTE_VALUE) {
			throw new IllegalArgumentException("attribute would exceed maximum");
		}

		this.strength = newStrength;
		this.agility = newAgility;
		this.endurance = newEndurance;
		this.perception = newPerception;
		this.unspentAttributePoints -= total;
		recomputeMaxVitalsPreservingCurrentRatios(updatedAt);
	}

	private void recomputeMaxVitalsPreservingCurrentRatios(Instant updatedAt) {
		int previousMaxHealth = this.maxHealth;
		int previousMaxStamina = this.maxStamina;
		this.maxHealth = CharacterBalance.maxHealth(endurance, level);
		this.maxStamina = CharacterBalance.maxStamina(endurance, agility);
		if (previousMaxHealth > 0) {
			this.currentHealth = Math.min(
					maxHealth,
					Math.max(1, (int) Math.round((double) currentHealth * maxHealth / previousMaxHealth)));
		}
		else {
			this.currentHealth = maxHealth;
		}
		if (previousMaxStamina > 0) {
			this.currentStamina = Math.min(
					maxStamina,
					Math.max(0, (int) Math.round((double) currentStamina * maxStamina / previousMaxStamina)));
		}
		else {
			this.currentStamina = maxStamina;
		}
		this.updatedAt = updatedAt;
	}
}
