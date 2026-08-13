-- Item catalog, owned instances, and equipment slots (Task 4).

CREATE TABLE item_definitions (
    id              UUID PRIMARY KEY,
    code            VARCHAR(64) NOT NULL,
    name            VARCHAR(128) NOT NULL,
    description     TEXT NOT NULL,
    type            VARCHAR(32) NOT NULL,
    rarity          VARCHAR(32) NOT NULL,
    base_value      INT NOT NULL,
    required_level  INT NOT NULL,
    weapon_damage   INT,
    armor_value     INT,
    heal_amount     INT,
    created_at      TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_item_definitions_code UNIQUE (code),
    CONSTRAINT chk_item_definitions_type CHECK (type IN ('WEAPON', 'ARMOR', 'CONSUMABLE', 'MATERIAL')),
    CONSTRAINT chk_item_definitions_rarity CHECK (rarity IN ('COMMON', 'UNCOMMON', 'RARE', 'EPIC')),
    CONSTRAINT chk_item_definitions_base_value CHECK (base_value >= 0),
    CONSTRAINT chk_item_definitions_required_level CHECK (required_level >= 1),
    CONSTRAINT chk_item_definitions_weapon_damage CHECK (weapon_damage IS NULL OR weapon_damage >= 0),
    CONSTRAINT chk_item_definitions_armor_value CHECK (armor_value IS NULL OR armor_value >= 0),
    CONSTRAINT chk_item_definitions_heal_amount CHECK (heal_amount IS NULL OR heal_amount > 0),
    CONSTRAINT chk_item_definitions_weapon_stats CHECK (
        (type = 'WEAPON' AND weapon_damage IS NOT NULL AND armor_value IS NULL AND heal_amount IS NULL)
        OR (type <> 'WEAPON')
    ),
    CONSTRAINT chk_item_definitions_armor_stats CHECK (
        (type = 'ARMOR' AND armor_value IS NOT NULL AND weapon_damage IS NULL AND heal_amount IS NULL)
        OR (type <> 'ARMOR')
    ),
    CONSTRAINT chk_item_definitions_consumable_stats CHECK (
        (type = 'CONSUMABLE' AND heal_amount IS NOT NULL AND weapon_damage IS NULL AND armor_value IS NULL)
        OR (type <> 'CONSUMABLE')
    ),
    CONSTRAINT chk_item_definitions_material_stats CHECK (
        (type = 'MATERIAL' AND weapon_damage IS NULL AND armor_value IS NULL AND heal_amount IS NULL)
        OR (type <> 'MATERIAL')
    )
);

CREATE TABLE item_instances (
    id                   UUID PRIMARY KEY,
    item_definition_id   UUID NOT NULL,
    owner_character_id   UUID NOT NULL,
    quantity             INT NOT NULL,
    created_at           TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_item_instances_definition FOREIGN KEY (item_definition_id) REFERENCES item_definitions (id),
    CONSTRAINT fk_item_instances_owner FOREIGN KEY (owner_character_id) REFERENCES characters (id),
    CONSTRAINT chk_item_instances_quantity CHECK (quantity >= 1)
);

CREATE INDEX idx_item_instances_owner ON item_instances (owner_character_id);
CREATE INDEX idx_item_instances_definition ON item_instances (item_definition_id);

CREATE TABLE equipment (
    id                 UUID PRIMARY KEY,
    character_id       UUID NOT NULL,
    slot               VARCHAR(16) NOT NULL,
    item_instance_id   UUID NOT NULL,
    CONSTRAINT fk_equipment_character FOREIGN KEY (character_id) REFERENCES characters (id),
    CONSTRAINT fk_equipment_item_instance FOREIGN KEY (item_instance_id) REFERENCES item_instances (id),
    CONSTRAINT uq_equipment_character_slot UNIQUE (character_id, slot),
    CONSTRAINT uq_equipment_item_instance UNIQUE (item_instance_id),
    CONSTRAINT chk_equipment_slot CHECK (slot IN ('WEAPON', 'ARMOR'))
);

CREATE INDEX idx_equipment_character ON equipment (character_id);

-- Deterministic catalog IDs for application and test references.
INSERT INTO item_definitions (
    id, code, name, description, type, rarity, base_value, required_level,
    weapon_damage, armor_value, heal_amount, created_at
) VALUES
    ('c0000000-0000-4000-8000-000000000001', 'RUSTY_SWORD', 'Rusty Sword',
     'A notched starter blade. Better than empty hands.',
     'WEAPON', 'COMMON', 5, 1, 6, NULL, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00'),
    ('c0000000-0000-4000-8000-000000000002', 'WORN_LEATHER_ARMOR', 'Worn Leather Armor',
     'Scuffed leather that still turns a glancing blow.',
     'ARMOR', 'COMMON', 5, 1, NULL, 3, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00'),
    ('c0000000-0000-4000-8000-000000000003', 'IRON_SWORD', 'Iron Sword',
     'A sturdy iron blade favored by city guards.',
     'WEAPON', 'UNCOMMON', 25, 2, 10, NULL, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00'),
    ('c0000000-0000-4000-8000-000000000004', 'LEATHER_ARMOR', 'Leather Armor',
     'Well-kept leather with reinforced stitching.',
     'ARMOR', 'UNCOMMON', 25, 2, NULL, 6, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00'),
    ('c0000000-0000-4000-8000-000000000005', 'OLD_DAGGER', 'Old Dagger',
     'A light blade that still finds soft spots.',
     'WEAPON', 'COMMON', 8, 1, 4, NULL, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00'),
    ('c0000000-0000-4000-8000-000000000006', 'HEALING_POTION', 'Healing Potion',
     'A bitter red tonic that knits minor wounds.',
     'CONSUMABLE', 'COMMON', 10, 1, NULL, NULL, 40, TIMESTAMPTZ '2026-01-01 00:00:00+00'),
    ('c0000000-0000-4000-8000-000000000007', 'WOLF_PELT', 'Wolf Pelt',
     'Thick fur from a forest wolf. Valuable to traders.',
     'MATERIAL', 'COMMON', 6, 1, NULL, NULL, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00');
