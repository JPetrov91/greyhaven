package com.example.game.quest.infrastructure;

import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.quest.domain.QuestObjectiveType;

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
@Table(name = "quest_objective_definition")
public class QuestObjectiveDefinitionEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "quest_id", nullable = false)
	private UUID questId;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private QuestObjectiveType type;

	@Column(name = "target_code", nullable = false, length = 64)
	private String targetCode;

	@Column(name = "required_amount", nullable = false)
	private int requiredAmount;

	@Column(name = "display_text", nullable = false, length = 256)
	private String displayText;

	@Column(name = "consume_on_turn_in", nullable = false)
	private boolean consumeOnTurnIn;

	@Transient
	private boolean unsaved;

	protected QuestObjectiveDefinitionEntity() {
	}

	public QuestObjectiveDefinitionEntity(
			UUID id,
			UUID questId,
			int sortOrder,
			QuestObjectiveType type,
			String targetCode,
			int requiredAmount,
			String displayText,
			boolean consumeOnTurnIn) {
		this.id = id;
		this.questId = questId;
		this.sortOrder = sortOrder;
		this.type = type;
		this.targetCode = targetCode;
		this.requiredAmount = requiredAmount;
		this.displayText = displayText;
		this.consumeOnTurnIn = consumeOnTurnIn;
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

	public UUID getQuestId() {
		return questId;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public QuestObjectiveType getType() {
		return type;
	}

	public String getTargetCode() {
		return targetCode;
	}

	public int getRequiredAmount() {
		return requiredAmount;
	}

	public String getDisplayText() {
		return displayText;
	}

	public boolean isConsumeOnTurnIn() {
		return consumeOnTurnIn;
	}
}
