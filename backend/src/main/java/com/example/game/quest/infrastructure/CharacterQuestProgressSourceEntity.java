package com.example.game.quest.infrastructure;

import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.quest.domain.QuestProgressSourceKind;

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
@Table(name = "character_quest_progress_source")
public class CharacterQuestProgressSourceEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "character_id", nullable = false)
	private UUID characterId;

	@Enumerated(EnumType.STRING)
	@Column(name = "source_kind", nullable = false, length = 32)
	private QuestProgressSourceKind sourceKind;

	@Column(name = "source_id", nullable = false, length = 128)
	private String sourceId;

	@Transient
	private boolean unsaved;

	protected CharacterQuestProgressSourceEntity() {
	}

	public CharacterQuestProgressSourceEntity(
			UUID id,
			UUID characterId,
			QuestProgressSourceKind sourceKind,
			String sourceId) {
		this.id = id;
		this.characterId = characterId;
		this.sourceKind = sourceKind;
		this.sourceId = sourceId;
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
}
