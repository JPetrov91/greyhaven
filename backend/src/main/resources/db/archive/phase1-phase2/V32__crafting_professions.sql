-- Crafting professions, recipes, jobs, salvage catalog, resource items, activity types.

ALTER TABLE activity_entries DROP CONSTRAINT chk_activity_entries_type;
ALTER TABLE activity_entries ADD CONSTRAINT chk_activity_entries_type CHECK (
	type IN (
		'COMBAT_VICTORY',
		'LEVEL_UP',
		'ITEM_FOUND',
		'EXPEDITION_COMPLETED',
		'EXPEDITION_CLAIMED',
		'MARKET_SOLD',
		'MARKET_BOUGHT',
		'MARKET_CANCELLED',
		'MASTERY_UNLOCK',
		'TECHNIQUE_UNLOCK',
		'ARENA_VICTORY',
		'ARENA_DEFEAT',
		'DUEL_RESULT',
		'CRAFTING_STARTED',
		'CRAFTING_CLAIMED',
		'PROFESSION_RANK_UP',
		'ITEM_SALVAGED',
		'MARKET_LISTING_FEE',
		'MARKET_SALE',
		'BUY_ORDER_CREATED',
		'BUY_ORDER_FILLED',
		'BUY_ORDER_CANCELLED'
	)
);

INSERT INTO item_definitions (
    id, code, name, description, type, rarity, base_value, required_level,
    weapon_damage, armor_value, heal_amount, created_at,
    equipment_slot, two_handed, weapon_family, armor_category,
    required_strength, required_agility, required_endurance, required_perception, legacy
) VALUES
    ('c0000000-0000-4000-8000-000000000020', 'IRON_INGOT', 'Iron Ingot',
     'Smelted iron ready for the forge.',
     'MATERIAL', 'COMMON', 12, 1, NULL, NULL, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     NULL, FALSE, NULL, NULL, 0, 0, 0, 0, FALSE),
    ('c0000000-0000-4000-8000-000000000021', 'CURED_LEATHER', 'Cured Leather',
     'Hide worked until it holds a stitch.',
     'MATERIAL', 'COMMON', 10, 1, NULL, NULL, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     NULL, FALSE, NULL, NULL, 0, 0, 0, 0, FALSE),
    ('c0000000-0000-4000-8000-000000000022', 'LEATHER_STRIPS', 'Leather Strips',
     'Cut bindings for light gear.',
     'MATERIAL', 'COMMON', 4, 1, NULL, NULL, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     NULL, FALSE, NULL, NULL, 0, 0, 0, 0, FALSE),
    ('c0000000-0000-4000-8000-000000000023', 'WEAPON_COMPONENTS', 'Weapon Components',
     'Salvaged fittings and blades from broken arms.',
     'MATERIAL', 'COMMON', 6, 1, NULL, NULL, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     NULL, FALSE, NULL, NULL, 0, 0, 0, 0, FALSE),
    ('c0000000-0000-4000-8000-000000000024', 'ARMOR_SCRAPS', 'Armor Scraps',
     'Torn plates and hide left after salvage.',
     'MATERIAL', 'COMMON', 5, 1, NULL, NULL, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     NULL, FALSE, NULL, NULL, 0, 0, 0, 0, FALSE),
    ('c0000000-0000-4000-8000-000000000025', 'RIVER_HERB', 'River Herb',
     'A bitter green used in Greyhaven draughts.',
     'MATERIAL', 'COMMON', 3, 1, NULL, NULL, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     NULL, FALSE, NULL, NULL, 0, 0, 0, 0, FALSE),
    ('c0000000-0000-4000-8000-000000000026', 'GREATER_HEALING_POTION', 'Greater Healing Potion',
     'A stronger draught that knits deeper wounds.',
     'CONSUMABLE', 'UNCOMMON', 22, 1, NULL, NULL, 80, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     NULL, FALSE, NULL, NULL, 0, 0, 0, 0, FALSE);

CREATE TABLE character_professions (
    id              UUID PRIMARY KEY,
    character_id    UUID NOT NULL,
    profession      VARCHAR(32) NOT NULL,
    xp              INT NOT NULL,
    rank            INT NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_character_professions_character FOREIGN KEY (character_id) REFERENCES characters (id),
    CONSTRAINT uq_character_professions UNIQUE (character_id, profession),
    CONSTRAINT chk_character_professions_profession CHECK (profession IN ('BLACKSMITH', 'ALCHEMIST', 'HUNTER')),
    CONSTRAINT chk_character_professions_xp CHECK (xp >= 0),
    CONSTRAINT chk_character_professions_rank CHECK (rank >= 1 AND rank <= 10)
);

