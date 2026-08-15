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
@Table(name = "character_quest_track")
public class CharacterQuestTrackEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "character_id", nullable = false)
	private UUID characterId;

	@Column(name = "quest_id", nullable = false)
	private UUID questId;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Transient
	private boolean unsaved;

	protected CharacterQuestTrackEntity() {
	}

	public CharacterQuestTrackEntity(UUID id, UUID characterId, UUID questId, int sortOrder) {
		this.id = id;
		this.characterId = characterId;
		this.questId = questId;
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

	public UUID getCharacterId() {
		return characterId;
	}

	public UUID getQuestId() {
		return questId;
	}

	public int getSortOrder() {
		return sortOrder;
	}
}
