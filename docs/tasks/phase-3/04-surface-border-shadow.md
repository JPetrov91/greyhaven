# TASK 4 — Surface, Border & Shadow Engine

Depends on: Task 2 — [Global Design Tokens](./02-global-design-tokens.md), Task 3 — [Typography System](./03-typography-system.md).

## Status

Implemented. Contract: [docs/ui/DESIGN_TOKENS.md](../../ui/DESIGN_TOKENS.md) (Surfaces, borders, shadows).

## Purpose

Replace the cheap “black div + brown border” panel finish with a controlled surface engine.

## In scope

- Border tokens: subtle, default, interactive, selected.
- Shadow tokens: inset, raised, floating, artwork.
- Surface variants: Base, Raised, Inset, Interactive, Selected, Floating.
- Apply the engine to shared primitives only (panel aliases, shared inputs, tooltip, dialog, toast, portrait).

## Out of scope

- Restyling feature layouts in `game-shell.css`.
- Restyling auth or create-character chrome.
- Changing legacy `--shadow` / `--line` values used by feature CSS.
- Glassmorphism, heavy blur, bright gold borders, or large glow.
