-- Task 7 follow-up: unique keep trophy, level-band tables, ruins approach, distinct enemies.

ALTER TABLE monster_loot_entries
    ADD COLUMN once_per_character BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE monster_loot_entries
SET once_per_character = TRUE
WHERE id = 'e0000000-0000-4000-8000-000000000023';

CREATE TABLE character_unique_drops (
    id             UUID PRIMARY KEY,
    character_id   UUID NOT NULL,
    item_code      VARCHAR(64) NOT NULL,
    granted_at     TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_character_unique_drops_character FOREIGN KEY (character_id) REFERENCES characters (id),
    CONSTRAINT uq_character_unique_drops UNIQUE (character_id, item_code)
);

INSERT INTO item_definition_modifiers (id, item_definition_id, stat, magnitude)
SELECT gen_random_uuid(), d.id, seed.stat, seed.magnitude
FROM item_definitions d
JOIN (
    VALUES
        ('WARDENS_SIGNET', 'ARMOR', 3),
        ('WARDENS_SIGNET', 'ENDURANCE', 2)
) AS seed(code, stat, magnitude) ON d.code = seed.code;

ALTER TABLE dungeon_room_edges
    ADD COLUMN skip_room_code VARCHAR(64);

UPDATE dungeon_room_edges SET skip_room_code = 'PRISON'
WHERE id = '92000000-0000-4000-8000-000000000003';

UPDATE dungeon_room_edges SET skip_room_code = 'ARMORY'
WHERE id = '92000000-0000-4000-8000-000000000004';

UPDATE dungeon_room_edges SET skip_room_code = 'CRYPT'
WHERE id = '92000000-0000-4000-8000-000000000008';

DELETE FROM location_connections
WHERE id IN (
    'b0000000-0000-4000-8000-000000000019',
    'b0000000-0000-4000-8000-00000000001a'
);

INSERT INTO location_connections (id, from_location_id, to_location_id) VALUES
    ('b0000000-0000-4000-8000-00000000001b',
     'a0000000-0000-4000-8000-00000000000c', 'a0000000-0000-4000-8000-00000000000d'),
    ('b0000000-0000-4000-8000-00000000001c',
     'a0000000-0000-4000-8000-00000000000d', 'a0000000-0000-4000-8000-00000000000c');

DELETE FROM location_encounter_weights
WHERE id IN (
    'f0000000-0000-4000-8000-000000000011',
    'f0000000-0000-4000-8000-00000000001a',
    'f0000000-0000-4000-8000-00000000001e'
);

UPDATE location_encounter_weights SET weight = 50
WHERE id = 'f0000000-0000-4000-8000-00000000000f';
UPDATE location_encounter_weights SET weight = 35
WHERE id = 'f0000000-0000-4000-8000-000000000010';

UPDATE location_encounter_weights SET weight = 40
WHERE id = 'f0000000-0000-4000-8000-000000000017';
UPDATE location_encounter_weights SET weight = 32
WHERE id = 'f0000000-0000-4000-8000-000000000018';
UPDATE location_encounter_weights SET weight = 13
WHERE id = 'f0000000-0000-4000-8000-000000000019';

UPDATE location_encounter_weights SET weight = 55
WHERE id = 'f0000000-0000-4000-8000-00000000001c';
UPDATE location_encounter_weights SET weight = 25
WHERE id = 'f0000000-0000-4000-8000-00000000001d';

UPDATE monster_definitions
SET ai_archetype = 'DEFENSIVE', armor = 10, xp_reward = 48
WHERE code = 'DOCK_BRAWLER';

UPDATE monster_definitions
SET ai_archetype = 'ASSASSIN', signature_status = 'BLEED', dodge = 16, xp_reward = 180
WHERE code = 'MINE_CRAWLER';

UPDATE monster_definitions
SET ai_archetype = 'AGGRESSIVE', signature_status = NULL, dodge = 18, critical_chance = 14, xp_reward = 280
WHERE code = 'CAMP_CUTTHROAT';

UPDATE monster_definitions
SET signature_status = 'BLEED', xp_reward = 320
WHERE code = 'SHIELDED_RAIDER';

UPDATE monster_definitions
SET ai_archetype = 'BERSERKER', signature_status = 'BLEED', armor = 14, dodge = 8, xp_reward = 700
WHERE code = 'RUIN_GUARDIAN';

UPDATE monster_definitions
SET ai_archetype = 'MARKSMAN', signature_status = 'OFF_BALANCE', xp_reward = 1800, gold_min = 80, gold_max = 120
WHERE code = 'WARDEN_OF_THE_KEEP';

UPDATE monster_definitions SET xp_reward = 85 WHERE code = 'PLAGUE_RAT';
UPDATE monster_definitions SET xp_reward = 95 WHERE code = 'SEWER_WATCHMAN';
UPDATE monster_definitions SET xp_reward = 240 WHERE code = 'CAVE_BRUTE';
UPDATE monster_definitions SET xp_reward = 400 WHERE code = 'PIT_OVERSEER';
UPDATE monster_definitions SET xp_reward = 480 WHERE code = 'BANDIT_LIEUTENANT';
UPDATE monster_definitions SET xp_reward = 520 WHERE code = 'RUIN_STALKER';
UPDATE monster_definitions SET xp_reward = 70 WHERE code = 'SMUGGLER';
