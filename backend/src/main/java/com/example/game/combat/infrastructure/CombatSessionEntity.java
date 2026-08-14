package com.example.game.combat.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.combat.domain.CombatSessionStatus;
import com.example.game.combat.domain.CombatRulesVersion;
import com.example.game.combat.domain.EnemyAiArchetype;
import com.example.game.combat.domain.StatusType;
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

	@Column(name = "rules_version", nullable = false)
	private int rulesVersion;

	@Column(name = "enemy_stamina", nullable = false)
	private int enemyStamina;

	@Column(name = "enemy_max_stamina", nullable = false)
	private int enemyMaxStamina;

	@Column(name = "snap_enemy_armor", nullable = false)
	private int snapEnemyArmor;

	@Column(name = "snap_enemy_accuracy", nullable = false)
	private int snapEnemyAccuracy;

	@Column(name = "snap_enemy_dodge", nullable = false)
	private int snapEnemyDodge;

	@Column(name = "snap_enemy_critical_chance", nullable = false)
	private int snapEnemyCriticalChance;

	@Column(name = "snap_enemy_damage_min", nullable = false)
	private int snapEnemyDamageMin;

	@Column(name = "snap_enemy_damage_max", nullable = false)
	private int snapEnemyDamageMax;

	@Enumerated(EnumType.STRING)
	@Column(name = "snap_ai_archetype", length = 16)
	private EnemyAiArchetype snapAiArchetype;

	@Enumerated(EnumType.STRING)
	@Column(name = "snap_signature_status", length = 32)
	private StatusType snapSignatureStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "weapon_family", length = 16)
	private WeaponFamily weaponFamily;

	@Column(name = "technique_codes", length = 256)
	private String techniqueCodes;

	@Column(name = "stamina_cost_reduction", nullable = false)
	private int staminaCostReduction;

	@Column(name = "last_enemy_missed", nullable = false)
	private boolean lastEnemyMissed;

	@Column(name = "last_player_guarded", nullable = false)
	private boolean lastPlayerGuarded;

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

	@Column(name = "reward_previous_level")
	private Integer rewardPreviousLevel;

	@Column(name = "reward_new_level")
	private Integer rewardNewLevel;

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
		this.rulesVersion = CombatRulesVersion.COMBAT_2;
		this.enemyStamina = 0;
		this.enemyMaxStamina = 0;
		this.staminaCostReduction = 0;
		this.lastEnemyMissed = false;
		this.lastPlayerGuarded = false;
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

	public int getRulesVersion() {
		return rulesVersion;
	}

	public int getEnemyStamina() {
		return enemyStamina;
	}

	public int getEnemyMaxStamina() {
		return enemyMaxStamina;
	}

	public int getSnapEnemyArmor() {
		return snapEnemyArmor;
	}

	public int getSnapEnemyAccuracy() {
		return snapEnemyAccuracy;
	}

	public int getSnapEnemyDodge() {
		return snapEnemyDodge;
	}

	public int getSnapEnemyCriticalChance() {
		return snapEnemyCriticalChance;
	}

	public int getSnapEnemyDamageMin() {
		return snapEnemyDamageMin;
	}

	public int getSnapEnemyDamageMax() {
		return snapEnemyDamageMax;
	}

	public EnemyAiArchetype getSnapAiArchetype() {
		return snapAiArchetype;
	}

	public StatusType getSnapSignatureStatus() {
		return snapSignatureStatus;
	}

	public WeaponFamily getWeaponFamily() {
		return weaponFamily;
	}

	public String getTechniqueCodes() {
		return techniqueCodes;
	}

	public int getStaminaCostReduction() {
		return staminaCostReduction;
	}

	public boolean isLastEnemyMissed() {
		return lastEnemyMissed;
	}

	public boolean isLastPlayerGuarded() {
		return lastPlayerGuarded;
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

	public Integer getRewardPreviousLevel() {
		return rewardPreviousLevel;
	}

	public Integer getRewardNewLevel() {
		return rewardNewLevel;
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

	public void captureCombat2Snapshot(
			int enemyStamina,
			int enemyMaxStamina,
			int armor,
			int accuracy,
			int dodge,
			int criticalChance,
			int damageMin,
			int damageMax,
			EnemyAiArchetype archetype,
			StatusType signatureStatus,
			WeaponFamily weaponFamily,
			String techniqueCodes,
			int staminaCostReduction) {
		this.rulesVersion = CombatRulesVersion.COMBAT_2;
		this.enemyStamina = enemyStamina;
		this.enemyMaxStamina = enemyMaxStamina;
		this.snapEnemyArmor = armor;
		this.snapEnemyAccuracy = accuracy;
		this.snapEnemyDodge = dodge;
		this.snapEnemyCriticalChance = criticalChance;
		this.snapEnemyDamageMin = damageMin;
		this.snapEnemyDamageMax = damageMax;
		this.snapAiArchetype = archetype;
		this.snapSignatureStatus = signatureStatus;
		this.weaponFamily = weaponFamily;
		this.techniqueCodes = techniqueCodes;
		this.staminaCostReduction = staminaCostReduction;
	}

	public void applyRound(
			int roundNumber,
			int playerHealth,
			int playerStamina,
			int enemyHealth,
			CombatSessionStatus status,
			Instant updatedAt) {
		applyRound(roundNumber, playerHealth, playerStamina, enemyHealth, this.enemyStamina, status,
				this.lastEnemyMissed, this.lastPlayerGuarded, updatedAt);
	}

	public void applyRound(
			int roundNumber,
			int playerHealth,
			int playerStamina,
			int enemyHealth,
			int enemyStamina,
			CombatSessionStatus status,
			boolean lastEnemyMissed,
			boolean lastPlayerGuarded,
			Instant updatedAt) {
		if (this.status != CombatSessionStatus.ACTIVE) {
			throw new IllegalStateException("cannot apply round to a terminal combat session");
		}
		if (roundNumber < this.roundNumber) {
			throw new IllegalArgumentException("roundNumber must not decrease");
		}
		if (playerHealth < 0 || playerStamina < 0 || enemyHealth < 0 || enemyStamina < 0) {
			throw new IllegalArgumentException("vitals must be non-negative");
		}
		this.roundNumber = roundNumber;
		this.playerHealth = playerHealth;
		this.playerStamina = playerStamina;
		this.enemyHealth = enemyHealth;
		this.enemyStamina = enemyStamina;
		this.status = status;
		this.lastEnemyMissed = lastEnemyMissed;
		this.lastPlayerGuarded = lastPlayerGuarded;
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

	public void markRewards(int xp, int gold, int previousLevel, int newLevel, Instant updatedAt) {
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
		this.rewardPreviousLevel = previousLevel;
		this.rewardNewLevel = newLevel;
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
