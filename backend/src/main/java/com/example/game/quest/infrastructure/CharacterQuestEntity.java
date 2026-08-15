package com.example.game.quest.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.quest.domain.QuestStatus;

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
@Table(name = "character_quest")
public class CharacterQuestEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "character_id", nullable = false)
	private UUID characterId;

	@Column(name = "quest_id", nullable = false)
	private UUID questId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private QuestStatus status;

	@Column(name = "accepted_at", nullable = false)
	private Instant acceptedAt;

	@Column(name = "ready_at")
	private Instant readyAt;

	@Column(name = "completed_at")
	private Instant completedAt;

	@Column(name = "rewards_applied", nullable = false)
	private boolean rewardsApplied;

	@Transient
	private boolean unsaved;

	protected CharacterQuestEntity() {
	}

	public CharacterQuestEntity(UUID id, UUID characterId, UUID questId, Instant acceptedAt) {
		this.id = id;
		this.characterId = characterId;
		this.questId = questId;
		this.status = QuestStatus.ACTIVE;
		this.acceptedAt = acceptedAt;
		this.rewardsApplied = false;
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

	public UUID getQuestId() {
		return questId;
	}

	public QuestStatus getStatus() {
		return status;
	}

	public Instant getAcceptedAt() {
		return acceptedAt;
	}

	public Instant getReadyAt() {
		return readyAt;
	}

	public Instant getCompletedAt() {
		return completedAt;
	}

	public boolean isRewardsApplied() {
		return rewardsApplied;
	}

	public void markReady(Instant now) {
		this.status = QuestStatus.READY_TO_TURN_IN;
		this.readyAt = now;
	}

	public void markActive() {
		this.status = QuestStatus.ACTIVE;
		this.readyAt = null;
	}

	public void markCompleted(Instant now) {
		this.status = QuestStatus.COMPLETED;
		this.completedAt = now;
		this.rewardsApplied = true;
	}
}
