-- One item instance may have several ACTIVE listings of disjoint quantities (Task 7).
-- Over-listing is prevented by reserved-quantity checks under the seller row lock.
DROP INDEX IF EXISTS uq_market_listings_active_item;
