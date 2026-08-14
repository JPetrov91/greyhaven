package com.example.game.item.infrastructure;

import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.item.domain.AffixKind;

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
@Table(name = "item_instance_affixes")
public class ItemInstanceAffixEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "item_instance_id", nullable = false)
	private UUID itemInstanceId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private AffixKind kind;

	@Column(nullable = false)
	private int ordinal;

	@Column(name = "affix_code", nullable = false, length = 64)
	private String affixCode;

	@Column(name = "rolled_magnitude", nullable = false)
	private int rolledMagnitude;

	@Transient
	private boolean unsaved;

	protected ItemInstanceAffixEntity() {
	}

	public ItemInstanceAffixEntity(
			UUID id,
			UUID itemInstanceId,
			AffixKind kind,
			int ordinal,
			String affixCode,
			int rolledMagnitude) {
		this.id = id;
		this.itemInstanceId = itemInstanceId;
		this.kind = kind;
		this.ordinal = ordinal;
		this.affixCode = affixCode;
		this.rolledMagnitude = rolledMagnitude;
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

	public UUID getItemInstanceId() {
		return itemInstanceId;
	}

	public AffixKind getKind() {
		return kind;
	}

	public int getOrdinal() {
		return ordinal;
	}

	public String getAffixCode() {
		return affixCode;
	}

	public int getRolledMagnitude() {
		return rolledMagnitude;
	}
}
