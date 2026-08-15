package com.example.game.quest.infrastructure;

import java.util.UUID;

import org.springframework.data.domain.Persistable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "character_quest_objective")
public class CharacterQuestObjectiveEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "character_quest_id", nullable = false)
	private UUID characterQuestId;

	@Column(name = "objective_id", nullable = false)
	private UUID objectiveId;

	@Column(name = "current_amount", nullable = false)
	private int currentAmount;

	@Column(nullable = false)
	private boolean completed;

	@Transient
	private boolean unsaved;

	protected CharacterQuestObjectiveEntity() {
	}

	public CharacterQuestObjectiveEntity(UUID id, UUID characterQuestId, UUID objectiveId) {
		this.id = id;
		this.characterQuestId = characterQuestId;
		this.objectiveId = objectiveId;
		this.currentAmount = 0;
		this.completed = false;
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

	public UUID getCharacterQuestId() {
		return characterQuestId;
	}

	public UUID getObjectiveId() {
		return objectiveId;
	}

	public int getCurrentAmount() {
		return currentAmount;
	}

	public boolean isCompleted() {
		return completed;
	}

	public void apply(int amount, boolean completed) {
		this.currentAmount = amount;
		this.completed = completed;
	}
}