CREATE INDEX idx_character_professions_character ON character_professions (character_id);

INSERT INTO character_professions (id, character_id, profession, xp, rank, updated_at)
SELECT gen_random_uuid(), c.id, p.profession, 0, 1, TIMESTAMPTZ '2026-01-01 00:00:00+00'
FROM characters c
CROSS JOIN (VALUES ('BLACKSMITH'), ('ALCHEMIST'), ('HUNTER')) AS p(profession);

CREATE TABLE crafting_recipes (
    id                          UUID PRIMARY KEY,
    code                        VARCHAR(64) NOT NULL,
    name                        VARCHAR(128) NOT NULL,
    profession                  VARCHAR(32) NOT NULL,
    required_profession_rank    INT NOT NULL,
    required_character_level    INT NOT NULL,
    gold_cost                   INT NOT NULL,
    duration_seconds            INT NOT NULL,
    output_item_definition_id   UUID NOT NULL,
    output_quantity             INT NOT NULL,
    min_rarity                  VARCHAR(32),
    max_rarity                  VARCHAR(32),
    profession_xp               INT NOT NULL,
    CONSTRAINT uq_crafting_recipes_code UNIQUE (code),
    CONSTRAINT fk_crafting_recipes_output FOREIGN KEY (output_item_definition_id) REFERENCES item_definitions (id),
    CONSTRAINT chk_crafting_recipes_profession CHECK (profession IN ('BLACKSMITH', 'ALCHEMIST', 'HUNTER')),
    CONSTRAINT chk_crafting_recipes_rank CHECK (required_profession_rank >= 1 AND required_profession_rank <= 10),
    CONSTRAINT chk_crafting_recipes_level CHECK (required_character_level >= 1),
    CONSTRAINT chk_crafting_recipes_gold CHECK (gold_cost >= 0),
    CONSTRAINT chk_crafting_recipes_duration CHECK (duration_seconds >= 1),
    CONSTRAINT chk_crafting_recipes_output_qty CHECK (output_quantity >= 1),
    CONSTRAINT chk_crafting_recipes_xp CHECK (profession_xp >= 0),
    CONSTRAINT chk_crafting_recipes_rarity CHECK (
        (min_rarity IS NULL AND max_rarity IS NULL)
        OR (
            min_rarity IN ('COMMON', 'UNCOMMON', 'RARE', 'EPIC')
            AND max_rarity IN ('COMMON', 'UNCOMMON', 'RARE', 'EPIC')
        )
    )
);

CREATE TABLE crafting_recipe_inputs (
    id                      UUID PRIMARY KEY,
    recipe_id               UUID NOT NULL,
    item_definition_id      UUID NOT NULL,
    quantity                INT NOT NULL,
    CONSTRAINT fk_crafting_recipe_inputs_recipe FOREIGN KEY (recipe_id) REFERENCES crafting_recipes (id),
    CONSTRAINT fk_crafting_recipe_inputs_item FOREIGN KEY (item_definition_id) REFERENCES item_definitions (id),
    CONSTRAINT uq_crafting_recipe_inputs UNIQUE (recipe_id, item_definition_id),
    CONSTRAINT chk_crafting_recipe_inputs_qty CHECK (quantity >= 1)
);

CREATE INDEX idx_crafting_recipe_inputs_recipe ON crafting_recipe_inputs (recipe_id);

