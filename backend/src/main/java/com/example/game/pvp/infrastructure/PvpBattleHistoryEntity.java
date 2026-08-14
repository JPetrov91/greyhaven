package com.example.game.pvp.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.pvp.domain.PvpHistoryResult;
import com.example.game.pvp.domain.PvpMatchKind;

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
@Table(name = "pvp_battle_history")
public class PvpBattleHistoryEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(name = "match_id", nullable = false)
	private UUID matchId;

	@Column(name = "character_id", nullable = false)
	private UUID characterId;

	@Column(name = "opponent_id", nullable = false)
	private UUID opponentId;

	@Column(name = "opponent_name", nullable = false, length = 64)
	private String opponentName;

	@Enumerated(EnumType.STRING)
	@Column(name = "match_kind", nullable = false, length = 16)
	private PvpMatchKind matchKind;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private PvpHistoryResult result;

	@Column(name = "rating_delta", nullable = false)
	private int ratingDelta;

	@Column(name = "marks_awarded", nullable = false)
	private int marksAwarded;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Transient
	private boolean unsaved;

	protected PvpBattleHistoryEntity() {
	}

	public PvpBattleHistoryEntity(
			UUID id,
			UUID matchId,
			UUID characterId,
			UUID opponentId,
			String opponentName,
			PvpMatchKind matchKind,
			PvpHistoryResult result,
			int ratingDelta,
			int marksAwarded,
			Instant createdAt) {
		this.id = id;
		this.matchId = matchId;
		this.characterId = characterId;
		this.opponentId = opponentId;
		this.opponentName = opponentName;
		this.matchKind = matchKind;
		this.result = result;
		this.ratingDelta = ratingDelta;
		this.marksAwarded = marksAwarded;
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

	public UUID getMatchId() {
		return matchId;
	}

	public UUID getCharacterId() {
		return characterId;
	}

	public UUID getOpponentId() {
		return opponentId;
	}

	public String getOpponentName() {
		return opponentName;
	}

	public PvpMatchKind getMatchKind() {
		return matchKind;
	}

	public PvpHistoryResult getResult() {
		return result;
	}

	public int getRatingDelta() {
		return ratingDelta;
	}

	public int getMarksAwarded() {
		return marksAwarded;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
