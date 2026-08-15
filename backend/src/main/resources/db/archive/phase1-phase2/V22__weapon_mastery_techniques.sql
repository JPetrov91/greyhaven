-- Weapon mastery progression, data-driven combat techniques, and a 4-slot loadout.

CREATE TABLE combat_technique_definitions (
    code                     VARCHAR(64) PRIMARY KEY,
    display_name             VARCHAR(64) NOT NULL,
    description              TEXT NOT NULL,
    weapon_family            VARCHAR(16) NOT NULL,
    unlock_mastery_level     INTEGER NOT NULL,
    kind                     VARCHAR(16) NOT NULL,
    effect_code              VARCHAR(64) NOT NULL,
    stamina_cost             INTEGER NOT NULL,
    accuracy_modifier        INTEGER NOT NULL,
    damage_percent_modifier  INTEGER NOT NULL,
    applies_status           VARCHAR(32),
    status_stacks            INTEGER NOT NULL,
    status_duration_rounds   INTEGER NOT NULL,
    tags                     VARCHAR(128) NOT NULL,
    CONSTRAINT chk_technique_weapon_family CHECK (
        weapon_family IN ('SWORD', 'AXE', 'MACE', 'DAGGER', 'BOW')
    ),
    CONSTRAINT chk_technique_unlock_level CHECK (
        unlock_mastery_level IN (2, 4, 6, 8, 10)
    ),
    CONSTRAINT chk_technique_kind CHECK (kind IN ('ACTIVE', 'PASSIVE')),
    CONSTRAINT chk_technique_stamina_cost CHECK (stamina_cost >= 0),
    CONSTRAINT chk_technique_status_stacks CHECK (status_stacks >= 0),
    CONSTRAINT chk_technique_status_duration CHECK (status_duration_rounds >= 0),
    CONSTRAINT chk_technique_passive_level CHECK (
        (kind = 'PASSIVE' AND unlock_mastery_level = 10)
        OR (kind = 'ACTIVE' AND unlock_mastery_level IN (2, 4, 6, 8))
    )
);

CREATE TABLE weapon_masteries (
    id                UUID PRIMARY KEY,
    character_id      UUID NOT NULL,
    weapon_family     VARCHAR(16) NOT NULL,
    total_experience  INTEGER NOT NULL,
    level             INTEGER NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_weapon_masteries_character
        FOREIGN KEY (character_id) REFERENCES characters (id),
    CONSTRAINT uq_weapon_masteries_character_family UNIQUE (character_id, weapon_family),
    CONSTRAINT chk_weapon_masteries_family CHECK (
        weapon_family IN ('SWORD', 'AXE', 'MACE', 'DAGGER', 'BOW')
    ),
    CONSTRAINT chk_weapon_masteries_level CHECK (level BETWEEN 0 AND 10),
    CONSTRAINT chk_weapon_masteries_xp CHECK (total_experience >= 0)
);

CREATE INDEX idx_weapon_masteries_character ON weapon_masteries (character_id);

CREATE TABLE character_techniques (
    id              UUID PRIMARY KEY,
    character_id    UUID NOT NULL,
    technique_code  VARCHAR(64) NOT NULL,
    unlocked_at     TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_character_techniques_character
        FOREIGN KEY (character_id) REFERENCES characters (id),
    CONSTRAINT fk_character_techniques_definition
        FOREIGN KEY (technique_code) REFERENCES combat_technique_definitions (code),
    CONSTRAINT uq_character_techniques_character_code UNIQUE (character_id, technique_code)
);

CREATE INDEX idx_character_techniques_character ON character_techniques (character_id);

CREATE TABLE technique_loadout_slots (
    id              UUID PRIMARY KEY,
    character_id    UUID NOT NULL,
    slot_index      INTEGER NOT NULL,
    technique_code  VARCHAR(64),
    CONSTRAINT fk_technique_loadout_character
        FOREIGN KEY (character_id) REFERENCES characters (id),
    CONSTRAINT fk_technique_loadout_definition
        FOREIGN KEY (technique_code) REFERENCES combat_technique_definitions (code),
    CONSTRAINT uq_technique_loadout_character_slot UNIQUE (character_id, slot_index),
    CONSTRAINT chk_technique_loadout_slot CHECK (slot_index BETWEEN 0 AND 3)
);

