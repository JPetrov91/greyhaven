-- Player marketplace listings (Task 7) and market activity event types.

CREATE TABLE market_listings (
    id                   UUID PRIMARY KEY,
    seller_character_id  UUID NOT NULL,
    buyer_character_id   UUID,
    item_instance_id     UUID,
    item_definition_id   UUID NOT NULL,
    quantity             INT NOT NULL,
    price                INT NOT NULL,
    status               VARCHAR(32) NOT NULL,
    created_at           TIMESTAMPTZ NOT NULL,
    sold_at              TIMESTAMPTZ,
    cancelled_at         TIMESTAMPTZ,
    version              BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_market_listings_seller
        FOREIGN KEY (seller_character_id) REFERENCES characters (id),
    CONSTRAINT fk_market_listings_buyer
        FOREIGN KEY (buyer_character_id) REFERENCES characters (id),
    CONSTRAINT fk_market_listings_item_instance
        FOREIGN KEY (item_instance_id) REFERENCES item_instances (id) ON DELETE SET NULL,
    CONSTRAINT fk_market_listings_item_definition
        FOREIGN KEY (item_definition_id) REFERENCES item_definitions (id),
    CONSTRAINT chk_market_listings_quantity CHECK (quantity >= 1),
    CONSTRAINT chk_market_listings_price CHECK (price >= 1),
    CONSTRAINT chk_market_listings_status CHECK (status IN ('ACTIVE', 'SOLD', 'CANCELLED')),
    CONSTRAINT chk_market_listings_active_item CHECK (
        (status = 'ACTIVE' AND item_instance_id IS NOT NULL)
        OR (status <> 'ACTIVE')
    ),
    CONSTRAINT chk_market_listings_sold CHECK (
        (status = 'SOLD' AND buyer_character_id IS NOT NULL AND sold_at IS NOT NULL AND cancelled_at IS NULL)
        OR (status <> 'SOLD' AND buyer_character_id IS NULL AND sold_at IS NULL)
    ),
    CONSTRAINT chk_market_listings_cancelled CHECK (
        (status = 'CANCELLED' AND cancelled_at IS NOT NULL AND buyer_character_id IS NULL AND sold_at IS NULL)
        OR (status <> 'CANCELLED' AND cancelled_at IS NULL)
    ),
    CONSTRAINT chk_market_listings_not_self_buy CHECK (
        buyer_character_id IS NULL OR buyer_character_id <> seller_character_id
    )
);

CREATE UNIQUE INDEX uq_market_listings_active_item
    ON market_listings (item_instance_id)
    WHERE status = 'ACTIVE';

CREATE INDEX idx_market_listings_status_created
    ON market_listings (status, created_at DESC);

CREATE INDEX idx_market_listings_seller_status
    ON market_listings (seller_character_id, status);

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
        'MARKET_CANCELLED'
    )
);
