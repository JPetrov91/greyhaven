-- Greyhaven world locations and connectivity (Task 3).
-- Characters start in CITY_SQUARE; movement is validated against location_connections.

CREATE TABLE locations (
    id          UUID PRIMARY KEY,
    code        VARCHAR(64) NOT NULL,
    name        VARCHAR(128) NOT NULL,
    description TEXT NOT NULL,
    safety      VARCHAR(32) NOT NULL,
    region      VARCHAR(64) NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_locations_code UNIQUE (code),
    CONSTRAINT chk_locations_safety CHECK (safety IN ('SAFE', 'DANGEROUS'))
);

CREATE TABLE location_connections (
    id                 UUID PRIMARY KEY,
    from_location_id   UUID NOT NULL,
    to_location_id     UUID NOT NULL,
    CONSTRAINT fk_location_connections_from FOREIGN KEY (from_location_id) REFERENCES locations (id),
    CONSTRAINT fk_location_connections_to FOREIGN KEY (to_location_id) REFERENCES locations (id),
    CONSTRAINT uq_location_connections_pair UNIQUE (from_location_id, to_location_id),
    CONSTRAINT chk_location_connections_not_self CHECK (from_location_id <> to_location_id)
);

CREATE INDEX idx_location_connections_from ON location_connections (from_location_id);

-- Deterministic seed IDs so application code and tests can rely on stable references by code.
INSERT INTO locations (id, code, name, description, safety, region, created_at) VALUES
    ('a0000000-0000-4000-8000-000000000001', 'CITY_SQUARE', 'City Square',
     'The bustling heart of Greyhaven. Merchants call out, travelers pass through, and every road in the region meets here.',
     'SAFE', 'Greyhaven', TIMESTAMPTZ '2026-01-01 00:00:00+00'),
    ('a0000000-0000-4000-8000-000000000002', 'TAVERN', 'Tavern',
     'A warm common room of oak and smoke. Adventurers rest here, swap rumors, and arrange expeditions into the wilds.',
     'SAFE', 'Greyhaven', TIMESTAMPTZ '2026-01-01 00:00:00+00'),
    ('a0000000-0000-4000-8000-000000000003', 'MARKET', 'Market',
     'Stalls crowd the square with goods and bargains. Players trade surplus loot and rare finds here.',
     'SAFE', 'Greyhaven', TIMESTAMPTZ '2026-01-01 00:00:00+00'),
    ('a0000000-0000-4000-8000-000000000004', 'OLD_TOWN', 'Old Town',
     'Narrow alleys and crumbling stone. Trouble lurks behind shuttered windows — encounters are common here.',
     'DANGEROUS', 'Greyhaven', TIMESTAMPTZ '2026-01-01 00:00:00+00'),
    ('a0000000-0000-4000-8000-000000000005', 'FOREST', 'Forest',
     'Dense woods press close to the road. Wolves and bandits hunt among the trees; forest patrols depart from here.',
     'DANGEROUS', 'Greyhaven', TIMESTAMPTZ '2026-01-01 00:00:00+00'),
    ('a0000000-0000-4000-8000-000000000006', 'NORTH_ROAD', 'North Road',
     'A lonely stretch of packed earth heading north from the city. Travelers are few, and danger is not.',
     'DANGEROUS', 'Greyhaven', TIMESTAMPTZ '2026-01-01 00:00:00+00');

-- Directed edges (both directions) so travel times can later differ per direction.
INSERT INTO location_connections (id, from_location_id, to_location_id) VALUES
    ('b0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000005'), -- CITY_SQUARE -> FOREST
    ('b0000000-0000-4000-8000-000000000002', 'a0000000-0000-4000-8000-000000000005', 'a0000000-0000-4000-8000-000000000001'), -- FOREST -> CITY_SQUARE
    ('b0000000-0000-4000-8000-000000000003', 'a0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000004'), -- CITY_SQUARE -> OLD_TOWN
    ('b0000000-0000-4000-8000-000000000004', 'a0000000-0000-4000-8000-000000000004', 'a0000000-0000-4000-8000-000000000001'), -- OLD_TOWN -> CITY_SQUARE
    ('b0000000-0000-4000-8000-000000000005', 'a0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000003'), -- CITY_SQUARE -> MARKET
    ('b0000000-0000-4000-8000-000000000006', 'a0000000-0000-4000-8000-000000000003', 'a0000000-0000-4000-8000-000000000001'), -- MARKET -> CITY_SQUARE
    ('b0000000-0000-4000-8000-000000000007', 'a0000000-0000-4000-8000-000000000001', 'a0000000-0000-4000-8000-000000000006'), -- CITY_SQUARE -> NORTH_ROAD
    ('b0000000-0000-4000-8000-000000000008', 'a0000000-0000-4000-8000-000000000006', 'a0000000-0000-4000-8000-000000000001'), -- NORTH_ROAD -> CITY_SQUARE
    ('b0000000-0000-4000-8000-000000000009', 'a0000000-0000-4000-8000-000000000003', 'a0000000-0000-4000-8000-000000000002'), -- MARKET -> TAVERN
    ('b0000000-0000-4000-8000-00000000000a', 'a0000000-0000-4000-8000-000000000002', 'a0000000-0000-4000-8000-000000000003'); -- TAVERN -> MARKET

-- Place any pre-existing characters in City Square before enforcing the FK.
UPDATE characters
SET current_location_id = 'a0000000-0000-4000-8000-000000000001'
WHERE current_location_id IS NULL;

ALTER TABLE characters
    ALTER COLUMN current_location_id SET NOT NULL;

ALTER TABLE characters
    ADD CONSTRAINT fk_characters_current_location
        FOREIGN KEY (current_location_id) REFERENCES locations (id);
