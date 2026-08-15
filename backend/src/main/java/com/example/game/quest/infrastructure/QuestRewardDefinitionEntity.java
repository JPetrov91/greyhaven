package com.example.game.quest.infrastructure;

import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.quest.domain.QuestRewardKind;

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
@Table(name = "quest_reward_definition")
public class QuestRewardDefinitionEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "quest_id", nullable = false)
	private UUID questId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private QuestRewardKind kind;

	@Column(nullable = false)
	private int amount;

	@Column(name = "item_code", length = 64)
	private String itemCode;

	@Column(name = "unlock_code", length = 64)
	private String unlockCode;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Transient
	private boolean unsaved;

	protected QuestRewardDefinitionEntity() {
	}

	public QuestRewardDefinitionEntity(
			UUID id,
			UUID questId,
			QuestRewardKind kind,
			int amount,
			String itemCode,
			String unlockCode,
			int sortOrder) {
		this.id = id;
		this.questId = questId;
		this.kind = kind;
		this.amount = amount;
		this.itemCode = itemCode;
		this.unlockCode = unlockCode;
		this.sortOrder = sortOrder;
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

	public QuestRewardKind getKind() {
		return kind;
	}

	public int getAmount() {
		return amount;
	}

	public String getItemCode() {
		return itemCode;
	}

	public String getUnlockCode() {
		return unlockCode;
	}

	public int getSortOrder() {
		return sortOrder;
	}
}
