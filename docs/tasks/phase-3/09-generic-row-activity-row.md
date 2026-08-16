# TASK 9 — Generic Row & Activity Row

Depends on: Tasks 3–8 — typography, surfaces, layout, controls, forms, meters.

## Status

Implemented. Contract: [docs/ui/DESIGN_TOKENS.md](../../ui/DESIGN_TOKENS.md) (Rows). Visual source: `docs/mockups/`.

## Purpose

Give shared list rows the mockup ledger language — hairline lines in a well, not ticket cards. Activity, notifications, dense lists, and compact logs should read as a newspaper/log.

## In scope

- `GenericRow`: icon, primary, secondary, metadata, optional action, selected / hover / tone.
- `ActivityRow` variants: `normal`, `system`, `reward`, `warning`, `market`, `pvp`, `completed` (icon + word color, no tinted plate).
- `NotificationRow`: claimable / notifications / alerts, optional unread.
- `CompactDataRow`: dense marketplace-like lists and compact logs.
- Reference mapping in the token contract.
- Activity rail + `/dev/main-shell` consume the primitives.
- `/dev/ui` generic examples: activity feed, notification stack, dense marketplace-like list, compact log.

## Out of scope

- Migrating real Market or Combat screens onto these primitives.
- Migrating Inventory, Battle History, or Crafting queue.
- Restyling leftover `.activity-row` rules in `game-shell.css` beyond the rail using the new primitive.
- Glow, colored pills, or display type on rows.
