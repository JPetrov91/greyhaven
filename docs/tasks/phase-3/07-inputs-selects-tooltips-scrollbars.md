# TASK 7 — Inputs, Selects, Tooltips & Scrollbars

Depends on: Tasks 2–6 — tokens, typography, surfaces, layout primitives, controls.

## Status

Implemented. Contract: [docs/ui/DESIGN_TOKENS.md](../../ui/DESIGN_TOKENS.md) (Forms, floating, and scrollbars).

## Purpose

Give shared form controls, tooltips, and scrollbars a restrained RPG finish — inset wells, one floating layer, and a dark metal scrollbar. Not browser-default chrome.

## In scope

- Form primitives: `TextInput`, `SearchInput`, `Textarea`, `Select`, `Dropdown`.
- States: default, hover, focus, disabled, error.
- `Floating` + `Tooltip`: raised/floating surface, Compact UI type, controlled width, shadow, viewport collision, placement top/right/bottom/left.
- Global dark-fantasy scrollbar.
- Unified `:focus-visible` on keyboard controls.

## Out of scope

- Rewriting auth, chat, market, or inventory screens onto the new React primitives.
- Changing chat / auth feature skins in `game-shell.css` beyond inheriting shared hover/focus where they do not override.
- Dialog / toast redesign.
