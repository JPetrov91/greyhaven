-- Planned item rolls on reward rows so inventory-full retries cannot reroll rarity/affixes.
-- Phase 2 loot: Bandit / Bandit Veteran drop IRON_AXE instead of legacy IRON_SWORD.

ALTER TABLE combat_reward_items
    ADD COLUMN rarity VARCHAR(32),
    ADD COLUMN rolled_weapon_damage INT,
    ADD COLUMN rolled_armor_value INT,
    ADD COLUMN rolled_affixes TEXT NOT NULL DEFAULT '';

ALTER TABLE combat_reward_items
    ADD CONSTRAINT chk_combat_reward_items_rarity CHECK (
        rarity IS NULL OR rarity IN ('COMMON', 'UNCOMMON', 'RARE', 'EPIC')
    );

ALTER TABLE expedition_reward_items
    ADD COLUMN rarity VARCHAR(32),
    ADD COLUMN rolled_weapon_damage INT,
    ADD COLUMN rolled_armor_value INT,
    ADD COLUMN rolled_affixes TEXT NOT NULL DEFAULT '';

ALTER TABLE expedition_reward_items
    ADD CONSTRAINT chk_expedition_reward_items_rarity CHECK (
        rarity IS NULL OR rarity IN ('COMMON', 'UNCOMMON', 'RARE', 'EPIC')
    );

UPDATE monster_loot_entries
SET item_definition_id = 'c0000000-0000-4000-8000-000000000009'
WHERE item_definition_id = 'c0000000-0000-4000-8000-000000000003';
