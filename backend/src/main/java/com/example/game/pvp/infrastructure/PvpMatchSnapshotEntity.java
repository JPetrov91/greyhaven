package com.example.game.pvp.infrastructure;

import java.time.Instant;
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
@Table(name = "pvp_match_snapshots")
public class PvpMatchSnapshotEntity implements Persistable<UUID> {

	@Id
	@Column(name = "match_id")
	private UUID matchId;

	@Column(name = "snapshot_version", nullable = false)
	private int snapshotVersion;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String payload;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Transient
	private boolean unsaved;

	protected PvpMatchSnapshotEntity() {
	}

	public PvpMatchSnapshotEntity(UUID matchId, int snapshotVersion, String payload, Instant createdAt) {
		this.matchId = matchId;
		this.snapshotVersion = snapshotVersion;
		this.payload = payload;
		this.createdAt = createdAt;
		this.unsaved = true;
	}

	@Override
	public UUID getId() {
		return matchId;
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

	public int getSnapshotVersion() {
		return snapshotVersion;
	}

	public String getPayload() {
		return payload;
	}
}
