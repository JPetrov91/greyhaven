# TASK 10 — Iconography & Ornament Foundation

Depends on: Tasks 2–9 — tokens, typography, surfaces, layout, controls, forms, meters, rows.

## Status

Implemented, then refined by the ornament / icon cohesion pass (icon wells, selected marks, asset-pack ornaments, 9-slice frames). Contract: [docs/ui/DESIGN_TOKENS.md](../../ui/DESIGN_TOKENS.md) (Iconography and ornaments).

## Purpose

Lock UI chrome glyphs to one grid and stroke, and ship a handful of reusable SVG ornaments. Stop the mix of emoji, ad-hoc strokes, and one-off decorative marks from becoming the default.

## Icon audit

Observed mix (not migrated in this task):

| Family | Where | Notes |
| --- | --- | --- |
| Emoji / symbols | `EquipmentPanel` resistances (`🔥 ❄ ⚡ ☠ ◈ ✦`) and locked effects (`⚔ 🛡`) | Only emoji used as icons |
| Painted webp packs | `/icons/nav`, `/icons/activity`, `/icons/actions`, `/icons/chat`, `/icons/env`, `/icons/vitals` | Content art; different illustration density |
| Hand SVG, 24 grid, stroke 1.6 | `chromeIcons.tsx`, most `locationMedia.tsx` fallbacks | Closest to the new contract |
| Hand SVG, 24 grid, stroke 1.7 | Auth login/register, several market/inventory glyphs | Near-miss |
| Hand SVG, mixed 1.4–1.8 | `equipmentIcons.tsx`, `itemIcons.tsx`, `locationMedia.tsx` | Same pack, uneven stroke |
| Hand SVG, 32 grid, stroke 2.2 + hardcoded fills | `combatStatusIcons.tsx` | Status illustration, not chrome |
| CSS-drawn marks | Search magnifier, select chevron data-URI | Not SVG glyphs |
| Inconsistent display size | `.chrome-icon` 1.1rem, nav art 1.5rem, `.btn-icon` 1.75rem hit, row inner 1.25rem, search 0.7rem | No shared size scale |
| No lucide / heroicons / FA | — | Do not introduce a third-party icon pack |

## In scope

- Icon tokens: grid, sizes, stroke, default / disabled / active color.
- `UiIcon` size + state wrapper.
- Four SVG ornaments: section divider, small corner, tiny diamond, section accent.
- `Ornament` primitive. Ornaments stay opt-in.

## Out of scope

- Replacing Equipment emoji, combat status art, auth icons, or painted nav/activity webp.
- Restyling `.chrome-icon` / `.btn-icon-chrome` sizes in `game-shell.css`.
- A large medieval ornament library.
- Auto-applying ornaments on `Section` / `Panel`.
