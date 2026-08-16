# TASK 3 — Typography System

Depends on: Task 2 — [Global Design Tokens](./02-global-design-tokens.md).

## Status

Implemented. Contract: [docs/ui/DESIGN_TOKENS.md](../../ui/DESIGN_TOKENS.md) (Typography).

## Purpose

Stop the “tiny fantasy font everywhere” feel. Define readable type roles and apply them only to common / global typography.

## In scope

- Role tokens in `frontend/src/ui/tokens.css` (family, size, weight, line-height, letter-spacing, color).
- Role classes and global element mapping in `frontend/src/ui/typography.css`.
- Numeric UI uses `font-variant-numeric: tabular-nums`.
- Shared primitives: item names, stat values, badges, muted copy, brand, headings.

## Out of scope

- Hand-fixing each gameplay page in `game-shell.css`.
- Restyling auth or create-character type.
- Replacing leftover `'Cinzel'` / `--font-display` on feature screens.
