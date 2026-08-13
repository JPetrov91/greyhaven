package com.example.game.expedition.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.expedition.domain.ExpeditionStatus;
import com.example.game.expedition.domain.ExpeditionStrategy;
import com.example.game.expedition.domain.ExpeditionType;

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
@Table(name = "expeditions")
public class ExpeditionEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "character_id", nullable = false)
	private UUID characterId;

	@Enumerated(EnumType.STRING)
	@Column(name = "expedition_type", nullable = false, length = 64)
	private ExpeditionType expeditionType;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private ExpeditionStrategy strategy;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private ExpeditionStatus status;

	@Column(name = "started_at", nullable = false)
	private Instant startedAt;

	@Column(name = "completes_at", nullable = false)
	private Instant completesAt;

	@Column(name = "claimed_at")
	private Instant claimedAt;

	@Column(name = "result_generated", nullable = false)
	private boolean resultGenerated;

	@Column(name = "planned_xp")
	private Integer plannedXp;

	@Column(name = "planned_gold")
	private Integer plannedGold;

	@Column(name = "planned_injury")
	private Integer plannedInjury;

	@Column(name = "xp_awarded")
	private Integer xpAwarded;

	@Column(name = "gold_awarded")
	private Integer goldAwarded;

	@Column(name = "injury_applied")
	private Integer injuryApplied;

	@Version
	@Column(nullable = false)
	private long version;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Transient
	private boolean unsaved;

	protected ExpeditionEntity() {
	}

	public ExpeditionEntity(
			UUID id,
			UUID characterId,
			ExpeditionType expeditionType,
			ExpeditionStrategy strategy,
			Instant startedAt,
			Instant completesAt,
			Instant createdAt,
			Instant updatedAt) {
		this.id = id;
		this.characterId = characterId;
		this.expeditionType = expeditionType;
		this.strategy = strategy;
		this.status = ExpeditionStatus.ACTIVE;
		this.startedAt = startedAt;
		this.completesAt = completesAt;
		this.claimedAt = null;
		this.resultGenerated = false;
		this.plannedXp = null;
		this.plannedGold = null;
		this.plannedInjury = null;
		this.xpAwarded = null;
		this.goldAwarded = null;
		this.injuryApplied = null;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.unsaved = true;
	}

	@PostPersist
	@PostLoad
	void markStored() {
		this.unsaved = false;
	}

	@Override
	public UUID getId() {
		return id;
	}

	@Override
	public boolean isNew() {
		return unsaved;
	}

	public UUID getCharacterId() {
		return characterId;
	}

	public ExpeditionType getExpeditionType() {
		return expeditionType;
	}

	public ExpeditionStrategy getStrategy() {
		return strategy;
	}

	public ExpeditionStatus getStatus() {
		return status;
	}

	public Instant getStartedAt() {
		return startedAt;
	}

	public Instant getCompletesAt() {
		return completesAt;
	}

	public Instant getClaimedAt() {
		return claimedAt;
	}

	public boolean isResultGenerated() {
		return resultGenerated;
	}

	public Integer getPlannedXp() {
		return plannedXp;
	}

	public Integer getPlannedGold() {
		return plannedGold;
	}

	public Integer getPlannedInjury() {
		return plannedInjury;
	}

	public Integer getXpAwarded() {
		return xpAwarded;
	}

	public Integer getGoldAwarded() {
		return goldAwarded;
	}

	public Integer getInjuryApplied() {
		return injuryApplied;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public boolean isDue(Instant now) {
		return !now.isBefore(completesAt);
	}

	public void markCompleted(Instant now) {
		if (status != ExpeditionStatus.ACTIVE) {
			throw new IllegalStateException("only ACTIVE expeditions can complete");
		}
		this.status = ExpeditionStatus.COMPLETED;
		this.updatedAt = now;
	}

	public void markResultPlan(int xp, int gold, int injury, Instant now) {
		if (resultGenerated) {
			return;
		}
		this.resultGenerated = true;
		this.plannedXp = xp;
		this.plannedGold = gold;
		this.plannedInjury = injury;
		this.updatedAt = now;
	}

	public void markClaimed(int xp, int gold, int injury, Instant now) {
		if (status != ExpeditionStatus.COMPLETED) {
			throw new IllegalStateException("only COMPLETED expeditions can be claimed");
		}
		this.status = ExpeditionStatus.CLAIMED;
		this.claimedAt = now;
		this.xpAwarded = xp;
		this.goldAwarded = gold;
		this.injuryApplied = injury;
		this.updatedAt = now;
	}
}
