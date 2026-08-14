-- Buy-order posting fee ledger, stronger crafting gold sinks, and lossy salvage recycling.

ALTER TABLE market_buy_orders
    ADD COLUMN posting_fee_paid INT NOT NULL DEFAULT 0;

ALTER TABLE market_buy_orders
    ADD CONSTRAINT chk_market_buy_orders_posting_fee CHECK (posting_fee_paid >= 0);

UPDATE crafting_recipes
SET gold_cost = gold_cost * 2;

INSERT INTO salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity)
SELECT gen_random_uuid(), d.id, 'c0000000-0000-4000-8000-00000000001c', 1
FROM item_definitions d
WHERE d.type = 'WEAPON'
  AND NOT EXISTS (
        SELECT 1
        FROM salvage_outputs s
        WHERE s.source_item_definition_id = d.id
          AND s.result_item_definition_id = 'c0000000-0000-4000-8000-00000000001c'
    );

INSERT INTO salvage_outputs (id, source_item_definition_id, result_item_definition_id, base_quantity)
SELECT gen_random_uuid(), d.id, 'c0000000-0000-4000-8000-000000000022', 1
FROM item_definitions d
WHERE d.type IN ('ARMOR', 'ACCESSORY')
  AND NOT EXISTS (
        SELECT 1
        FROM salvage_outputs s
        WHERE s.source_item_definition_id = d.id
          AND s.result_item_definition_id = 'c0000000-0000-4000-8000-000000000022'
    );
