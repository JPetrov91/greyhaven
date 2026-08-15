package com.example.game.world.infrastructure;

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
@Table(name = "npc_definitions")
public class NpcDefinitionEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true, length = 64)
	private String code;

	@Column(nullable = false, length = 128)
	private String name;

	@Column(nullable = false, length = 128)
	private String title;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String greeting;

	@Column(name = "portrait_code", nullable = false, length = 64)
	private String portraitCode;

	@Column(name = "location_code", nullable = false, length = 64)
	private String locationCode;

	@Column(name = "merchant_code", length = 64)
	private String merchantCode;

	@Column(nullable = false, length = 256)
	private String interactions;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Transient
	private boolean unsaved;

	protected NpcDefinitionEntity() {
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

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getGreeting() {
		return greeting;
	}

	public String getPortraitCode() {
		return portraitCode;
	}

	public String getLocationCode() {
		return locationCode;
	}

	public String getMerchantCode() {
		return merchantCode;
	}

	public String getInteractions() {
		return interactions;
	}

	public int getSortOrder() {
		return sortOrder;
	}
}