INSERT INTO crafting_recipes (
    id, code, name, profession, required_profession_rank, required_character_level,
    gold_cost, duration_seconds, output_item_definition_id, output_quantity,
    min_rarity, max_rarity, profession_xp
) VALUES
    ('f0000000-0000-4000-8000-000000000001', 'SMELT_IRON_INGOT', 'Smelt Iron Ingot',
     'BLACKSMITH', 1, 1, 2, 60, 'c0000000-0000-4000-8000-000000000020', 1, NULL, NULL, 12),
    ('f0000000-0000-4000-8000-000000000002', 'FORGE_IRON_SWORD', 'Forge Iron Sword',
     'BLACKSMITH', 2, 1, 8, 180, 'c0000000-0000-4000-8000-000000000003', 1, 'COMMON', 'RARE', 20),
    ('f0000000-0000-4000-8000-000000000003', 'FORGE_IRON_AXE', 'Forge Iron Axe',
     'BLACKSMITH', 3, 1, 8, 180, 'c0000000-0000-4000-8000-000000000009', 1, 'COMMON', 'RARE', 22),
    ('f0000000-0000-4000-8000-000000000004', 'FORGE_IRON_HELM', 'Forge Iron Helm',
     'BLACKSMITH', 4, 1, 10, 210, 'c0000000-0000-4000-8000-00000000000d', 1, 'COMMON', 'RARE', 24),
    ('f0000000-0000-4000-8000-000000000005', 'FORGE_IRON_PLATE', 'Forge Iron Plate',
     'BLACKSMITH', 7, 8, 18, 300, 'c0000000-0000-4000-8000-000000000012', 1, 'COMMON', 'EPIC', 40),
    ('f0000000-0000-4000-8000-000000000006', 'BREW_HEALING_POTION', 'Brew Healing Potion',
     'ALCHEMIST', 1, 1, 4, 90, 'c0000000-0000-4000-8000-000000000006', 1, NULL, NULL, 12),
    ('f0000000-0000-4000-8000-000000000007', 'BREW_GREATER_HEALING_POTION', 'Brew Greater Healing Potion',
     'ALCHEMIST', 5, 5, 12, 150, 'c0000000-0000-4000-8000-000000000026', 1, NULL, NULL, 28),
    ('f0000000-0000-4000-8000-000000000008', 'CURE_LEATHER', 'Cure Leather',
     'HUNTER', 1, 1, 2, 60, 'c0000000-0000-4000-8000-000000000021', 1, NULL, NULL, 12),
    ('f0000000-0000-4000-8000-000000000009', 'CUT_LEATHER_STRIPS', 'Cut Leather Strips',
     'HUNTER', 1, 1, 1, 45, 'c0000000-0000-4000-8000-000000000022', 2, NULL, NULL, 10),
    ('f0000000-0000-4000-8000-00000000000a', 'CRAFT_LEATHER_CAP', 'Craft Leather Cap',
     'HUNTER', 2, 1, 6, 150, 'c0000000-0000-4000-8000-00000000000c', 1, 'COMMON', 'UNCOMMON', 18),
    ('f0000000-0000-4000-8000-00000000000b', 'CRAFT_LEATHER_GLOVES', 'Craft Leather Gloves',
     'HUNTER', 3, 1, 6, 150, 'c0000000-0000-4000-8000-00000000000e', 1, 'COMMON', 'UNCOMMON', 20),
    ('f0000000-0000-4000-8000-00000000000c', 'CRAFT_LEATHER_ARMOR', 'Craft Leather Armor',
     'HUNTER', 4, 1, 10, 210, 'c0000000-0000-4000-8000-000000000004', 1, 'COMMON', 'RARE', 26);

INSERT INTO crafting_recipe_inputs (id, recipe_id, item_definition_id, quantity) VALUES
    ('f1000000-0000-4000-8000-000000000001', 'f0000000-0000-4000-8000-000000000001', 'c0000000-0000-4000-8000-00000000001c', 3),
    ('f1000000-0000-4000-8000-000000000002', 'f0000000-0000-4000-8000-000000000002', 'c0000000-0000-4000-8000-000000000020', 4),
    ('f1000000-0000-4000-8000-000000000003', 'f0000000-0000-4000-8000-000000000003', 'c0000000-0000-4000-8000-000000000020', 4),
    ('f1000000-0000-4000-8000-000000000004', 'f0000000-0000-4000-8000-000000000004', 'c0000000-0000-4000-8000-000000000020', 3),
    ('f1000000-0000-4000-8000-000000000005', 'f0000000-0000-4000-8000-000000000005', 'c0000000-0000-4000-8000-000000000020', 6),
    ('f1000000-0000-4000-8000-000000000006', 'f0000000-0000-4000-8000-000000000006', 'c0000000-0000-4000-8000-000000000025', 2),
    ('f1000000-0000-4000-8000-000000000007', 'f0000000-0000-4000-8000-000000000007', 'c0000000-0000-4000-8000-000000000025', 4),
    ('f1000000-0000-4000-8000-000000000008', 'f0000000-0000-4000-8000-000000000007', 'c0000000-0000-4000-8000-000000000006', 1),
    ('f1000000-0000-4000-8000-000000000009', 'f0000000-0000-4000-8000-000000000008', 'c0000000-0000-4000-8000-000000000007', 2),
    ('f1000000-0000-4000-8000-00000000000a', 'f0000000-0000-4000-8000-000000000009', 'c0000000-0000-4000-8000-000000000021', 1),
    ('f1000000-0000-4000-8000-00000000000b', 'f0000000-0000-4000-8000-00000000000a', 'c0000000-0000-4000-8000-000000000021', 2),
    ('f1000000-0000-4000-8000-00000000000c', 'f0000000-0000-4000-8000-00000000000a', 'c0000000-0000-4000-8000-000000000022', 1),
    ('f1000000-0000-4000-8000-00000000000d', 'f0000000-0000-4000-8000-00000000000b', 'c0000000-0000-4000-8000-000000000021', 1),
    ('f1000000-0000-4000-8000-00000000000e', 'f0000000-0000-4000-8000-00000000000b', 'c0000000-0000-4000-8000-000000000022', 2),
    ('f1000000-0000-4000-8000-00000000000f', 'f0000000-0000-4000-8000-00000000000c', 'c0000000-0000-4000-8000-000000000021', 3),
    ('f1000000-0000-4000-8000-000000000010', 'f0000000-0000-4000-8000-00000000000c', 'c0000000-0000-4000-8000-000000000022', 2);

