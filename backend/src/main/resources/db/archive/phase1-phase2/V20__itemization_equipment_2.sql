-- Phase 2 Task 3: instance rolls/rarity, affixes, ACCESSORY, catalog expansion.
-- Does not rewrite V6/V18. Legacy definitions remain legacy.

ALTER TABLE item_definitions
    DROP CONSTRAINT chk_item_definitions_type;

ALTER TABLE item_definitions
    DROP CONSTRAINT chk_item_definitions_weapon_stats;

ALTER TABLE item_definitions
    DROP CONSTRAINT chk_item_definitions_armor_stats;

ALTER TABLE item_definitions
    DROP CONSTRAINT chk_item_definitions_consumable_stats;

ALTER TABLE item_definitions
    DROP CONSTRAINT chk_item_definitions_material_stats;

ALTER TABLE item_definitions
    ADD CONSTRAINT chk_item_definitions_type CHECK (
        type IN ('WEAPON', 'ARMOR', 'CONSUMABLE', 'MATERIAL', 'ACCESSORY')
    );

ALTER TABLE item_definitions
    ADD CONSTRAINT chk_item_definitions_weapon_stats CHECK (
        (type = 'WEAPON' AND weapon_damage IS NOT NULL AND armor_value IS NULL AND heal_amount IS NULL)
        OR (type <> 'WEAPON')
    );

ALTER TABLE item_definitions
    ADD CONSTRAINT chk_item_definitions_armor_stats CHECK (
        (type = 'ARMOR' AND armor_value IS NOT NULL AND weapon_damage IS NULL AND heal_amount IS NULL)
        OR (type <> 'ARMOR')
    );

ALTER TABLE item_definitions
    ADD CONSTRAINT chk_item_definitions_consumable_stats CHECK (
        (type = 'CONSUMABLE' AND heal_amount IS NOT NULL AND weapon_damage IS NULL AND armor_value IS NULL)
        OR (type <> 'CONSUMABLE')
    );

ALTER TABLE item_definitions
    ADD CONSTRAINT chk_item_definitions_material_stats CHECK (
        (type = 'MATERIAL' AND weapon_damage IS NULL AND armor_value IS NULL AND heal_amount IS NULL)
        OR (type <> 'MATERIAL')
    );

ALTER TABLE item_definitions
    ADD CONSTRAINT chk_item_definitions_accessory_stats CHECK (
        (type = 'ACCESSORY' AND weapon_damage IS NULL AND armor_value IS NULL AND heal_amount IS NULL)
        OR (type <> 'ACCESSORY')
    );

UPDATE item_definitions
SET weapon_family = 'SWORD'
WHERE code IN ('RUSTY_SWORD', 'IRON_SWORD');

UPDATE item_definitions
SET weapon_family = 'DAGGER'
WHERE code = 'OLD_DAGGER';

UPDATE item_definitions
SET armor_category = 'LIGHT'
WHERE code IN ('WORN_LEATHER_ARMOR', 'LEATHER_ARMOR');

ALTER TABLE item_definitions
    ADD CONSTRAINT chk_item_definitions_weapon_family CHECK (
        (type = 'WEAPON' AND weapon_family IN ('SWORD', 'AXE', 'MACE', 'DAGGER', 'BOW'))
        OR (type <> 'WEAPON' AND weapon_family IS NULL)
    );

ALTER TABLE item_definitions
    ADD CONSTRAINT chk_item_definitions_armor_category CHECK (
        (type = 'ARMOR' AND armor_category IN ('LIGHT', 'MEDIUM', 'HEAVY'))
        OR (type <> 'ARMOR' AND armor_category IS NULL)
    );

ALTER TABLE item_definitions
    ADD CONSTRAINT chk_item_definitions_two_handed CHECK (
        (two_handed = FALSE)
        OR (two_handed = TRUE AND type = 'WEAPON' AND equipment_slot = 'MAIN_HAND')
    );

ALTER TABLE item_definitions
    ADD CONSTRAINT chk_item_definitions_equippable_slot CHECK (
        (type IN ('WEAPON', 'ARMOR', 'ACCESSORY') AND equipment_slot IS NOT NULL)
        OR (type IN ('CONSUMABLE', 'MATERIAL') AND equipment_slot IS NULL)
    );

ALTER TABLE item_instances
    ADD COLUMN rarity VARCHAR(32),
    ADD COLUMN rolled_weapon_damage INT,
    ADD COLUMN rolled_armor_value INT;

UPDATE item_instances i
SET rarity = d.rarity,
    rolled_weapon_damage = d.weapon_damage,
    rolled_armor_value = d.armor_value
FROM item_definitions d
WHERE i.item_definition_id = d.id;

ALTER TABLE item_instances
    ALTER COLUMN rarity SET NOT NULL;

ALTER TABLE item_instances
    ADD CONSTRAINT chk_item_instances_rarity CHECK (rarity IN ('COMMON', 'UNCOMMON', 'RARE', 'EPIC'));

ALTER TABLE item_instances
    ADD CONSTRAINT chk_item_instances_rolled_weapon CHECK (
        rolled_weapon_damage IS NULL OR rolled_weapon_damage >= 0
    );

