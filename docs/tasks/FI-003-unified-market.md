FI-003 — Unified Market Hub & NPC Merchants

Phase: 2
Type: Fast Improvement / Intermediate Task
Priority: REQUIRED

1. Purpose

Expand the existing Phase 1 player marketplace into a unified in-world Market Hub containing two clearly separated economic systems:

NPC Merchants
Player Market

The player should perceive both systems as parts of the same Greyhaven marketplace rather than unrelated application screens.

The goal is to create a reliable baseline equipment economy before the larger Phase 2 PvE expansion.

The system should support the following progression loop:

Combat
↓
Gold / Loot
↓
Greyhaven Market
↓
Buy guaranteed basic equipment
OR
buy stronger / more specialized item from another player
↓
Improve character
↓
Enter harder content
2. Product Motivation

The existing Player Market depends on other players supplying useful items.

This creates several problems:

new players may not find basic equipment;
low-population environments may have an empty market;
progression can become too dependent on random loot;
PvE balancing becomes harder because access to baseline equipment is unpredictable.

NPC Merchants solve this by providing a guaranteed baseline.

However, NPC Merchants must NOT replace or invalidate the player-driven economy.

The intended relationship is:

NPC Merchants
=
reliable
basic
immediate
less efficient

Player Market
=
dynamic
varied
potentially stronger
potentially better value
player-driven
3. Core Design

There is one top-level destination:

GREYHAVEN MARKET

Inside it:

MARKET
│
├── Merchants
│
├── Player Market
│
└── My Listings

Task 9 may later extend it with:

Buy Orders
advanced filters
crafting-related economy
market history

Do NOT implement those Task 9 systems as part of FI-003.

4. UX Structure

The Market should use one shared screen shell.

Recommended navigation:

[ MERCHANTS ] [ PLAYER MARKET ] [ MY LISTINGS ]

The selected mode changes the main content area.

The player should not need to leave the Market screen to switch between NPC and player trading.

5. Merchants

Implement several basic NPC merchant categories.

Initial merchants:

Weaponsmith
Armorer
Apothecary
General Merchant

Exact names may follow Greyhaven lore.

Example:

Weaponsmith
→ basic swords
→ axes
→ maces
→ daggers
→ bows

Armorer
→ basic armor
→ basic shields

Apothecary
→ healing potions
→ basic consumables

General Merchant
→ selected basic utility items

Do not create dozens of merchants.

The purpose is functional coverage, not content volume.

6. Merchant Identity

Merchants should be represented as game-world NPCs rather than anonymous shop categories where practical.

A Merchant definition may include:

id
code
name
title
description
merchantType
portrait/image reference

Example:

Edric Varn
Greyhaven Weaponsmith

The architecture should allow future location-specific merchants without implementing a large regional merchant system now.

7. Merchant Inventory

Merchant inventory should be definition-driven.

Do NOT hardcode the entire shop inventory inside frontend components or controller logic.

A merchant stock entry should conceptually contain:

merchantId
itemDefinitionId
price
availabilityType

Initial implementation may use permanent unlimited stock.

8. Core Stock

Core Merchant inventory contains guaranteed baseline items.

Examples:

Common Sword
Common Axe
Common Mace
Common Dagger
Common Bow

Basic Light Armor
Basic Medium Armor
Basic Heavy Armor

Basic Shield

Minor Healing Potion

The specific existing Phase 2 ItemDefinitions should be reused where appropriate.

Do not create duplicate item systems for merchant equipment.

9. Merchant Equipment Quality

NPC merchants primarily sell:

COMMON

equipment.

A small number of intentionally defined:

UNCOMMON

items may be allowed later, but they should not be required for FI-003.

NPC merchants must NOT routinely sell:

RARE
EPIC

randomized equipment.

The Player Market and gameplay loot should remain the primary source of higher-value itemization.

10. Random Affixes

Core Merchant equipment should generally use deterministic baseline configurations.

Do not turn the normal NPC shop into a randomized loot generator.

Example:

Iron Longsword

COMMON

Damage 12–16

not:

refresh shop repeatedly until perfect affixes appear

This protects the purpose of Player Market and PvE loot.

11. Merchant Purchase

Player can buy an item from an NPC Merchant.

Flow:

Select Merchant
↓
Select Item
↓
Inspect Item
↓
Buy
↓
Gold deducted
↓
Item created / transferred to Inventory

Backend validates:

merchant exists
item is sold by this merchant
character has sufficient gold
inventory has capacity
price is server-authoritative

Frontend must never submit an authoritative purchase price.

The client identifies:

merchant
item
quantity

and the server determines the real price.

12. Merchant Purchase Transaction

Buying from an NPC must be transactional.

Conceptually:

validate purchase
↓
deduct gold
↓
create ItemInstance
↓
add item to character inventory
↓
create Activity entry if appropriate
↓
commit

Failure at any stage must rollback the complete transaction.

Repeated or duplicated purchase requests should not accidentally create uncontrolled duplication where request-idempotency infrastructure already exists.

13. Selling Items to Merchants

