-- FI-003: COMMON axe/mace/medium/heavy so NPC merchants cover baseline loadouts
-- without selling UNCOMMON or RARE catalog gear.

INSERT INTO item_definitions (
    id, code, name, description, type, rarity, base_value, required_level,
    weapon_damage, armor_value, heal_amount, created_at,
    equipment_slot, two_handed, weapon_family, armor_category,
    required_strength, required_agility, required_endurance, required_perception, legacy
) VALUES
    ('c0000000-0000-4000-8000-000000000015', 'WOODSMAN_AXE', 'Woodsman Axe',
     'A hatchet honest enough for Greyhaven''s gate guards.',
     'WEAPON', 'COMMON', 10, 1, 8, NULL, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     'MAIN_HAND', FALSE, 'AXE', NULL, 0, 0, 0, 0, FALSE),
    ('c0000000-0000-4000-8000-000000000016', 'KNOBBED_CLUB', 'Knobbed Club',
     'A weighted stick that still counts as a mace.',
     'WEAPON', 'COMMON', 9, 1, 7, NULL, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     'MAIN_HAND', FALSE, 'MACE', NULL, 0, 0, 0, 0, FALSE),
    ('c0000000-0000-4000-8000-000000000017', 'PADDED_JACK', 'Padded Jack',
     'Quilted medium armor for travellers who cannot wait on mail.',
     'ARMOR', 'COMMON', 12, 1, NULL, 5, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     'CHEST', FALSE, NULL, 'MEDIUM', 2, 0, 0, 0, FALSE),
    ('c0000000-0000-4000-8000-000000000018', 'SPLINT_VEST', 'Splint Vest',
     'Crude heavy plates on a leather backing. Better than hope.',
     'ARMOR', 'COMMON', 14, 1, NULL, 6, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     'CHEST', FALSE, NULL, 'HEAVY', 4, 0, 2, 0, FALSE);

INSERT INTO merchant_stock (
    id, merchant_id, item_definition_id, availability_type, sort_order
) VALUES
    ('e0000000-0000-4000-8000-00000000000e', 'd0000000-0000-4000-8000-000000000001',
     'c0000000-0000-4000-8000-000000000015', 'UNLIMITED', 3),
    ('e0000000-0000-4000-8000-00000000000f', 'd0000000-0000-4000-8000-000000000001',
     'c0000000-0000-4000-8000-000000000016', 'UNLIMITED', 4),
    ('e0000000-0000-4000-8000-000000000010', 'd0000000-0000-4000-8000-000000000002',
     'c0000000-0000-4000-8000-000000000017', 'UNLIMITED', 6),
    ('e0000000-0000-4000-8000-000000000011', 'd0000000-0000-4000-8000-000000000002',
     'c0000000-0000-4000-8000-000000000018', 'UNLIMITED', 7);
