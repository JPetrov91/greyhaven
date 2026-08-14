package com.example.game.pvp.infrastructure;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.combat.domain.CombatAction;
import com.example.game.pvp.domain.PvpMatchKind;
import com.example.game.pvp.domain.PvpMatchStatus;

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
@Table(name = "pvp_matches")
public class PvpMatchEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Enumerated(EnumType.STRING)
	@Column(name = "match_kind", nullable = false, length = 16)
	private PvpMatchKind matchKind;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private PvpMatchStatus status;

	@Column(name = "attacker_id", nullable = false)
	private UUID attackerId;

	@Column(name = "defender_id", nullable = false)
	private UUID defenderId;

	@Column(name = "round_number", nullable = false)
	private int roundNumber;

	@Column(name = "attacker_health", nullable = false)
	private int attackerHealth;

	@Column(name = "attacker_stamina", nullable = false)
	private int attackerStamina;

	@Column(name = "defender_health", nullable = false)
	private int defenderHealth;

	@Column(name = "defender_stamina", nullable = false)
	private int defenderStamina;

	@Column(name = "attacker_potion_charges", nullable = false)
	private int attackerPotionCharges;

	@Column(name = "defender_potion_charges", nullable = false)
	private int defenderPotionCharges;

	@Column(name = "last_defender_missed", nullable = false)
	private boolean lastDefenderMissed;

	@Column(name = "last_attacker_guarded", nullable = false)
	private boolean lastAttackerGuarded;

	@Enumerated(EnumType.STRING)
	@Column(name = "pending_attacker_action", length = 32)
	private CombatAction pendingAttackerAction;

	@Column(name = "pending_attacker_technique", length = 64)
	private String pendingAttackerTechnique;

	@Enumerated(EnumType.STRING)
	@Column(name = "pending_defender_action", length = 32)
	private CombatAction pendingDefenderAction;

	@Column(name = "pending_defender_technique", length = 64)
	private String pendingDefenderTechnique;

	@Column(name = "action_deadline_at")
	private Instant actionDeadlineAt;

	@Column(name = "expires_at")
	private Instant expiresAt;

	@Column(name = "planned_attacker_rating_delta", nullable = false)
	private int plannedAttackerRatingDelta;

	@Column(name = "planned_defender_rating_delta", nullable = false)
	private int plannedDefenderRatingDelta;

	@Column(name = "planned_attacker_marks", nullable = false)
	private int plannedAttackerMarks;

	@Column(name = "planned_defender_marks", nullable = false)
	private int plannedDefenderMarks;

	@Column(name = "attacker_rating_at_start", nullable = false)
	private int attackerRatingAtStart;

	@Column(name = "defender_rating_at_start", nullable = false)
	private int defenderRatingAtStart;

	@Column(name = "rating_reward_multiplier", nullable = false, precision = 6, scale = 3)
	private BigDecimal ratingRewardMultiplier;

	@Column(name = "settlement_applied", nullable = false)
	private boolean settlementApplied;

	@Column(name = "outcome_acknowledged", nullable = false)
	private boolean outcomeAcknowledged;

	@Version
	@Column(nullable = false)
	private int version;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Transient
	private boolean unsaved;

	protected PvpMatchEntity() {
	}

	public PvpMatchEntity(
			UUID id,
			PvpMatchKind matchKind,
			PvpMatchStatus status,
			UUID attackerId,
			UUID defenderId,
			int roundNumber,
			int attackerHealth,
			int attackerStamina,
			int defenderHealth,
			int defenderStamina,
			int attackerPotionCharges,
			int defenderPotionCharges,
			Instant expiresAt,
			int plannedAttackerRatingDelta,
			int plannedDefenderRatingDelta,
			int plannedAttackerMarks,
			int plannedDefenderMarks,
			int attackerRatingAtStart,
			int defenderRatingAtStart,
			BigDecimal ratingRewardMultiplier,
			boolean outcomeAcknowledged,
			Instant createdAt) {
		this.id = id;
		this.matchKind = matchKind;
		this.status = status;
		this.attackerId = attackerId;
		this.defenderId = defenderId;
		this.roundNumber = roundNumber;
		this.attackerHealth = attackerHealth;
		this.attackerStamina = attackerStamina;
		this.defenderHealth = defenderHealth;
		this.defenderStamina = defenderStamina;
		this.attackerPotionCharges = attackerPotionCharges;
		this.defenderPotionCharges = defenderPotionCharges;
		this.expiresAt = expiresAt;
		this.plannedAttackerRatingDelta = plannedAttackerRatingDelta;
		this.plannedDefenderRatingDelta = plannedDefenderRatingDelta;
		this.plannedAttackerMarks = plannedAttackerMarks;
		this.plannedDefenderMarks = plannedDefenderMarks;
		this.attackerRatingAtStart = attackerRatingAtStart;
		this.defenderRatingAtStart = defenderRatingAtStart;
		this.ratingRewardMultiplier = ratingRewardMultiplier;
		this.settlementApplied = false;
		this.outcomeAcknowledged = outcomeAcknowledged;
		this.createdAt = createdAt;
		this.updatedAt = createdAt;
		this.unsaved = true;
	}

	public void applyRound(
			int roundNumber,
			int attackerHealth,
			int attackerStamina,
			int defenderHealth,
			int defenderStamina,
			int attackerPotionCharges,
			int defenderPotionCharges,
			boolean lastDefenderMissed,
			boolean lastAttackerGuarded,
			PvpMatchStatus status,
			Instant updatedAt) {
		this.roundNumber = roundNumber;
		this.attackerHealth = attackerHealth;
		this.attackerStamina = attackerStamina;
		this.defenderHealth = defenderHealth;
		this.defenderStamina = defenderStamina;
		this.attackerPotionCharges = attackerPotionCharges;
		this.defenderPotionCharges = defenderPotionCharges;
		this.lastDefenderMissed = lastDefenderMissed;
		this.lastAttackerGuarded = lastAttackerGuarded;
		this.status = status;
		if (status != PvpMatchStatus.ACTIVE) {
			this.outcomeAcknowledged = false;
			this.pendingAttackerAction = null;
			this.pendingAttackerTechnique = null;
			this.pendingDefenderAction = null;
			this.pendingDefenderTechnique = null;
			this.actionDeadlineAt = null;
		}
		this.updatedAt = updatedAt;
	}

	public void setPendingAttacker(CombatAction action, String technique, Instant deadline, Instant updatedAt) {
		this.pendingAttackerAction = action;
		this.pendingAttackerTechnique = technique;
		this.actionDeadlineAt = deadline;
		this.updatedAt = updatedAt;
	}

	public void setPendingDefender(CombatAction action, String technique, Instant deadline, Instant updatedAt) {
		this.pendingDefenderAction = action;
		this.pendingDefenderTechnique = technique;
		this.actionDeadlineAt = deadline;
		this.updatedAt = updatedAt;
	}

	public void clearPending(Instant updatedAt) {
		this.pendingAttackerAction = null;
		this.pendingAttackerTechnique = null;
		this.pendingDefenderAction = null;
		this.pendingDefenderTechnique = null;
		this.actionDeadlineAt = null;
		this.updatedAt = updatedAt;
	}

	public void activateFromPending(
			int attackerHealth,
			int attackerStamina,
			int defenderHealth,
			int defenderStamina,
			int attackerPotionCharges,
			int defenderPotionCharges,
			int plannedAttackerRatingDelta,
			int plannedDefenderRatingDelta,
			int plannedAttackerMarks,
			int plannedDefenderMarks,
			int attackerRatingAtStart,
			int defenderRatingAtStart,
			Instant actionDeadlineAt,
			Instant updatedAt) {
		this.status = PvpMatchStatus.ACTIVE;
		this.roundNumber = 0;
		this.attackerHealth = attackerHealth;
		this.attackerStamina = attackerStamina;
		this.defenderHealth = defenderHealth;
		this.defenderStamina = defenderStamina;
		this.attackerPotionCharges = attackerPotionCharges;
		this.defenderPotionCharges = defenderPotionCharges;
		this.plannedAttackerRatingDelta = plannedAttackerRatingDelta;
		this.plannedDefenderRatingDelta = plannedDefenderRatingDelta;
		this.plannedAttackerMarks = plannedAttackerMarks;
		this.plannedDefenderMarks = plannedDefenderMarks;
		this.attackerRatingAtStart = attackerRatingAtStart;
		this.defenderRatingAtStart = defenderRatingAtStart;
		this.actionDeadlineAt = actionDeadlineAt;
		this.updatedAt = updatedAt;
	}

	public void markExpired(Instant updatedAt) {
		this.status = PvpMatchStatus.EXPIRED;
		this.updatedAt = updatedAt;
	}

	public void markDeclined(Instant updatedAt) {
		this.status = PvpMatchStatus.DECLINED;
		this.updatedAt = updatedAt;
	}

	public void markForfeit(Instant updatedAt) {
		this.status = PvpMatchStatus.ATTACKER_FORFEIT;
		this.outcomeAcknowledged = false;
		this.updatedAt = updatedAt;
	}

	public void markSettlementApplied(Instant updatedAt) {
		this.settlementApplied = true;
		this.updatedAt = updatedAt;
	}

	public void acknowledgeOutcome(Instant updatedAt) {
		this.outcomeAcknowledged = true;
		this.updatedAt = updatedAt;
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

	public PvpMatchKind getMatchKind() {
		return matchKind;
	}

	public PvpMatchStatus getStatus() {
		return status;
	}

	public UUID getAttackerId() {
		return attackerId;
	}

	public UUID getDefenderId() {
		return defenderId;
	}

	public int getRoundNumber() {
		return roundNumber;
	}

	public int getAttackerHealth() {
		return attackerHealth;
	}

	public int getAttackerStamina() {
		return attackerStamina;
	}

	public int getDefenderHealth() {
		return defenderHealth;
	}

	public int getDefenderStamina() {
		return defenderStamina;
	}

	public int getAttackerPotionCharges() {
		return attackerPotionCharges;
	}

	public int getDefenderPotionCharges() {
		return defenderPotionCharges;
	}

	public boolean isLastDefenderMissed() {
		return lastDefenderMissed;
	}

	public boolean isLastAttackerGuarded() {
		return lastAttackerGuarded;
	}

	public CombatAction getPendingAttackerAction() {
		return pendingAttackerAction;
	}

	public String getPendingAttackerTechnique() {
		return pendingAttackerTechnique;
	}

	public CombatAction getPendingDefenderAction() {
		return pendingDefenderAction;
	}

	public String getPendingDefenderTechnique() {
		return pendingDefenderTechnique;
	}

	public Instant getActionDeadlineAt() {
		return actionDeadlineAt;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}

	public int getPlannedAttackerRatingDelta() {
		return plannedAttackerRatingDelta;
	}

	public int getPlannedDefenderRatingDelta() {
		return plannedDefenderRatingDelta;
	}

	public int getPlannedAttackerMarks() {
		return plannedAttackerMarks;
	}

	public int getPlannedDefenderMarks() {
		return plannedDefenderMarks;
	}

	public int getAttackerRatingAtStart() {
		return attackerRatingAtStart;
	}

	public int getDefenderRatingAtStart() {
		return defenderRatingAtStart;
	}

	public BigDecimal getRatingRewardMultiplier() {
		return ratingRewardMultiplier;
	}

	public boolean isSettlementApplied() {
		return settlementApplied;
	}

	public boolean isOutcomeAcknowledged() {
		return outcomeAcknowledged;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
