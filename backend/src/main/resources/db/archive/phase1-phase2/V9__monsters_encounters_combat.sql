-- Monsters, encounters, combat sessions/events/rewards (Task 5).

CREATE TABLE monster_definitions (
    id              UUID PRIMARY KEY,
    code            VARCHAR(64) NOT NULL,
    name            VARCHAR(128) NOT NULL,
    level           INT NOT NULL,
    max_health      INT NOT NULL,
    damage_min      INT NOT NULL,
    damage_max      INT NOT NULL,
    xp_reward       INT NOT NULL,
    gold_min        INT NOT NULL,
    gold_max        INT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_monster_definitions_code UNIQUE (code),
    CONSTRAINT chk_monster_definitions_level CHECK (level >= 1),
    CONSTRAINT chk_monster_definitions_max_health CHECK (max_health >= 1),
    CONSTRAINT chk_monster_definitions_damage CHECK (damage_min >= 0 AND damage_max >= damage_min),
    CONSTRAINT chk_monster_definitions_xp CHECK (xp_reward >= 0),
    CONSTRAINT chk_monster_definitions_gold CHECK (gold_min >= 0 AND gold_max >= gold_min)
);

CREATE TABLE monster_loot_entries (
    id                   UUID PRIMARY KEY,
    monster_definition_id UUID NOT NULL,
    item_definition_id   UUID NOT NULL,
    drop_chance_percent  INT NOT NULL,
    quantity_min         INT NOT NULL,
    quantity_max         INT NOT NULL,
    CONSTRAINT fk_monster_loot_monster FOREIGN KEY (monster_definition_id) REFERENCES monster_definitions (id),
    CONSTRAINT fk_monster_loot_item FOREIGN KEY (item_definition_id) REFERENCES item_definitions (id),
    CONSTRAINT chk_monster_loot_chance CHECK (drop_chance_percent >= 0 AND drop_chance_percent <= 100),
    CONSTRAINT chk_monster_loot_quantity CHECK (quantity_min >= 1 AND quantity_max >= quantity_min)
);

CREATE INDEX idx_monster_loot_monster ON monster_loot_entries (monster_definition_id);

CREATE TABLE location_encounter_weights (
    id                    UUID PRIMARY KEY,
    location_id           UUID NOT NULL,
    monster_definition_id UUID,
    weight                INT NOT NULL,
    CONSTRAINT fk_location_encounter_location FOREIGN KEY (location_id) REFERENCES locations (id),
    CONSTRAINT fk_location_encounter_monster FOREIGN KEY (monster_definition_id) REFERENCES monster_definitions (id),
    CONSTRAINT chk_location_encounter_weight CHECK (weight >= 1)
);

CREATE INDEX idx_location_encounter_location ON location_encounter_weights (location_id);

CREATE TABLE encounters (
    id                    UUID PRIMARY KEY,
    character_id          UUID NOT NULL,
    location_id           UUID NOT NULL,
    monster_definition_id UUID,
    status                VARCHAR(32) NOT NULL,
    created_at            TIMESTAMPTZ NOT NULL,
    updated_at            TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_encounters_character FOREIGN KEY (character_id) REFERENCES characters (id),
    CONSTRAINT fk_encounters_location FOREIGN KEY (location_id) REFERENCES locations (id),
    CONSTRAINT fk_encounters_monster FOREIGN KEY (monster_definition_id) REFERENCES monster_definitions (id),
    CONSTRAINT chk_encounters_status CHECK (status IN ('AVAILABLE', 'COMBAT_STARTED', 'RESOLVED', 'EXPIRED')),
    CONSTRAINT chk_encounters_monster_presence CHECK (
        (status = 'AVAILABLE' AND monster_definition_id IS NOT NULL)
        OR (status IN ('COMBAT_STARTED', 'RESOLVED', 'EXPIRED'))
    )
);

CREATE INDEX idx_encounters_character ON encounters (character_id);

-- At most one unresolved encounter per character.
CREATE UNIQUE INDEX uq_encounters_one_unresolved_per_character
    ON encounters (character_id)
    WHERE status IN ('AVAILABLE', 'COMBAT_STARTED');