CREATE UNIQUE INDEX uq_technique_loadout_character_technique
    ON technique_loadout_slots (character_id, technique_code)
    WHERE technique_code IS NOT NULL;

CREATE INDEX idx_technique_loadout_character ON technique_loadout_slots (character_id);

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
        'TECHNIQUE_UNLOCK'
    )
);

INSERT INTO combat_technique_definitions (
    code, display_name, description, weapon_family, unlock_mastery_level, kind,
    effect_code, stamina_cost, accuracy_modifier, damage_percent_modifier,
    applies_status, status_stacks, status_duration_rounds, tags
) VALUES
    ('SWORD_RIPOSTE', 'Riposte',
     'A precise counter after an opening. Contract for Combat 2.0.',
     'SWORD', 2, 'ACTIVE', 'RIPOSTE', 8, 8, 0, NULL, 0, 0, 'COUNTER'),
    ('SWORD_DEEP_CUT', 'Deep Cut',
     'A heavier slash intended to open a bleeding wound.',
     'SWORD', 4, 'ACTIVE', 'DEEP_CUT', 12, 0, 15, 'BLEED', 1, 3, ''),
    ('SWORD_GUARD_BREAK', 'Guard Break',
     'Forces the opponent off balance by hammering their guard.',
     'SWORD', 6, 'ACTIVE', 'GUARD_BREAK', 14, -4, 10, 'OFF_BALANCE', 1, 1, ''),
    ('SWORD_DUELISTS_TEMPO', 'Duelist''s Tempo',
     'An advanced sequence that rewards measured swordplay.',
     'SWORD', 8, 'ACTIVE', 'DUELISTS_TEMPO', 16, 6, 20, NULL, 0, 0, 'ADVANCED'),
    ('SWORD_MASTERY', 'Sword Mastery',
     'Passive familiarity with blades. Resolved in Combat 2.0.',
     'SWORD', 10, 'PASSIVE', 'SWORD_MASTERY', 0, 4, 5, NULL, 0, 0, 'MASTERY_PASSIVE'),

    ('AXE_RENDING_CHOP', 'Rending Chop',
     'A brutal chop meant to tear through flesh.',
     'AXE', 2, 'ACTIVE', 'RENDING_CHOP', 10, -2, 18, 'BLEED', 1, 2, ''),
    ('AXE_CLEAVE', 'Cleave',
     'A wide swing. Contract reserved for Combat 2.0.',
     'AXE', 4, 'ACTIVE', 'CLEAVE', 14, -6, 22, NULL, 0, 0, 'CLEAVE'),
    ('AXE_SHATTER_ARMOR', 'Shatter Armor',
     'Splits protection and leaves the target easier to wound.',
     'AXE', 6, 'ACTIVE', 'SHATTER_ARMOR', 14, -4, 12, 'ARMOR_BREAK', 1, 3, ''),
    ('AXE_EXECUTIONER', 'Executioner',
     'An advanced finishing blow against wounded foes.',
     'AXE', 8, 'ACTIVE', 'EXECUTIONER', 18, 0, 28, NULL, 0, 0, 'ADVANCED'),
    ('AXE_MASTERY', 'Axe Mastery',
     'Passive familiarity with axes. Resolved in Combat 2.0.',
     'AXE', 10, 'PASSIVE', 'AXE_MASTERY', 0, 0, 8, NULL, 0, 0, 'MASTERY_PASSIVE'),

    ('MACE_CRUSHING_BLOW', 'Crushing Blow',
     'A heavy strike that trades finesse for impact.',
     'MACE', 2, 'ACTIVE', 'CRUSHING_BLOW', 10, -4, 20, NULL, 0, 0, ''),
    ('MACE_CONCUSSIVE_STRIKE', 'Concussive Strike',
     'A stunning impact. Anti-chain rules belong to Combat 2.0.',
     'MACE', 4, 'ACTIVE', 'CONCUSSIVE_STRIKE', 16, -2, 8, 'STUN', 1, 1, ''),
    ('MACE_BREAK_GUARD', 'Break Guard',
     'Smashes through a defensive stance.',
     'MACE', 6, 'ACTIVE', 'BREAK_GUARD', 14, -4, 10, 'ARMOR_BREAK', 1, 2, ''),
    ('MACE_OVERWHELM', 'Overwhelm',
     'An advanced press that keeps the opponent reeling.',
     'MACE', 8, 'ACTIVE', 'OVERWHELM', 18, 0, 24, 'OFF_BALANCE', 1, 1, 'ADVANCED'),
    ('MACE_MASTERY', 'Mace Mastery',
     'Passive familiarity with blunt weapons. Resolved in Combat 2.0.',
     'MACE', 10, 'PASSIVE', 'MACE_MASTERY', 0, 0, 8, NULL, 0, 0, 'MASTERY_PASSIVE'),

    ('DAGGER_FEINT', 'Feint',
     'A deceptive cut that leaves the target off balance.',
     'DAGGER', 2, 'ACTIVE', 'FEINT', 6, 6, 0, 'OFF_BALANCE', 1, 1, ''),
    ('DAGGER_POISONED_STRIKE', 'Poisoned Strike',
     'Delivers lingering toxin. Combat 2.0 owns the tick.',
     'DAGGER', 4, 'ACTIVE', 'POISONED_STRIKE', 10, 2, 5, 'POISON', 1, 4, ''),
    ('DAGGER_EVASIVE_CUT', 'Evasive Cut',
     'A light attack that favors positioning over power.',
     'DAGGER', 6, 'ACTIVE', 'EVASIVE_CUT', 8, 8, 8, NULL, 0, 0, ''),
    ('DAGGER_FINISHER', 'Finisher',
     'An advanced strike against a compromised foe.',
     'DAGGER', 8, 'ACTIVE', 'FINISHER', 14, 4, 30, NULL, 0, 0, 'ADVANCED'),
    ('DAGGER_MASTERY', 'Dagger Mastery',
     'Passive familiarity with daggers. Resolved in Combat 2.0.',
     'DAGGER', 10, 'PASSIVE', 'DAGGER_MASTERY', 0, 6, 4, NULL, 0, 0, 'MASTERY_PASSIVE'),

    ('BOW_AIMED_SHOT', 'Aimed Shot',
     'A careful shot that favors accuracy.',
     'BOW', 2, 'ACTIVE', 'AIMED_SHOT', 10, 12, 5, NULL, 0, 0, ''),
    ('BOW_CRIPPLING_SHOT', 'Crippling Shot',
     'A shot meant to hobble movement.',
     'BOW', 4, 'ACTIVE', 'CRIPPLING_SHOT', 12, 4, 8, 'OFF_BALANCE', 1, 2, ''),
    ('BOW_PIERCING_SHOT', 'Piercing Shot',
     'Ignores part of a target''s protection. Combat 2.0 resolves it.',
     'BOW', 6, 'ACTIVE', 'PIERCING_SHOT', 14, 2, 16, 'ARMOR_BREAK', 1, 2, ''),
    ('BOW_RAPID_SHOT', 'Rapid Shot',
     'An advanced flurry that spends stamina for tempo.',
     'BOW', 8, 'ACTIVE', 'RAPID_SHOT', 16, -2, 12, NULL, 0, 0, 'ADVANCED'),
    ('BOW_MASTERY', 'Bow Mastery',
     'Passive familiarity with bows. Resolved in Combat 2.0.',
     'BOW', 10, 'PASSIVE', 'BOW_MASTERY', 0, 6, 4, NULL, 0, 0, 'MASTERY_PASSIVE');
