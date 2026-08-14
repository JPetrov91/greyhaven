package com.example.game.world.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.world.domain.LocationSafety;

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
@Table(name = "locations")
public class LocationEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true, length = 64)
	private String code;

	@Column(nullable = false, length = 128)
	private String name;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private LocationSafety safety;

	@Column(nullable = false, length = 64)
	private String region;

	@Column(name = "recommended_level_min")
	private Integer recommendedLevelMin;

	@Column(name = "recommended_level_max")
	private Integer recommendedLevelMax;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Transient
	private boolean unsaved;

	protected LocationEntity() {
	}

	public LocationEntity(
			UUID id,
			String code,
			String name,
			String description,
			LocationSafety safety,
			String region,
			Instant createdAt) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.description = description;
		this.safety = safety;
		this.region = region;
		this.createdAt = createdAt;
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

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public LocationSafety getSafety() {
		return safety;
	}

	public String getRegion() {
		return region;
	}

	public Integer getRecommendedLevelMin() {
		return recommendedLevelMin;
	}

	public Integer getRecommendedLevelMax() {
		return recommendedLevelMax;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
