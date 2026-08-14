package com.example.game.combat.infrastructure;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

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
@Table(name = "monster_definitions")
public class MonsterDefinitionEntity implements Persistable<UUID> {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true, length = 64)
	private String code;

	@Column(nullable = false, length = 128)
	private String name;

	@Column(nullable = false)
	private int level;

	@Column(name = "max_health", nullable = false)
	private int maxHealth;

	@Column(name = "damage_min", nullable = false)
	private int damageMin;

	@Column(name = "damage_max", nullable = false)
	private int damageMax;

	@Column(name = "xp_reward", nullable = false)
	private int xpReward;

	@Column(name = "gold_min", nullable = false)
	private int goldMin;

	@Column(name = "gold_max", nullable = false)
	private int goldMax;

	@Column(nullable = false)
	private int armor;

	@Column(nullable = false)
	private int accuracy;

	@Column(nullable = false)
	private int dodge;

	@Column(name = "critical_chance", nullable = false)
	private int criticalChance;

	@Column(name = "max_stamina", nullable = false)
	private int maxStamina;

	@Enumerated(EnumType.STRING)
	@Column(name = "ai_archetype", nullable = false, length = 16)
	private com.example.game.combat.domain.EnemyAiArchetype aiArchetype;

	@Enumerated(EnumType.STRING)
	@Column(name = "signature_status", length = 32)
	private com.example.game.combat.domain.StatusType signatureStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "monster_tier", nullable = false, length = 16)
	private com.example.game.combat.domain.MonsterTier monsterTier;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Transient
	private boolean unsaved;

	protected MonsterDefinitionEntity() {
	}

	public MonsterDefinitionEntity(
			UUID id,
			String code,
			String name,
			int level,
			int maxHealth,
			int damageMin,
			int damageMax,
			int xpReward,
			int goldMin,
			int goldMax,
			Instant createdAt) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.level = level;
		this.maxHealth = maxHealth;
		this.damageMin = damageMin;
		this.damageMax = damageMax;
		this.xpReward = xpReward;
		this.goldMin = goldMin;
		this.goldMax = goldMax;
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

	public int getLevel() {
		return level;
	}

	public int getMaxHealth() {
		return maxHealth;
	}

	public int getDamageMin() {
		return damageMin;
	}

	public int getDamageMax() {
		return damageMax;
	}

	public int getXpReward() {
		return xpReward;
	}

	public int getGoldMin() {
		return goldMin;
	}

	public int getGoldMax() {
		return goldMax;
	}

	public int getArmor() {
		return armor;
	}

	public int getAccuracy() {
		return accuracy;
	}

	public int getDodge() {
		return dodge;
	}

	public int getCriticalChance() {
		return criticalChance;
	}

	public int getMaxStamina() {
		return maxStamina;
	}

	public com.example.game.combat.domain.EnemyAiArchetype getAiArchetype() {
		return aiArchetype;
	}

	public com.example.game.combat.domain.StatusType getSignatureStatus() {
		return signatureStatus;
	}

	public com.example.game.combat.domain.MonsterTier getMonsterTier() {
		return monsterTier == null ? com.example.game.combat.domain.MonsterTier.NORMAL : monsterTier;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
