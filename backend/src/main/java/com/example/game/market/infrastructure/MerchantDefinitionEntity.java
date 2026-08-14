package com.example.game.market.infrastructure;

import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.market.domain.MerchantType;

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
@Table(name = "merchant_definitions")
public class MerchantDefinitionEntity implements Persistable<UUID> {

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

	@Enumerated(EnumType.STRING)
	@Column(name = "merchant_type", nullable = false, length = 32)
	private MerchantType merchantType;

	@Column(name = "portrait_code", nullable = false, length = 64)
	private String portraitCode;

	@Column(name = "sort_order", nullable = false)
	private int sortOrder;

	@Transient
	private boolean unsaved;

	protected MerchantDefinitionEntity() {
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

	public MerchantType getMerchantType() {
		return merchantType;
	}

	public String getPortraitCode() {
		return portraitCode;
	}

	public int getSortOrder() {
		return sortOrder;
	}
}
