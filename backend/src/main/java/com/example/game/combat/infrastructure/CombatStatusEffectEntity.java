package com.example.game.combat.infrastructure;

import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.combat.domain.CombatantSide;
import com.example.game.combat.domain.StatusType;

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
@Table(name = "combat_status_effects")
public class CombatStatusEffectEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "session_id", nullable = false)
	private UUID sessionId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 8)
	private CombatantSide target;

	@Enumerated(EnumType.STRING)
	@Column(name = "status_type", nullable = false, length = 32)
	private StatusType statusType;

	@Column(nullable = false)
	private int stacks;

	@Column(name = "remaining_rounds", nullable = false)
	private int remainingRounds;

	@Transient
	private boolean unsaved;

	protected CombatStatusEffectEntity() {
	}

	public CombatStatusEffectEntity(
			UUID id,
			UUID sessionId,
			CombatantSide target,
			StatusType statusType,
			int stacks,
			int remainingRounds) {
		this.id = id;
		this.sessionId = sessionId;
		this.target = target;
		this.statusType = statusType;
		this.stacks = stacks;
		this.remainingRounds = remainingRounds;
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

	public CombatantSide getTarget() {
		return target;
	}

	public StatusType getStatusType() {
		return statusType;
	}

	public int getStacks() {
		return stacks;
	}

	public int getRemainingRounds() {
		return remainingRounds;
	}
}