ALTER TABLE item_instances
    ADD CONSTRAINT chk_item_instances_rolled_armor CHECK (
        rolled_armor_value IS NULL OR rolled_armor_value >= 0
    );

CREATE TABLE affix_definitions (
    code                     VARCHAR(64) PRIMARY KEY,
    kind                     VARCHAR(16) NOT NULL,
    display_name             VARCHAR(64) NOT NULL,
    stat                     VARCHAR(32) NOT NULL,
    magnitude_min            INT NOT NULL,
    magnitude_max            INT NOT NULL,
    allowed_item_types       VARCHAR(128) NOT NULL DEFAULT '',
    allowed_equipment_slots  VARCHAR(256) NOT NULL DEFAULT '',
    allowed_weapon_families  VARCHAR(128) NOT NULL DEFAULT '',
    allowed_armor_categories VARCHAR(64) NOT NULL DEFAULT '',
    CONSTRAINT chk_affix_definitions_kind CHECK (kind IN ('PREFIX', 'SUFFIX')),
    CONSTRAINT chk_affix_definitions_stat CHECK (stat IN (
        'DAMAGE_PERCENT', 'ACCURACY', 'CRIT_CHANCE', 'ARMOR',
        'STRENGTH', 'AGILITY', 'ENDURANCE', 'PERCEPTION',
        'DODGE', 'STAMINA_COST'
    )),
    CONSTRAINT chk_affix_definitions_magnitude CHECK (
        magnitude_min >= 1 AND magnitude_max >= magnitude_min
    )
);

CREATE TABLE item_instance_affixes (
    id                 UUID PRIMARY KEY,
    item_instance_id   UUID NOT NULL,
    kind               VARCHAR(16) NOT NULL,
    ordinal            INT NOT NULL,
    affix_code         VARCHAR(64) NOT NULL,
    rolled_magnitude   INT NOT NULL,
    CONSTRAINT fk_item_instance_affixes_instance FOREIGN KEY (item_instance_id)
        REFERENCES item_instances (id) ON DELETE CASCADE,
    CONSTRAINT fk_item_instance_affixes_affix FOREIGN KEY (affix_code)
        REFERENCES affix_definitions (code),
    CONSTRAINT uq_item_instance_affixes_slot UNIQUE (item_instance_id, kind, ordinal),
    CONSTRAINT chk_item_instance_affixes_kind CHECK (kind IN ('PREFIX', 'SUFFIX')),
    CONSTRAINT chk_item_instance_affixes_ordinal CHECK (ordinal >= 0),
    CONSTRAINT chk_item_instance_affixes_magnitude CHECK (rolled_magnitude >= 0)
);

CREATE INDEX idx_item_instance_affixes_instance ON item_instance_affixes (item_instance_id);

INSERT INTO affix_definitions (
    code, kind, display_name, stat, magnitude_min, magnitude_max,
    allowed_item_types, allowed_equipment_slots, allowed_weapon_families, allowed_armor_categories
) VALUES
    ('SHARP', 'PREFIX', 'Sharp', 'DAMAGE_PERCENT', 4, 8, 'WEAPON', '', '', ''),
    ('BALANCED', 'PREFIX', 'Balanced', 'ACCURACY', 3, 6, 'WEAPON', '', '', ''),
    ('VICIOUS', 'PREFIX', 'Vicious', 'CRIT_CHANCE', 2, 4, 'WEAPON', '', '', ''),
    ('QUICK', 'PREFIX', 'Quick', 'STAMINA_COST', 1, 3, 'WEAPON', '', '', ''),
    ('REINFORCED', 'PREFIX', 'Reinforced', 'ARMOR', 2, 5, 'ARMOR', '', '', ''),
    ('FORTIFIED', 'PREFIX', 'Fortified', 'ARMOR', 3, 6, 'ARMOR', '', '', ''),
    ('HARDENED', 'PREFIX', 'Hardened', 'ARMOR', 1, 3, 'ARMOR', '', '', ''),
    ('NIMBLE', 'PREFIX', 'Nimble', 'DODGE', 2, 4, 'ARMOR', '', '', 'LIGHT'),
    ('GUARDED', 'PREFIX', 'Guarded', 'ARMOR', 1, 3, 'ARMOR', 'OFF_HAND', '', ''),
    ('GLIMMERING', 'PREFIX', 'Glimmering', 'CRIT_CHANCE', 1, 3, 'ACCESSORY', '', '', ''),
    ('WARDING', 'PREFIX', 'Warding', 'ARMOR', 1, 2, 'ACCESSORY', '', '', ''),
    ('SWIFT', 'PREFIX', 'Swift', 'DODGE', 1, 3, 'ACCESSORY', '', '', ''),
    ('OF_STRENGTH', 'SUFFIX', 'of Strength', 'STRENGTH', 1, 3, 'WEAPON,ARMOR,ACCESSORY', '', '', ''),
    ('OF_THE_FOX', 'SUFFIX', 'of the Fox', 'AGILITY', 1, 3, 'WEAPON,ARMOR,ACCESSORY', '', '', ''),
    ('OF_VITALITY', 'SUFFIX', 'of Vitality', 'ENDURANCE', 1, 3, 'WEAPON,ARMOR,ACCESSORY', '', '', ''),
    ('OF_PRECISION', 'SUFFIX', 'of Precision', 'PERCEPTION', 1, 3, 'WEAPON,ARMOR,ACCESSORY', '', '', '');

