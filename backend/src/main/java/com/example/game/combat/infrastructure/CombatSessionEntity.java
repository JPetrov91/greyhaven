package com.example.game.combat.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.combat.domain.CombatSessionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

@Entity
@Table(name = "combat_sessions")
public class CombatSessionEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "encounter_id", nullable = false, unique = true)
	private UUID encounterId;

	@Column(name = "character_id", nullable = false)
	private UUID characterId;

	@Column(name = "monster_definition_id", nullable = false)
	private UUID monsterDefinitionId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private CombatSessionStatus status;

	@Column(name = "round_number", nullable = false)
	private int roundNumber;

	@Column(name = "player_health", nullable = false)
	private int playerHealth;

	@Column(name = "player_stamina", nullable = false)
	private int playerStamina;

	@Column(name = "enemy_health", nullable = false)
	private int enemyHealth;

	@Column(name = "rewards_applied", nullable = false)
	private boolean rewardsApplied;

	@Column(name = "reward_plan_created", nullable = false)
	private boolean rewardPlanCreated;

	@Column(name = "planned_xp")
	private Integer plannedXp;

	@Column(name = "planned_gold")
	private Integer plannedGold;

	@Column(name = "outcome_acknowledged", nullable = false)
	private boolean outcomeAcknowledged;

	@Column(name = "xp_awarded")
	private Integer xpAwarded;

	@Column(name = "gold_awarded")
	private Integer goldAwarded;

	@Version
	@Column(nullable = false)
	private long version;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Transient
	private boolean unsaved;

	protected CombatSessionEntity() {
	}

	public CombatSessionEntity(
			UUID id,
			UUID encounterId,
			UUID characterId,
			UUID monsterDefinitionId,
			CombatSessionStatus status,
			int roundNumber,
			int playerHealth,
			int playerStamina,
			int enemyHealth,
			Instant createdAt,
			Instant updatedAt) {
		this.id = id;
		this.encounterId = encounterId;
		this.characterId = characterId;
		this.monsterDefinitionId = monsterDefinitionId;
		this.status = status;
		this.roundNumber = roundNumber;
		this.playerHealth = playerHealth;
		this.playerStamina = playerStamina;
		this.enemyHealth = enemyHealth;
		this.rewardsApplied = false;
		this.rewardPlanCreated = false;
		this.plannedXp = null;
		this.plannedGold = null;
		this.outcomeAcknowledged = true;
		this.xpAwarded = null;
		this.goldAwarded = null;
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

	public UUID getEncounterId() {
		return encounterId;
	}

	public UUID getCharacterId() {
		return characterId;
	}

	public UUID getMonsterDefinitionId() {
		return monsterDefinitionId;
	}

	public CombatSessionStatus getStatus() {
		return status;
	}

	public int getRoundNumber() {
		return roundNumber;
	}

	public int getPlayerHealth() {
		return playerHealth;
	}

	public int getPlayerStamina() {
		return playerStamina;
	}

	public int getEnemyHealth() {
		return enemyHealth;
	}

	public boolean isRewardsApplied() {
		return rewardsApplied;
	}

	public boolean isRewardPlanCreated() {
		return rewardPlanCreated;
	}

	public Integer getPlannedXp() {
		return plannedXp;
	}

	public Integer getPlannedGold() {
		return plannedGold;
	}

	public boolean isOutcomeAcknowledged() {
		return outcomeAcknowledged;
	}

	public Integer getXpAwarded() {
		return xpAwarded;
	}

	public Integer getGoldAwarded() {
		return goldAwarded;
	}

	public long getVersion() {
		return version;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void applyRound(
			int roundNumber,
			int playerHealth,
			int playerStamina,
			int enemyHealth,
			CombatSessionStatus status,
			Instant updatedAt) {
		if (this.status != CombatSessionStatus.ACTIVE) {
			throw new IllegalStateException("cannot apply round to a terminal combat session");
		}
		if (roundNumber < this.roundNumber) {
			throw new IllegalArgumentException("roundNumber must not decrease");
		}
		if (playerHealth < 0 || playerStamina < 0 || enemyHealth < 0) {
			throw new IllegalArgumentException("vitals must be non-negative");
		}
		this.roundNumber = roundNumber;
		this.playerHealth = playerHealth;
		this.playerStamina = playerStamina;
		this.enemyHealth = enemyHealth;
		this.status = status;
		this.updatedAt = updatedAt;
		if (status != CombatSessionStatus.ACTIVE) {
			this.outcomeAcknowledged = false;
		}
	}

	public void acknowledgeOutcome(Instant updatedAt) {
		if (this.status == CombatSessionStatus.ACTIVE) {
			throw new IllegalStateException("cannot acknowledge an active combat session");
		}
		this.outcomeAcknowledged = true;
		this.updatedAt = updatedAt;
	}

	/**
	 * Keeps session HP aligned after an out-of-band heal (inventory potion) during ACTIVE combat.
	 */
	public void syncPlayerHealth(int playerHealth, Instant updatedAt) {
		if (this.status != CombatSessionStatus.ACTIVE) {
			throw new IllegalStateException("cannot sync health on a terminal combat session");
		}
		if (playerHealth < 0) {
			throw new IllegalArgumentException("playerHealth must be non-negative");
		}
		this.playerHealth = playerHealth;
		this.updatedAt = updatedAt;
	}

	public void markRewards(int xp, int gold, Instant updatedAt) {
		if (rewardsApplied) {
			throw new IllegalStateException("rewards already applied");
		}
		if (status != CombatSessionStatus.PLAYER_WON) {
			throw new IllegalStateException("rewards require PLAYER_WON status");
		}
		if (xp < 0 || gold < 0) {
			throw new IllegalArgumentException("xp and gold must be non-negative");
		}
		this.rewardsApplied = true;
		this.xpAwarded = xp;
		this.goldAwarded = gold;
		this.updatedAt = updatedAt;
	}

	public void markRewardPlan(int xp, int gold, Instant updatedAt) {
		if (rewardPlanCreated) {
			throw new IllegalStateException("reward plan already created");
		}
		if (xp < 0 || gold < 0) {
			throw new IllegalArgumentException("planned rewards must be non-negative");
		}
		this.rewardPlanCreated = true;
		this.plannedXp = xp;
		this.plannedGold = gold;
		this.updatedAt = updatedAt;
	}
}
