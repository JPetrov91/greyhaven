-- Combat 2.0: versioned rules, status effects, enemy combat ratings, session snapshots.
-- Existing combat_sessions finish on Phase 1 rules (rules_version = 1).

ALTER TABLE combat_sessions
    ADD COLUMN rules_version INTEGER NOT NULL DEFAULT 2,
    ADD COLUMN enemy_stamina INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN enemy_max_stamina INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN snap_enemy_armor INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN snap_enemy_accuracy INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN snap_enemy_dodge INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN snap_enemy_critical_chance INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN snap_enemy_damage_min INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN snap_enemy_damage_max INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN snap_ai_archetype VARCHAR(16),
    ADD COLUMN snap_signature_status VARCHAR(32),
    ADD COLUMN weapon_family VARCHAR(16),
    ADD COLUMN technique_codes VARCHAR(256),
    ADD COLUMN stamina_cost_reduction INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN last_enemy_missed BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN last_player_guarded BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE combat_sessions SET rules_version = 1;

ALTER TABLE combat_sessions
    ADD CONSTRAINT chk_combat_sessions_rules_version CHECK (rules_version IN (1, 2)),
    ADD CONSTRAINT chk_combat_sessions_weapon_family CHECK (
        weapon_family IS NULL OR weapon_family IN ('SWORD', 'AXE', 'MACE', 'DAGGER', 'BOW')
    ),
    ADD CONSTRAINT chk_combat_sessions_ai_archetype CHECK (
        snap_ai_archetype IS NULL OR snap_ai_archetype IN (
            'AGGRESSIVE', 'DEFENSIVE', 'CONTROL', 'ASSASSIN', 'ARMORED', 'BERSERKER'
        )
    );

CREATE TABLE combat_status_effects (
    id                UUID PRIMARY KEY,
    session_id        UUID NOT NULL,
    target            VARCHAR(8) NOT NULL,
    status_type       VARCHAR(32) NOT NULL,
    stacks            INTEGER NOT NULL,
    remaining_rounds  INTEGER NOT NULL,
    CONSTRAINT fk_combat_status_session FOREIGN KEY (session_id) REFERENCES combat_sessions (id),
    CONSTRAINT chk_combat_status_target CHECK (target IN ('PLAYER', 'ENEMY')),
    CONSTRAINT chk_combat_status_type CHECK (
        status_type IN (
            'BLEED', 'POISON', 'STUN', 'ARMOR_BREAK', 'OFF_BALANCE', 'GUARDED', 'STUN_IMMUNITY'
        )
    ),
    CONSTRAINT chk_combat_status_stacks CHECK (stacks >= 0),
    CONSTRAINT chk_combat_status_rounds CHECK (remaining_rounds >= 0),
    CONSTRAINT uq_combat_status_session_target_type UNIQUE (session_id, target, status_type)
);

CREATE INDEX idx_combat_status_session ON combat_status_effects (session_id);

ALTER TABLE monster_definitions
    ADD COLUMN armor INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN accuracy INTEGER NOT NULL DEFAULT 70,
    ADD COLUMN dodge INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN critical_chance INTEGER NOT NULL DEFAULT 5,
    ADD COLUMN max_stamina INTEGER NOT NULL DEFAULT 40,
    ADD COLUMN ai_archetype VARCHAR(16) NOT NULL DEFAULT 'AGGRESSIVE',
    ADD COLUMN signature_status VARCHAR(32);

ALTER TABLE monster_definitions
    ADD CONSTRAINT chk_monster_ai_archetype CHECK (
        ai_archetype IN ('AGGRESSIVE', 'DEFENSIVE', 'CONTROL', 'ASSASSIN', 'ARMORED', 'BERSERKER')
    ),
    ADD CONSTRAINT chk_monster_signature_status CHECK (
        signature_status IS NULL OR signature_status IN (
            'BLEED', 'POISON', 'STUN', 'ARMOR_BREAK', 'OFF_BALANCE', 'GUARDED'
        )
    ),
    ADD CONSTRAINT chk_monster_armor CHECK (armor >= 0),
    ADD CONSTRAINT chk_monster_stamina CHECK (max_stamina >= 0);

UPDATE monster_definitions SET
    armor = 4, accuracy = 72, dodge = 4, critical_chance = 5, max_stamina = 40,
    ai_archetype = 'AGGRESSIVE', signature_status = NULL
WHERE code = 'STREET_THUG';

UPDATE monster_definitions SET
    armor = 2, accuracy = 70, dodge = 12, critical_chance = 8, max_stamina = 35,
    ai_archetype = 'ASSASSIN', signature_status = NULL
WHERE code = 'GIANT_RAT';

UPDATE monster_definitions SET
    armor = 3, accuracy = 74, dodge = 8, critical_chance = 6, max_stamina = 45,
    ai_archetype = 'BERSERKER', signature_status = 'BLEED'
WHERE code = 'FOREST_WOLF';

UPDATE monster_definitions SET
    armor = 8, accuracy = 76, dodge = 6, critical_chance = 5, max_stamina = 50,
    ai_archetype = 'DEFENSIVE', signature_status = NULL
WHERE code = 'BANDIT';

UPDATE monster_definitions SET
    armor = 16, accuracy = 80, dodge = 4, critical_chance = 5, max_stamina = 55,
    ai_archetype = 'ARMORED', signature_status = 'ARMOR_BREAK'
WHERE code = 'BANDIT_VETERAN';