NPC Merchants must provide an immediate item disposal option.

The player may sell eligible items directly to the system.

This is intentionally different from Player Market listing.

Player Market:

potentially higher value
requires another player
takes time
listing mechanics

Merchant:

instant
guaranteed
lower value
item leaves economy
14. Merchant Buy Price

Every sellable ItemInstance should have a server-calculated Merchant value.

Do not use a price supplied by the frontend.

Conceptually:

Merchant Buy Price
=
Base Item Value
× Rarity Modifier
× Affix / Stat Value Modifier
× Merchant Buy Multiplier

Exact Phase 2 formula should remain deliberately simple.

Do not attempt to build a sophisticated item valuation AI.

15. Base Value

ItemDefinition should expose or reuse a concept equivalent to:

baseValue

Base value represents approximate system value before market dynamics.

It may be used by:

merchant pricing
future crafting economics
salvage balancing
loot balancing

If Phase 1 already contains baseValue, reuse it.

Do not create a duplicate concept.

16. Merchant Price Spread

The economy should generally satisfy:

Merchant Buy Price
<
Base Value
<
Merchant Sell Price

Example:

Iron Sword

Base Value:       100
Merchant Buys:     55
Merchant Sells:   130

Exact multipliers must be configurable.

Recommended initial balance:

Merchant Buy Multiplier:
0.50–0.60

Merchant Sell Multiplier:
1.20–1.40

Do not scatter these values through services.

Use:

MarketBalance
MerchantBalance

or equivalent existing balance infrastructure.

17. Item Sink

Selling equipment to an NPC should normally remove that ItemInstance from the player economy.

Flow:

Player Item
↓
Merchant Sell
↓
Gold awarded
↓
ItemInstance removed / marked consumed

The Merchant does NOT need to resell that exact ItemInstance.

This is an intentional economic item sink.

18. Sell Restrictions

Player must not be able to sell:

an item they do not own
an equipped item
an item currently listed on Player Market
an item reserved by another system

Future locked/soulbound concepts are out of scope unless they already exist.

19. Stackable Items

If existing inventory supports stackable items:

merchant purchase and sale must support quantities safely.

Example:

Healing Potion ×5

Server must validate quantity.

Prevent:

negative quantity
zero quantity
selling more than owned
purchasing invalid quantities
20. Quick Sell from Inventory

Improve Inventory UX by allowing direct access to Merchant selling.

When inspecting an eligible item, provide something equivalent to:

Sell

The player should be able to compare:

Merchant Offer

with the option to:

List on Player Market

Example UX:

IRON SWORD

Merchant offer:
72 Gold

[Sell Now]

[Create Market Listing]

Do not require the player to manually navigate through several screens merely to sell junk equipment.

21. No Estimated Player Price Yet

Do NOT implement a sophisticated:

Estimated Player Market Value: 140–180g

unless reliable market-history infrastructure already exists.

A misleading estimate is worse than no estimate.

This may be added during Economy 2.0 when sufficient data exists.

22. Player Market

Preserve the existing Phase 1 Player Market functionality.

It should now appear inside the unified Market Hub.

Existing behavior such as:

browse listings
create listing
buy listing
cancel own listing

must remain functional.

Do not rewrite the Player Market unnecessarily.

23. My Listings

Provide a clear Market mode for the player's own listings.

At minimum:

Active Listings

Existing sold/cancelled state may be shown if already supported.

Do not implement full historical analytics in FI-003.

24. Visual Distinction

NPC Merchants and Player Market must be clearly different modes inside one coherent Market screen.

Example:

Merchants

May emphasize:

merchant portrait
merchant name
fixed inventory
fixed prices
Player Market

May emphasize:

search
filters
seller
rarity
dynamic price

Do not merge NPC goods directly into the same Player Market results table without a clear distinction.

That would make the economy harder to understand.

25. Proposed Market Screen

Conceptual layout:

┌────────────────────────────────────────────────────────────┐
│ GREYHAVEN MARKET                                          │
│ Trade District                                            │
├────────────────────────────────────────────────────────────┤
│ [ Merchants ] [ Player Market ] [ My Listings ]           │
├────────────────────────────────────────────────────────────┤
│                                                            │
│  Merchant List       Selected Merchant                    │
│                                                            │
│  Weaponsmith         Edric Varn                           │
│  Armorer             Greyhaven Weaponsmith                │
│  Apothecary                                              │
│  General Goods       Iron Sword                 130g      │
│                      Iron Axe                   150g      │
│                      Wooden Shield              100g      │
│                                                            │
└────────────────────────────────────────────────────────────┘

This is an information-architecture example, not a mandatory pixel layout.

Follow the project's approved design system.

26. Activity Feed

Appropriate events may include:

You bought Iron Sword for 130 Gold.
You sold Worn Leather Armor for 42 Gold.

Avoid spamming Activity Feed for every trivial consumable purchase if the existing activity system is intended only for meaningful events.

Follow existing product conventions.

27. API

Use existing:

/api/v1

Potential endpoints:

