# TASK 11 — UI Showcase / Design System Playground

Depends on: Tasks 2–10 — tokens, typography, surfaces, layout, controls, forms, meters, rows, iconography.

## Status

Implemented. Route: `/dev/ui` (development only). Contract: [docs/ui/DESIGN_TOKENS.md](../../ui/DESIGN_TOKENS.md) (UI Showcase).

`/dev/ui` is two parts: **Part B** generic game composition (full shell, fake records desk) and **Part A** component reference. Visual QA of the engine as a whole is against `docs/mockups` and `docs/ui/VISUAL_FIDELITY_SPEC.md`.

## Purpose

See the new UI Engine on one page, separate from gameplay, before productization restyles real screens.

This page is the primary visual QA environment for the engine.

## In scope

- Dev-only route `/dev/ui` (not registered in production builds).
- One catalog page covering colors, surfaces, typography roles, shared primitives, and control states.
- Page-local layout CSS only. No feature-screen restyle.

## Out of scope

- Migrating home, combat, market, inventory, auth, or create-character onto the new look.
- Adding a second component library or theme switcher beyond the existing Office Mode toggle.