CREATE TABLE combat_sessions (
    id                    UUID PRIMARY KEY,
    encounter_id          UUID NOT NULL,
    character_id          UUID NOT NULL,
    monster_definition_id UUID NOT NULL,
    status                VARCHAR(32) NOT NULL,
    round_number          INT NOT NULL,
    player_health         INT NOT NULL,
    player_stamina        INT NOT NULL,
    enemy_health          INT NOT NULL,
    rewards_applied       BOOLEAN NOT NULL DEFAULT FALSE,
    xp_awarded            INT,
    gold_awarded          INT,
    version               BIGINT NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ NOT NULL,
    updated_at            TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_combat_sessions_encounter UNIQUE (encounter_id),
    CONSTRAINT fk_combat_sessions_encounter FOREIGN KEY (encounter_id) REFERENCES encounters (id),
    CONSTRAINT fk_combat_sessions_character FOREIGN KEY (character_id) REFERENCES characters (id),
    CONSTRAINT fk_combat_sessions_monster FOREIGN KEY (monster_definition_id) REFERENCES monster_definitions (id),
    CONSTRAINT chk_combat_sessions_status CHECK (
        status IN ('ACTIVE', 'PLAYER_WON', 'PLAYER_LOST', 'PLAYER_ESCAPED')
    ),
    CONSTRAINT chk_combat_sessions_round CHECK (round_number >= 0),
    CONSTRAINT chk_combat_sessions_player_health CHECK (player_health >= 0),
    CONSTRAINT chk_combat_sessions_player_stamina CHECK (player_stamina >= 0),
    CONSTRAINT chk_combat_sessions_enemy_health CHECK (enemy_health >= 0),
    CONSTRAINT chk_combat_sessions_rewards CHECK (
        (rewards_applied = FALSE AND xp_awarded IS NULL AND gold_awarded IS NULL)
        OR (rewards_applied = TRUE AND xp_awarded IS NOT NULL AND gold_awarded IS NOT NULL)
    )
);

CREATE INDEX idx_combat_sessions_character ON combat_sessions (character_id);

CREATE UNIQUE INDEX uq_combat_sessions_one_active_per_character
    ON combat_sessions (character_id)
    WHERE status = 'ACTIVE';

CREATE TABLE combat_events (
    id              UUID PRIMARY KEY,
    session_id      UUID NOT NULL,
    round_number    INT NOT NULL,
    sequence_number INT NOT NULL,
    event_type      VARCHAR(64) NOT NULL,
    message         TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_combat_events_session FOREIGN KEY (session_id) REFERENCES combat_sessions (id),
    CONSTRAINT uq_combat_events_session_round_seq UNIQUE (session_id, round_number, sequence_number),
    CONSTRAINT chk_combat_events_round CHECK (round_number >= 0),
    CONSTRAINT chk_combat_events_sequence CHECK (sequence_number >= 1)
);

CREATE INDEX idx_combat_events_session ON combat_events (session_id, round_number, sequence_number);

CREATE TABLE combat_reward_items (
    id                 UUID PRIMARY KEY,
    session_id         UUID NOT NULL,
    item_definition_id UUID NOT NULL,
    quantity           INT NOT NULL,
    granted            BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_combat_reward_items_session FOREIGN KEY (session_id) REFERENCES combat_sessions (id),
    CONSTRAINT fk_combat_reward_items_item FOREIGN KEY (item_definition_id) REFERENCES item_definitions (id),
    CONSTRAINT chk_combat_reward_items_quantity CHECK (quantity >= 1)
);

CREATE INDEX idx_combat_reward_items_session ON combat_reward_items (session_id);

-- Deterministic monster seeds (MVP_SPEC §10).
INSERT INTO monster_definitions (
    id, code, name, level, max_health, damage_min, damage_max, xp_reward, gold_min, gold_max, created_at
) VALUES
    ('d0000000-0000-4000-8000-000000000001', 'STREET_THUG', 'Street Thug',
     1, 70, 5, 8, 20, 4, 10, TIMESTAMPTZ '2026-01-01 00:00:00+00'),
    ('d0000000-0000-4000-8000-000000000002', 'GIANT_RAT', 'Giant Rat',
     1, 50, 3, 6, 15, 2, 6, TIMESTAMPTZ '2026-01-01 00:00:00+00'),
    ('d0000000-0000-4000-8000-000000000003', 'FOREST_WOLF', 'Forest Wolf',
     2, 100, 7, 11, 30, 6, 14, TIMESTAMPTZ '2026-01-01 00:00:00+00'),
    ('d0000000-0000-4000-8000-000000000004', 'BANDIT', 'Bandit',
     3, 130, 10, 15, 45, 10, 22, TIMESTAMPTZ '2026-01-01 00:00:00+00'),
    ('d0000000-0000-4000-8000-000000000005', 'BANDIT_VETERAN', 'Bandit Veteran',
     5, 220, 15, 22, 80, 18, 35, TIMESTAMPTZ '2026-01-01 00:00:00+00');

