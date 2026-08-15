-- Two COMMON one-handed swords for early Greyhaven, sold by the weaponsmith.

INSERT INTO item_definitions (
    id, code, name, description, type, rarity, base_value, required_level,
    weapon_damage, armor_value, heal_amount, created_at,
    equipment_slot, two_handed, weapon_family, armor_category,
    required_strength, required_agility, required_endurance, required_perception, legacy
) VALUES
    ('c0000000-0000-4000-8000-000000000019', 'MILITIA_SHORTSWORD', 'Militia Shortsword',
     'A serviceable short blade issued to Greyhaven wall watch. Better steel than rust, still honest work.',
     'WEAPON', 'COMMON', 8, 1, 7, NULL, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     'MAIN_HAND', FALSE, 'SWORD', NULL, 0, 0, 0, 0, FALSE),
    ('c0000000-0000-4000-8000-00000000001a', 'ARMING_SWORD', 'Arming Sword',
     'A straight one-handed sword for travellers who have outgrown a notched starter.',
     'WEAPON', 'COMMON', 12, 1, 8, NULL, NULL, TIMESTAMPTZ '2026-01-01 00:00:00+00',
     'MAIN_HAND', FALSE, 'SWORD', NULL, 0, 0, 0, 0, FALSE);

INSERT INTO merchant_stock (
    id, merchant_id, item_definition_id, availability_type, sort_order
) VALUES
    ('e0000000-0000-4000-8000-000000000012', 'd0000000-0000-4000-8000-000000000001',
     'c0000000-0000-4000-8000-000000000019', 'UNLIMITED', 5),
    ('e0000000-0000-4000-8000-000000000013', 'd0000000-0000-4000-8000-000000000001',
     'c0000000-0000-4000-8000-00000000001a', 'UNLIMITED', 6);
