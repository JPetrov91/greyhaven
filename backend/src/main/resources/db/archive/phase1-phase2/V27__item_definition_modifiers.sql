-- Catalog implicits live as rows, not as type-wide defaults and not as extra columns on
-- item_definitions. weapon_damage / armor_value / heal_amount stay on the definition as the
-- primary combat stat for that item type.

CREATE TABLE item_definition_modifiers (
    id                   UUID PRIMARY KEY,
    item_definition_id   UUID NOT NULL,
    stat                 VARCHAR(32) NOT NULL,
    magnitude            INT NOT NULL,
    CONSTRAINT fk_item_definition_modifiers_definition
        FOREIGN KEY (item_definition_id) REFERENCES item_definitions (id),
    CONSTRAINT uq_item_definition_modifiers_stat UNIQUE (item_definition_id, stat),
    CONSTRAINT chk_item_definition_modifiers_stat CHECK (stat IN (
        'DAMAGE_PERCENT', 'ACCURACY', 'CRIT_CHANCE', 'ARMOR',
        'STRENGTH', 'AGILITY', 'ENDURANCE', 'PERCEPTION',
        'DODGE', 'STAMINA_COST'
    )),
    CONSTRAINT chk_item_definition_modifiers_magnitude CHECK (magnitude <> 0)
);

CREATE INDEX idx_item_definition_modifiers_definition
    ON item_definition_modifiers (item_definition_id);

-- Current catalog values, keyed by item code so two swords can diverge later.
INSERT INTO item_definition_modifiers (id, item_definition_id, stat, magnitude)
SELECT gen_random_uuid(), d.id, seed.stat, seed.magnitude
FROM item_definitions d
JOIN (
    VALUES
        ('RUSTY_SWORD', 'ACCURACY', 4),
        ('RUSTY_SWORD', 'CRIT_CHANCE', 1),
        ('IRON_SWORD', 'ACCURACY', 4),
        ('IRON_SWORD', 'CRIT_CHANCE', 1),
        ('MILITIA_SHORTSWORD', 'ACCURACY', 4),
        ('MILITIA_SHORTSWORD', 'CRIT_CHANCE', 1),
        ('ARMING_SWORD', 'ACCURACY', 4),
        ('ARMING_SWORD', 'CRIT_CHANCE', 1),
        ('IRON_AXE', 'CRIT_CHANCE', 3),
        ('WOODSMAN_AXE', 'CRIT_CHANCE', 3),
        ('IRON_MACE', 'ARMOR', 1),
        ('IRON_MACE', 'ACCURACY', 2),
        ('KNOBBED_CLUB', 'ARMOR', 1),
        ('KNOBBED_CLUB', 'ACCURACY', 2),
        ('OLD_DAGGER', 'CRIT_CHANCE', 4),
        ('OLD_DAGGER', 'DODGE', 2),
        ('OLD_DAGGER', 'STAMINA_COST', 1),
        ('HUNTING_BOW', 'ACCURACY', 6),
        ('HUNTING_BOW', 'CRIT_CHANCE', 2),
        ('LEATHER_CAP', 'PERCEPTION', 1),
        ('IRON_HELM', 'PERCEPTION', 1),
        ('LEATHER_GLOVES', 'ACCURACY', 2),
        ('LEATHER_LEGGINGS', 'AGILITY', 1),
        ('LEATHER_BOOTS', 'DODGE', 1),
        ('WOODEN_BUCKLER', 'DODGE', 1),
        ('COPPER_AMULET', 'CRIT_CHANCE', 2),
        ('COPPER_RING', 'ACCURACY', 2)
) AS seed(code, stat, magnitude) ON d.code = seed.code;
