-- Phase 2 foundation: equipment slot rename, level CHECK headroom, itemization prep columns.
-- Does not rewrite XP or level values. Application MAX_LEVEL remains 10 until Task 2.

ALTER TABLE characters
    DROP CONSTRAINT chk_characters_level;

ALTER TABLE characters
    ADD CONSTRAINT chk_characters_level CHECK (level >= 1 AND level <= 30);

ALTER TABLE item_definitions
    ADD COLUMN equipment_slot VARCHAR(16),
    ADD COLUMN two_handed BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN weapon_family VARCHAR(32),
    ADD COLUMN armor_category VARCHAR(32),
    ADD COLUMN required_strength INT NOT NULL DEFAULT 0,
    ADD COLUMN required_agility INT NOT NULL DEFAULT 0,
    ADD COLUMN required_endurance INT NOT NULL DEFAULT 0,
    ADD COLUMN required_perception INT NOT NULL DEFAULT 0,
    ADD COLUMN legacy BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE item_definitions
SET equipment_slot = 'MAIN_HAND'
WHERE type = 'WEAPON';

UPDATE item_definitions
SET equipment_slot = 'CHEST'
WHERE type = 'ARMOR';

UPDATE item_definitions
SET legacy = TRUE;

ALTER TABLE item_definitions
    ADD CONSTRAINT chk_item_definitions_equipment_slot CHECK (
        equipment_slot IS NULL
        OR equipment_slot IN (
            'HEAD', 'CHEST', 'HANDS', 'LEGS', 'FEET',
            'MAIN_HAND', 'OFF_HAND', 'AMULET', 'RING'
        )
    );

ALTER TABLE item_definitions
    ADD CONSTRAINT chk_item_definitions_attr_requirements CHECK (
        required_strength >= 0
        AND required_agility >= 0
        AND required_endurance >= 0
        AND required_perception >= 0
    );

ALTER TABLE item_instances
    ADD COLUMN legacy BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE item_instances
SET legacy = TRUE;

ALTER TABLE equipment
    DROP CONSTRAINT chk_equipment_slot;

UPDATE equipment
SET slot = 'MAIN_HAND'
WHERE slot = 'WEAPON';

UPDATE equipment
SET slot = 'CHEST'
WHERE slot = 'ARMOR';

ALTER TABLE equipment
    ADD CONSTRAINT chk_equipment_slot CHECK (
        slot IN (
            'HEAD', 'CHEST', 'HANDS', 'LEGS', 'FEET',
            'MAIN_HAND', 'OFF_HAND', 'AMULET', 'RING'
        )
    );