CREATE TABLE crafting_jobs (
    id                          UUID PRIMARY KEY,
    character_id                UUID NOT NULL,
    recipe_id                   UUID NOT NULL,
    profession                  VARCHAR(32) NOT NULL,
    status                      VARCHAR(32) NOT NULL,
    started_at                  TIMESTAMPTZ NOT NULL,
    completes_at                TIMESTAMPTZ NOT NULL,
    claimed_at                  TIMESTAMPTZ,
    result_generated            BOOLEAN NOT NULL,
    output_item_definition_id   UUID NOT NULL,
    output_item_code            VARCHAR(64) NOT NULL,
    output_quantity             INT NOT NULL,
    rarity                      VARCHAR(32),
    rolled_weapon_damage        INT,
    rolled_armor_value          INT,
    rolled_affixes              TEXT,
    profession_xp_planned       INT NOT NULL,
    version                     BIGINT NOT NULL,
    created_at                  TIMESTAMPTZ NOT NULL,
    updated_at                  TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_crafting_jobs_character FOREIGN KEY (character_id) REFERENCES characters (id),
    CONSTRAINT fk_crafting_jobs_recipe FOREIGN KEY (recipe_id) REFERENCES crafting_recipes (id),
    CONSTRAINT fk_crafting_jobs_output FOREIGN KEY (output_item_definition_id) REFERENCES item_definitions (id),
    CONSTRAINT chk_crafting_jobs_profession CHECK (profession IN ('BLACKSMITH', 'ALCHEMIST', 'HUNTER')),
    CONSTRAINT chk_crafting_jobs_status CHECK (status IN ('ACTIVE', 'COMPLETED', 'CLAIMED')),
    CONSTRAINT chk_crafting_jobs_output_qty CHECK (output_quantity >= 1),
    CONSTRAINT chk_crafting_jobs_xp CHECK (profession_xp_planned >= 0)
);

CREATE UNIQUE INDEX uq_crafting_jobs_one_open
    ON crafting_jobs (character_id)
    WHERE status IN ('ACTIVE', 'COMPLETED');

CREATE INDEX idx_crafting_jobs_character ON crafting_jobs (character_id, created_at DESC);

CREATE TABLE salvage_outputs (
    id                          UUID PRIMARY KEY,
    source_item_definition_id   UUID NOT NULL,
    result_item_definition_id   UUID NOT NULL,
    base_quantity               INT NOT NULL,
    CONSTRAINT fk_salvage_outputs_source FOREIGN KEY (source_item_definition_id) REFERENCES item_definitions (id),
    CONSTRAINT fk_salvage_outputs_result FOREIGN KEY (result_item_definition_id) REFERENCES item_definitions (id),
    CONSTRAINT uq_salvage_outputs UNIQUE (source_item_definition_id, result_item_definition_id),
    CONSTRAINT chk_salvage_outputs_qty CHECK (base_quantity >= 1)
);

INSERT INTO salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity)
SELECT gen_random_uuid(), id, 'c0000000-0000-4000-8000-000000000023', 1
FROM item_definitions
WHERE type = 'WEAPON';

INSERT INTO salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity)
SELECT gen_random_uuid(), id, 'c0000000-0000-4000-8000-000000000024', 1
FROM item_definitions
WHERE type IN ('ARMOR', 'ACCESSORY');
