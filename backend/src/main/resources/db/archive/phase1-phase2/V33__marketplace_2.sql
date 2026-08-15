-- Marketplace 2.0: listing snapshots, fees, buy orders.

ALTER TABLE market_listings
    ADD COLUMN listing_fee_paid INT NOT NULL DEFAULT 0,
    ADD COLUMN sale_fee_paid INT,
    ADD COLUMN instance_rarity VARCHAR(32),
    ADD COLUMN item_type VARCHAR(32),
    ADD COLUMN weapon_family VARCHAR(32),
    ADD COLUMN required_level INT;

UPDATE market_listings AS l
SET instance_rarity = COALESCE(src.instance_rarity, src.definition_rarity),
    item_type = src.item_type,
    weapon_family = src.weapon_family,
    required_level = src.required_level
FROM (
    SELECT
        ml.id,
        i.rarity AS instance_rarity,
        d.rarity AS definition_rarity,
        d.type AS item_type,
        d.weapon_family,
        d.required_level
    FROM market_listings ml
    JOIN item_definitions d ON d.id = ml.item_definition_id
    LEFT JOIN item_instances i ON i.id = ml.item_instance_id
) src
WHERE l.id = src.id;

ALTER TABLE market_listings
    ALTER COLUMN instance_rarity SET NOT NULL,
    ALTER COLUMN item_type SET NOT NULL,
    ALTER COLUMN required_level SET NOT NULL;

ALTER TABLE market_listings
    ADD CONSTRAINT chk_market_listings_listing_fee CHECK (listing_fee_paid >= 0),
    ADD CONSTRAINT chk_market_listings_sale_fee CHECK (sale_fee_paid IS NULL OR sale_fee_paid >= 0),
    ADD CONSTRAINT chk_market_listings_instance_rarity CHECK (
        instance_rarity IN ('COMMON', 'UNCOMMON', 'RARE', 'EPIC')
    ),
    ADD CONSTRAINT chk_market_listings_item_type CHECK (
        item_type IN ('WEAPON', 'ARMOR', 'CONSUMABLE', 'MATERIAL', 'ACCESSORY')
    );

CREATE INDEX idx_market_listings_browse
    ON market_listings (status, created_at DESC);
CREATE INDEX idx_market_listings_filters
    ON market_listings (status, item_type, instance_rarity, weapon_family, required_level, price);

CREATE TABLE market_buy_orders (
    id                      UUID PRIMARY KEY,
    buyer_character_id      UUID NOT NULL,
    item_definition_id      UUID NOT NULL,
    remaining_quantity      INT NOT NULL,
    original_quantity       INT NOT NULL,
    max_unit_price          INT NOT NULL,
    reserved_gold           INT NOT NULL,
    status                  VARCHAR(32) NOT NULL,
    version                 BIGINT NOT NULL,
    created_at              TIMESTAMPTZ NOT NULL,
    updated_at              TIMESTAMPTZ NOT NULL,
    filled_at               TIMESTAMPTZ,
    cancelled_at            TIMESTAMPTZ,
    CONSTRAINT fk_market_buy_orders_buyer FOREIGN KEY (buyer_character_id) REFERENCES characters (id),
    CONSTRAINT fk_market_buy_orders_item FOREIGN KEY (item_definition_id) REFERENCES item_definitions (id),
    CONSTRAINT chk_market_buy_orders_remaining CHECK (remaining_quantity >= 0),
    CONSTRAINT chk_market_buy_orders_original CHECK (original_quantity >= 1),
    CONSTRAINT chk_market_buy_orders_price CHECK (max_unit_price >= 1),
    CONSTRAINT chk_market_buy_orders_reserved CHECK (reserved_gold >= 0),
    CONSTRAINT chk_market_buy_orders_status CHECK (status IN ('ACTIVE', 'FILLED', 'CANCELLED'))
);

CREATE INDEX idx_market_buy_orders_active_item
    ON market_buy_orders (status, item_definition_id, created_at DESC);
CREATE INDEX idx_market_buy_orders_buyer
    ON market_buy_orders (buyer_character_id, created_at DESC);

CREATE TABLE market_buy_order_fills (
    id                  UUID PRIMARY KEY,
    buy_order_id        UUID NOT NULL,
    seller_character_id UUID NOT NULL,
    item_instance_id    UUID NOT NULL,
    quantity            INT NOT NULL,
    gross_gold          INT NOT NULL,
    sale_fee            INT NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_market_buy_order_fills_order FOREIGN KEY (buy_order_id) REFERENCES market_buy_orders (id),
    CONSTRAINT fk_market_buy_order_fills_seller FOREIGN KEY (seller_character_id) REFERENCES characters (id),
    CONSTRAINT chk_market_buy_order_fills_qty CHECK (quantity >= 1),
    CONSTRAINT chk_market_buy_order_fills_gold CHECK (gross_gold >= 1),
    CONSTRAINT chk_market_buy_order_fills_fee CHECK (sale_fee >= 0)
);

CREATE INDEX idx_market_buy_order_fills_order ON market_buy_order_fills (buy_order_id, created_at DESC);
