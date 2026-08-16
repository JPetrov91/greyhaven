# TASK 8 — Progress Bars, Badges & Status Primitives

Depends on: Tasks 2–7 — tokens, typography, surfaces, layout primitives, controls, forms.

## Status

Implemented. Contract: [docs/ui/DESIGN_TOKENS.md](../../ui/DESIGN_TOKENS.md) (Meters and badges).

## Purpose

Give shared meters and status marks the mockup language from combat (vitals, status hierarchy, disabled/action feedback) and equipment (durability, rarity, tooltip density). Not flat pills, pastel chips, or animated fills.

## In scope

- Progress primitives: `ProgressBar`, `HealthBar`, `StaminaBar`, `XPBar`, `DurabilityBar`.
- Badge primitives: `Badge`, `StatusBadge`, `CounterBadge` (plus existing `RarityBadge` geometry).
- Progress finish: inset trough, directional edge, desaturated fill + sheen, vital segments, overlay/beside values.
- Badge finish: compact metal labels; counters as red discs; status effects as icon + name + duration.
- Tooltip inspector density, global scrollbar, and tight metal `:focus-visible`.
- Realistic `/dev/ui` samples only — not Combat or Equipment screens.

## Out of scope

- Restyling feature skins in `game-shell.css` (overview vitals, combat meters, equipment durability track, inventory count chip).
- Migrating screens onto `HealthBar` / `XPBar` / `CounterBadge`.
- Building Combat or Equipment.
- Glow, value animation, or lemon-gold chrome.
