-- An equipped item must belong to the character wearing it.
--
-- V6 referenced characters and item_instances with two independent foreign keys, so the schema
-- allowed an equipment row pointing at another character's item. The application never creates
-- one, but the invariant belongs in the database: once items change owner the application check
-- alone is no longer the only path to the table.

ALTER TABLE item_instances
    ADD CONSTRAINT uq_item_instances_id_owner UNIQUE (id, owner_character_id);

-- Subsumed by the composite key added below, which references the same row plus its owner.
ALTER TABLE equipment
    DROP CONSTRAINT fk_equipment_item_instance;

ALTER TABLE equipment
    ADD CONSTRAINT fk_equipment_owned_item FOREIGN KEY (item_instance_id, character_id)
        REFERENCES item_instances (id, owner_character_id);
