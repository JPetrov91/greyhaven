# TASK 2 — Global Design Tokens

Depends on: Task 1 — [UI Engine Audit](../../ui/UI_ENGINE_AUDIT.md).

## Status

Implemented. Contract: [docs/ui/DESIGN_TOKENS.md](../../ui/DESIGN_TOKENS.md).

## Purpose

Create a single visual-system foundation in `frontend/src/ui/tokens.css` without migrating every screen to a new look.

## In scope

- Semantic color, spacing, radius, and z-index tokens listed in the design-token doc.
- Legacy token aliases so existing CSS keeps the same computed values.
- Exact-match hardcoded replacements only (same hex / same z-index).

## Out of scope

- Restyling auth, create-character, or in-game feature pages.
- Aligning nearby golds, brass, or chat browns to the new names.
- Raising tooltip z-index above dropdowns.
- New component library, Tailwind, or CSS-in-JS.