INSERT INTO item_definitions (
    id, code, name, description, type, rarity, base_value, required_level,
    weapon_damage, armor_value, heal_amount, created_at,
    equipment_slot, two_handed, weapon_family, armor_category,
    required_strength, required_agility, required_endurance, required_perception, legacy
) VALUES
    ('c0000000-0000-4000-8000-000000000008', 'HUNTING_BOW', 'Hunting Bow',
     'A two-handed bow used by Greyhaven scouts.',
     'WEAPON', 'COMMON', 18, 1, 8, NULL, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     'MAIN_HAND', TRUE, 'BOW', NULL, 0, 0, 0, 0, FALSE),
    ('c0000000-0000-4000-8000-000000000009', 'IRON_AXE', 'Iron Axe',
     'A heavy axe that trades grace for force.',
     'WEAPON', 'UNCOMMON', 28, 2, 13, NULL, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     'MAIN_HAND', FALSE, 'AXE', NULL, 0, 0, 0, 0, FALSE),
    ('c0000000-0000-4000-8000-00000000000a', 'IRON_MACE', 'Iron Mace',
     'A blunt iron head made to defeat armor.',
     'WEAPON', 'UNCOMMON', 26, 2, 11, NULL, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     'MAIN_HAND', FALSE, 'MACE', NULL, 0, 0, 0, 0, FALSE),
    ('c0000000-0000-4000-8000-00000000000b', 'WOODEN_BUCKLER', 'Wooden Buckler',
     'A light off-hand shield.',
     'ARMOR', 'COMMON', 12, 1, NULL, 2, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     'OFF_HAND', FALSE, NULL, 'LIGHT', 0, 0, 0, 0, FALSE),
    ('c0000000-0000-4000-8000-00000000000c', 'LEATHER_CAP', 'Leather Cap',
     'A simple leather cap.',
     'ARMOR', 'COMMON', 8, 1, NULL, 1, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     'HEAD', FALSE, NULL, 'LIGHT', 0, 0, 0, 0, FALSE),
    ('c0000000-0000-4000-8000-00000000000d', 'IRON_HELM', 'Iron Helm',
     'A heavy helm that demands strength.',
     'ARMOR', 'UNCOMMON', 40, 1, NULL, 4, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     'HEAD', FALSE, NULL, 'HEAVY', 8, 0, 6, 0, FALSE),
    ('c0000000-0000-4000-8000-00000000000e', 'LEATHER_GLOVES', 'Leather Gloves',
     'Supple gloves that keep hands free.',
     'ARMOR', 'COMMON', 7, 1, NULL, 1, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     'HANDS', FALSE, NULL, 'LIGHT', 0, 0, 0, 0, FALSE),
    ('c0000000-0000-4000-8000-00000000000f', 'LEATHER_LEGGINGS', 'Leather Leggings',
     'Light protection for the legs.',
     'ARMOR', 'COMMON', 10, 1, NULL, 2, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     'LEGS', FALSE, NULL, 'LIGHT', 0, 0, 0, 0, FALSE),
    ('c0000000-0000-4000-8000-000000000010', 'LEATHER_BOOTS', 'Leather Boots',
     'Quiet boots for city streets.',
     'ARMOR', 'COMMON', 8, 1, NULL, 1, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     'FEET', FALSE, NULL, 'LIGHT', 0, 0, 0, 0, FALSE),
    ('c0000000-0000-4000-8000-000000000011', 'MAIL_HAUBERK', 'Mail Hauberk',
     'Balanced medium armor for Greyhaven guards.',
     'ARMOR', 'UNCOMMON', 45, 4, NULL, 8, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     'CHEST', FALSE, NULL, 'MEDIUM', 6, 0, 0, 0, FALSE),
    ('c0000000-0000-4000-8000-000000000012', 'IRON_PLATE', 'Iron Plate',
     'Heavy chest armor with demanding requirements.',
     'ARMOR', 'RARE', 90, 8, NULL, 12, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     'CHEST', FALSE, NULL, 'HEAVY', 14, 0, 10, 0, FALSE),
    ('c0000000-0000-4000-8000-000000000013', 'COPPER_AMULET', 'Copper Amulet',
     'A modest charm worn at the throat.',
     'ACCESSORY', 'COMMON', 15, 1, NULL, NULL, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     'AMULET', FALSE, NULL, NULL, 0, 0, 0, 0, FALSE),
    ('c0000000-0000-4000-8000-000000000014', 'COPPER_RING', 'Copper Ring',
     'A simple copper band.',
     'ACCESSORY', 'COMMON', 12, 1, NULL, NULL, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     'RING', FALSE, NULL, NULL, 0, 0, 0, 0, FALSE);
