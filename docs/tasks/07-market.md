TASK 7 — Player Marketplace
Goal

Introduce the first meaningful persistent multiplayer economy.

Implement:

create listing
browse active listings
filter by item type
buy listing
cancel own listing

Market purchase must be transactional.

Prevent:

double purchase
negative gold
selling another player's item
selling equipped item
buying own listing
duplicating item stacks

Use appropriate database locking and constraints.

Frontend:

Implement marketplace screen with:

item
rarity
seller
price
buy button
filters

Implement own listings view.

Generate activity event when:

item sells
item is bought
listing is cancelled

Include concurrency integration test where two buyers attempt to purchase the same listing.

Exactly one purchase must succeed.