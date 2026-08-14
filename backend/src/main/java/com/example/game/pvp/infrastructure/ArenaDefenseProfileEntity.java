package com.example.game.pvp.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

import com.example.game.combat.domain.CombatAction;
import com.example.game.pvp.domain.ArenaDefenseStrategy;

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
@Table(name = "arena_defense_profiles")
public class ArenaDefenseProfileEntity implements Persistable<UUID> {

	@Id
	@Column(name = "character_id")
	private UUID characterId;

	@Enumerated(EnumType.STRING)
	@Column(name = "preferred_action", nullable = false, length = 32)
	private CombatAction preferredAction;

	@Column(name = "preferred_technique_code", length = 64)
	private String preferredTechniqueCode;

	@Column(name = "heal_when_hp_percent_below", nullable = false)
	private int healWhenHpPercentBelow;

	@Column(name = "defend_when_stamina_percent_below", nullable = false)
	private int defendWhenStaminaPercentBelow;

	@Column(name = "finisher_when_enemy_hp_percent_below", nullable = false)
	private int finisherWhenEnemyHpPercentBelow;

	@Column(name = "finisher_technique_code", length = 64)
	private String finisherTechniqueCode;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Transient
	private boolean unsaved;

	protected ArenaDefenseProfileEntity() {
	}

	public ArenaDefenseProfileEntity(UUID characterId, ArenaDefenseStrategy strategy, Instant updatedAt) {
		this.characterId = characterId;
		apply(strategy, updatedAt);
		this.unsaved = true;
	}

	public void apply(ArenaDefenseStrategy strategy, Instant updatedAt) {
		this.preferredAction = strategy.preferredAction();
		this.preferredTechniqueCode = strategy.preferredTechniqueCode();
		this.healWhenHpPercentBelow = strategy.healWhenHpPercentBelow();
		this.defendWhenStaminaPercentBelow = strategy.defendWhenStaminaPercentBelow();
		this.finisherWhenEnemyHpPercentBelow = strategy.finisherWhenEnemyHpPercentBelow();
		this.finisherTechniqueCode = strategy.finisherTechniqueCode();
		this.updatedAt = updatedAt;
	}

	public ArenaDefenseStrategy toStrategy() {
		return new ArenaDefenseStrategy(
				preferredAction,
				preferredTechniqueCode,
				healWhenHpPercentBelow,
				defendWhenStaminaPercentBelow,
				finisherWhenEnemyHpPercentBelow,
				finisherTechniqueCode);
	}

	@Override
	public UUID getId() {
		return characterId;
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
}
