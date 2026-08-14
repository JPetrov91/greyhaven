-- FI-003: NPC merchant identities and unlimited core stock.
-- Prices are computed from item base_value and market balance, not stored here.

CREATE TABLE merchant_definitions (
    id            UUID PRIMARY KEY,
    code          VARCHAR(64) NOT NULL,
    name          VARCHAR(128) NOT NULL,
    title         VARCHAR(128) NOT NULL,
    description   TEXT NOT NULL,
    merchant_type VARCHAR(32) NOT NULL,
    portrait_code VARCHAR(64) NOT NULL,
    sort_order    INT NOT NULL,
    CONSTRAINT uq_merchant_definitions_code UNIQUE (code),
    CONSTRAINT chk_merchant_definitions_type CHECK (
        merchant_type IN ('WEAPONSMITH', 'ARMORER', 'APOTHECARY', 'GENERAL')
    ),
    CONSTRAINT chk_merchant_definitions_sort CHECK (sort_order >= 0)
);

CREATE TABLE merchant_stock (
    id                 UUID PRIMARY KEY,
    merchant_id        UUID NOT NULL,
    item_definition_id UUID NOT NULL,
    availability_type  VARCHAR(32) NOT NULL,
    sort_order         INT NOT NULL,
    CONSTRAINT fk_merchant_stock_merchant FOREIGN KEY (merchant_id)
        REFERENCES merchant_definitions (id),
    CONSTRAINT fk_merchant_stock_item FOREIGN KEY (item_definition_id)
        REFERENCES item_definitions (id),
    CONSTRAINT uq_merchant_stock_item UNIQUE (merchant_id, item_definition_id),
    CONSTRAINT chk_merchant_stock_availability CHECK (availability_type IN ('UNLIMITED')),
    CONSTRAINT chk_merchant_stock_sort CHECK (sort_order >= 0)
);

CREATE INDEX idx_merchant_stock_merchant ON merchant_stock (merchant_id, sort_order);

INSERT INTO merchant_definitions (
    id, code, name, title, description, merchant_type, portrait_code, sort_order
) VALUES
    ('d0000000-0000-4000-8000-000000000001', 'WEAPONSMITH', 'Edric Varn', 'Greyhaven Weaponsmith',
     'Edric keeps a modest rack of honest steel for travellers who cannot wait on another adventurer''s listing.',
     'WEAPONSMITH', 'edric-varn', 0),
    ('d0000000-0000-4000-8000-000000000002', 'ARMORER', 'Mara Helden', 'Greyhaven Armorer',
     'Mara sells sturdy everyday protection and a simple shield. Fancy plate is someone else''s problem.',
     'ARMORER', 'mara-helden', 1),
    ('d0000000-0000-4000-8000-000000000003', 'APOTHECARY', 'Sister Calia', 'Greyhaven Apothecary',
     'Calia restocks bitter healing draughts for those who return from the forest in one piece.',
     'APOTHECARY', 'sister-calia', 2),
    ('d0000000-0000-4000-8000-000000000004', 'GENERAL', 'Tomas Reed', 'Greyhaven Provisioner',
     'Tomas deals in modest charms and odds that keep a newcomer equipped without emptying the player market.',
     'GENERAL', 'tomas-reed', 3);

INSERT INTO merchant_stock (
    id, merchant_id, item_definition_id, availability_type, sort_order
) VALUES
    ('e0000000-0000-4000-8000-000000000001', 'd0000000-0000-4000-8000-000000000001',
     'c0000000-0000-4000-8000-000000000001', 'UNLIMITED', 0),
    ('e0000000-0000-4000-8000-000000000002', 'd0000000-0000-4000-8000-000000000001',
     'c0000000-0000-4000-8000-000000000005', 'UNLIMITED', 1),
    ('e0000000-0000-4000-8000-000000000003', 'd0000000-0000-4000-8000-000000000001',
     'c0000000-0000-4000-8000-000000000008', 'UNLIMITED', 2),
    ('e0000000-0000-4000-8000-000000000004', 'd0000000-0000-4000-8000-000000000002',
     'c0000000-0000-4000-8000-000000000002', 'UNLIMITED', 0),
    ('e0000000-0000-4000-8000-000000000005', 'd0000000-0000-4000-8000-000000000002',
     'c0000000-0000-4000-8000-00000000000b', 'UNLIMITED', 1),
    ('e0000000-0000-4000-8000-000000000006', 'd0000000-0000-4000-8000-000000000002',
     'c0000000-0000-4000-8000-00000000000c', 'UNLIMITED', 2),
    ('e0000000-0000-4000-8000-000000000007', 'd0000000-0000-4000-8000-000000000002',
     'c0000000-0000-4000-8000-00000000000e', 'UNLIMITED', 3),
    ('e0000000-0000-4000-8000-000000000008', 'd0000000-0000-4000-8000-000000000002',
     'c0000000-0000-4000-8000-00000000000f', 'UNLIMITED', 4),
    ('e0000000-0000-4000-8000-000000000009', 'd0000000-0000-4000-8000-000000000002',
     'c0000000-0000-4000-8000-000000000010', 'UNLIMITED', 5),
    ('e0000000-0000-4000-8000-00000000000a', 'd0000000-0000-4000-8000-000000000003',
     'c0000000-0000-4000-8000-000000000006', 'UNLIMITED', 0),
    ('e0000000-0000-4000-8000-00000000000b', 'd0000000-0000-4000-8000-000000000004',
     'c0000000-0000-4000-8000-000000000013', 'UNLIMITED', 0),
    ('e0000000-0000-4000-8000-00000000000c', 'd0000000-0000-4000-8000-000000000004',
     'c0000000-0000-4000-8000-000000000014', 'UNLIMITED', 1),
    ('e0000000-0000-4000-8000-00000000000d', 'd0000000-0000-4000-8000-000000000004',
     'c0000000-0000-4000-8000-000000000006', 'UNLIMITED', 2);
