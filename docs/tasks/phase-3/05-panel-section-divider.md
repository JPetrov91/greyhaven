# TASK 5 — Panel / Section / Divider Primitives

Depends on: Task 4 — [Surface, Border & Shadow](./04-surface-border-shadow.md).

## Status

Implemented. Contract: [docs/ui/DESIGN_TOKENS.md](../../ui/DESIGN_TOKENS.md) (Layout primitives).

## Purpose

Create the main layout surfaces of the UI Engine without nested frames.

## In scope

- Reusable React + CSS: `Panel`, `Section`, `SectionHeader`, `Divider`.
- Panel variants: `base`, `raised`, `inset`, `floating` (compose Task 4 surface classes).
- Section inside a Panel uses heading + divider + content, not another panel frame.
- Two opt-in ornamental divider variants. Ornaments are never automatic.

## Out of scope

- Restyling feature layouts in `game-shell.css`.
- Migrating existing screens onto `Section` / `Divider`.
- Nested-panel “chrome rail / hero / flat inspector” skins (later productization).