-- Loot tables (item ids from V6).
INSERT INTO monster_loot_entries (
    id, monster_definition_id, item_definition_id, drop_chance_percent, quantity_min, quantity_max
) VALUES
    -- Street Thug: potion / old dagger
    ('e0000000-0000-4000-8000-000000000001', 'd0000000-0000-4000-8000-000000000001',
     'c0000000-0000-4000-8000-000000000006', 25, 1, 1),
    ('e0000000-0000-4000-8000-000000000002', 'd0000000-0000-4000-8000-000000000001',
     'c0000000-0000-4000-8000-000000000005', 10, 1, 1),
    -- Giant Rat: potion
    ('e0000000-0000-4000-8000-000000000003', 'd0000000-0000-4000-8000-000000000002',
     'c0000000-0000-4000-8000-000000000006', 15, 1, 1),
    -- Forest Wolf: wolf pelt / potion
    ('e0000000-0000-4000-8000-000000000004', 'd0000000-0000-4000-8000-000000000003',
     'c0000000-0000-4000-8000-000000000007', 70, 1, 1),
    ('e0000000-0000-4000-8000-000000000005', 'd0000000-0000-4000-8000-000000000003',
     'c0000000-0000-4000-8000-000000000006', 30, 1, 1),
    -- Bandit: iron sword / potion / leather armor
    ('e0000000-0000-4000-8000-000000000006', 'd0000000-0000-4000-8000-000000000004',
     'c0000000-0000-4000-8000-000000000003', 12, 1, 1),
    ('e0000000-0000-4000-8000-000000000007', 'd0000000-0000-4000-8000-000000000004',
     'c0000000-0000-4000-8000-000000000006', 35, 1, 2),
    ('e0000000-0000-4000-8000-000000000008', 'd0000000-0000-4000-8000-000000000004',
     'c0000000-0000-4000-8000-000000000004', 8, 1, 1),
    -- Bandit Veteran: iron sword / leather armor / potion
    ('e0000000-0000-4000-8000-000000000009', 'd0000000-0000-4000-8000-000000000005',
     'c0000000-0000-4000-8000-000000000003', 25, 1, 1),
    ('e0000000-0000-4000-8000-00000000000a', 'd0000000-0000-4000-8000-000000000005',
     'c0000000-0000-4000-8000-000000000004', 20, 1, 1),
    ('e0000000-0000-4000-8000-00000000000b', 'd0000000-0000-4000-8000-000000000005',
     'c0000000-0000-4000-8000-000000000006', 40, 1, 2);

-- Encounter weights per dangerous location (NULL monster = nothing found).
-- Location ids from V4.
INSERT INTO location_encounter_weights (id, location_id, monster_definition_id, weight) VALUES
    -- OLD_TOWN: Street Thug 55 / Giant Rat 35 / nothing 10
    ('f0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000004',
     'd0000000-0000-4000-8000-000000000001', 55),
    ('f0000000-0000-4000-8000-000000000002', 'a0000000-0000-4000-8000-000000000004',
     'd0000000-0000-4000-8000-000000000002', 35),
    ('f0000000-0000-4000-8000-000000000003', 'a0000000-0000-4000-8000-000000000004',
     NULL, 10),
    -- FOREST: Forest Wolf 70 / Bandit 20 / nothing 10
    ('f0000000-0000-4000-8000-000000000004', 'a0000000-0000-4000-8000-000000000005',
     'd0000000-0000-4000-8000-000000000003', 70),
    ('f0000000-0000-4000-8000-000000000005', 'a0000000-0000-4000-8000-000000000005',
     'd0000000-0000-4000-8000-000000000004', 20),
    ('f0000000-0000-4000-8000-000000000006', 'a0000000-0000-4000-8000-000000000005',
     NULL, 10),
    -- NORTH_ROAD: Bandit 40 / Street Thug 30 / Bandit Veteran 15 / Giant Rat 10 / nothing 5
    ('f0000000-0000-4000-8000-000000000007', 'a0000000-0000-4000-8000-000000000006',
     'd0000000-0000-4000-8000-000000000004', 40),
    ('f0000000-0000-4000-8000-000000000008', 'a0000000-0000-4000-8000-000000000006',
     'd0000000-0000-4000-8000-000000000001', 30),
    ('f0000000-0000-4000-8000-000000000009', 'a0000000-0000-4000-8000-000000000006',
     'd0000000-0000-4000-8000-000000000005', 15),
    ('f0000000-0000-4000-8000-00000000000a', 'a0000000-0000-4000-8000-000000000006',
     'd0000000-0000-4000-8000-000000000002', 10),
    ('f0000000-0000-4000-8000-00000000000b', 'a0000000-0000-4000-8000-000000000006',
     NULL, 5);
