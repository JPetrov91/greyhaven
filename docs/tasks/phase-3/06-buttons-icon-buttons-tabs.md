# TASK 6 — Buttons, Icon Buttons & Tabs

Depends on: Tasks 2–5 — tokens, typography, surfaces, layout primitives.

## Status

Implemented. Contract: [docs/ui/DESIGN_TOKENS.md](../../ui/DESIGN_TOKENS.md) (Controls).

## Purpose

Give shared actions and tab lists a restrained RPG finish — metal plates and a thin gold accent, not 2005 MMO bevels or pill chips.

## In scope

- Button variants: primary, secondary, danger, ghost.
- Button states: default, hover, active, focus, disabled, loading.
- `IconButton` with the same variants and states.
- Tabs states: inactive, hover, selected, disabled.
- Selected tab: selected surface wash, brighter text, 1px gold accent.

## Out of scope

- Restyling feature skins in `game-shell.css` (arena tabs, equipment subtabs, chrome icon buttons, plate CTAs).
- Routing raw `.btn` class strings on links through a `ButtonLink`.
- Migrating market / arena / equipment raw tab markup onto `Tabs`.
