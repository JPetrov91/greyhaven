-- Stackable item instances must occupy a single inventory slot per definition.
-- Non-stackable gear may still have many rows sharing the same definition.

ALTER TABLE item_instances
    ADD COLUMN stackable BOOLEAN;

UPDATE item_instances AS i
SET stackable = (d.type IN ('CONSUMABLE', 'MATERIAL'))
FROM item_definitions AS d
WHERE d.id = i.item_definition_id;

ALTER TABLE item_instances
    ALTER COLUMN stackable SET NOT NULL;

ALTER TABLE item_instances
    ADD CONSTRAINT chk_item_instances_nonstackable_quantity
        CHECK (stackable OR quantity = 1);

CREATE UNIQUE INDEX uq_item_instances_owner_stackable_definition
    ON item_instances (owner_character_id, item_definition_id)
    WHERE stackable;