GET  /api/v1/market/merchants
GET  /api/v1/market/merchants/{merchantId}

POST /api/v1/market/merchants/{merchantId}/purchases

POST /api/v1/market/merchant-sales

Exact API shape should follow existing code conventions.

Do not create:

/api/v2

just for this improvement.

Do not expose persistence entities directly.

28. Suggested Backend Components

The exact architecture should follow the repository.

Potential components:

MerchantDefinition
MerchantStockDefinition

MerchantService
MerchantPurchaseService
MerchantSellService

MerchantPriceCalculator
MerchantBalance

Avoid putting price formulas directly in controllers.

29. Data Storage

Merchant static definitions may live in the same game-definition mechanism currently used for:

items
monsters
locations
techniques

Choose the existing project convention.

Do not introduce a new database/configuration technology solely for merchants.

30. Merchant Stock Persistence

For the initial FI-003 implementation:

unlimited deterministic core stock

is sufficient.

A database table tracking physical stock counts is NOT required unless existing architecture makes it trivial.

Do not implement:

merchant restock jobs
hourly random rotation
limited inventory
dynamic NPC supply

as part of FI-003.

31. Rotating Merchant Stock

Future extension only.

The architecture should not make rotating stock impossible, but FI-003 must not introduce FOMO-oriented rotating shops.

Do not implement:

Shop refresh in 2h
Buy before it disappears!

at this stage.

32. Marketplace Compatibility

Existing Player Market listings must remain valid after FI-003.

Merchant functionality must not alter existing listings.

NPC merchants should not automatically interact with Player Market listings.

They are separate economic mechanisms.

33. Interaction With Itemization 2.0

Merchant equipment must use the same:

ItemDefinition
ItemInstance
Equipment Requirements
Weapon Family
Armor Category
Rarity

system introduced earlier in Phase 2.

Do NOT create simplified "ShopItem" equipment objects disconnected from normal items.

After purchase, a merchant item must behave exactly like another normal owned item.

34. Interaction With Combat 2.0

Merchant baseline equipment should provide enough progression support for PvE balancing.

FI-003 should make it possible for a player who experiences poor loot RNG to still obtain reasonable baseline equipment using earned Gold.

Do not attempt to balance all PvE inside this FI.

Task 7 remains responsible for PvE expansion.

35. Interaction With Task 9

Task 9 — Crafting, Professions and Economy 2.0 — will later extend this Market Hub.

FI-003 must NOT prematurely implement:

Buy Orders
Profession vendors
Crafting economy
Advanced resources
Market price history
Advanced fees
Regional economy

Task 9 should build on FI-003 rather than replace it.

36. Server Authority

The server must determine:

merchant inventory
merchant prices
purchase validity
sell validity
sale price
item creation
gold changes

Frontend sends only player intent.

Example:

{
  "merchantId": "...",
  "itemDefinitionId": "...",
  "quantity": 1
}

Do NOT accept:

{
  "price": 50
}

as authoritative purchase pricing.

37. Transaction Safety

Critical operations:

Merchant Purchase
Merchant Sale

must be transactional.

Prevent:

negative gold
duplicate equipment
selling same item twice
buying without sufficient gold
inventory overflow
partial transaction completion
38. Testing Requirements
Merchant Purchase

Test:

valid purchase
insufficient gold
invalid merchant
item not sold by merchant
inventory full
quantity purchase
gold deducted correctly
ItemInstance created exactly once
Merchant Sale

Test:

valid sale
wrong owner
equipped item
market-listed item
invalid quantity
stack sale
gold awarded correctly
item removed correctly
duplicate sale attempt
Pricing

Test:

base value
buy multiplier
sell multiplier
rarity effect if applicable
price rounding
configured values

All pricing tests must be deterministic.

Integration

Use PostgreSQL Testcontainers for important transaction flows.

At minimum:

buy merchant item transaction

sell owned item transaction

failed purchase rollback

failed sale rollback

Player Market regression
39. UX Acceptance Criteria

The player should be able to:

Open Market
↓
Select Merchants
↓
Open Weaponsmith
↓
Inspect Sword
↓
Buy Sword
↓
See it immediately in Inventory

And:

Open Inventory
↓
Inspect unwanted item
↓
See Merchant Sell option
↓
Sell immediately
↓
Receive Gold

Switching to:

Player Market

must remain fast and obvious.

40. Technical Acceptance Criteria

FI-003 is complete when:

one unified Market Hub exists;
Merchants and Player Market are separate modes inside it;
at least basic Weaponsmith functionality exists;
basic armor merchant functionality exists;
basic consumable merchant functionality exists;
merchant inventory is server-defined;
merchant prices are server-authoritative;
NPC purchase works transactionally;
NPC selling works transactionally;
items sold to NPC leave the player economy;
equipped items cannot be sold;
listed items cannot be sold;
merchant items use normal ItemInstance infrastructure;
existing Player Market still works;
Inventory provides practical Quick Sell UX;
pricing is configurable;
appropriate automated tests exist;
no Task 9 economy systems were prematurely implemented.