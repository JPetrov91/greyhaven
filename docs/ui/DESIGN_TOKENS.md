# Design Tokens

Source of truth: `frontend/src/ui/tokens.css`.

This is Phase 3 UI Tasks 2–11 (depends on the [UI Engine Audit](./UI_ENGINE_AUDIT.md)). Tokens exist so later productization can share one palette, type scale, and surface finish. Color/spacing work does not restyle screens. Typography, surfaces, layout, controls, forms, meters, rows, and iconography map global elements and shared primitives only. Visual QA lives on the dev-only `/dev/ui` showcase.

Legacy names (`--ink`, `--panel`, `--space-1`…`--space-5`, `--radius`, `--radius-sm`) remain aliases with the same computed values.

## Color

| Token | Role | Value |
| --- | --- | --- |
| `--color-page-bg` | Page ground (cool charcoal) | `#07090a` |
| `--color-surface-base` | Default chrome / solid panel ground | `#161310` |
| `--color-surface-raised` | Inputs, raised plates | `#1c1714` |
| `--color-surface-inset` | Recessed wells | `#100e0c` |
| `--color-surface-interactive` | Hover / interactive wash | gold 14% |
| `--color-surface-selected` | Selected / soft gold line | gold 20% |
| `--color-surface-floating` | Menus, dialogs, toasts | `#1c1714` |
| `--color-bronze-dim` | Dark structural bronze | `#53402c` |
| `--color-bronze-normal` | Default forged metal / section labels | `#6a5340` |
| `--color-bronze-strong` | Lifted bronze | `#8a6e55` |
| `--color-gold-dim` | Structural metal (aliases bronze) | `#6a5340` |
| `--color-gold-normal` | Aged gold — numbers, selected metal | `#a48667` |
| `--color-gold-strong` | Brighter brass | `#b1967a` |
| `--color-gold-highlight` | Selected brass (rare) | `#c09962` |
| `--color-text-primary` | Body / item names (silver-parchment) | `#c8c2b8` |
| `--color-text-secondary` | Supporting copy | `#93887e` |
| `--color-text-muted` | Meta, empty hints | `#7a7168` |
| `--color-text-disabled` | Unavailable | `#5c564f` |
| `--color-text-bright` | Key numbers | `#e4e0d8` |
| `--color-positive` | Success / upgrade (forest) | `#4a7c44` |
| `--color-negative` | Danger / health (dried blood) | `#8f3c2c` |
| `--color-warning` | Caution / stamina (amber metal) | `#b07a3a` |
| `--color-info` | System / links (restrained blue) | `#5a7a8c` |

## Spacing

Scale in CSS pixels at a 16px root. Use `--spacing-*`, not the legacy `--space-N` index (legacy `--space-4` is 16px).

| Token | Size |
| --- | --- |
| `--spacing-4` | 4px |
| `--spacing-6` | 6px |
| `--spacing-8` | 8px |
| `--spacing-12` | 12px |
| `--spacing-16` | 16px |
| `--spacing-20` | 20px |
| `--spacing-24` | 24px |
| `--spacing-32` | 32px |

Office Mode remaps `--spacing-8/12/16/24` to the same compact values previously applied to `--space-2`…`--space-5`.

## Radius

Restrained RPG corners: sharp to slightly eased. Prefer `--radius-2` / `--radius-4` for new chrome. Do not change existing `--radius` (10px) or `--radius-sm` (8px) usage in this task.

| Token | Size |
| --- | --- |
| `--radius-none` | 0 |
| `--radius-2` | 2px |
| `--radius-4` | 4px |
| `--radius-8` | 8px |
| `--radius-10` | 10px |
| `--radius-pill` | 999px |

## Z-index

| Token | Value | Current use |
| --- | --- | --- |
| `--z-base` | 0 | Default flow |
| `--z-raised` | 1 | Local stacking |
| `--z-sticky` | 10 | Reserved sticky chrome |
| `--z-dropdown` | 30 | Chrome menus |
| `--z-tooltip` | 20 | Tooltips (kept at 20 so stacking does not change) |
| `--z-modal` | 40 | Toasts / modal layer |

Do not invent new z-index numbers in feature CSS. If a layer is missing, add a token first.

## Typography

Source of truth for role values: `frontend/src/ui/tokens.css`.  
Role classes and global element mapping: `frontend/src/ui/typography.css`.

This is Phase 3 UI Task 3, aligned to `docs/ui/VISUAL_FIDELITY_SPEC.md` §5. Fantasy display type is reserved for **Display**, **Page Heading**, and the small **Panel Heading** nameplate. Body, compact, metadata, micro, numeric, and section labels use `--font-ui` (Source Sans 3). Do not put Cinzel on controls, rows, or body copy.

`--font-display` aliases `--font-chrome` (Cinzel). Two families only.

| Role | Class | Family | Size | Weight | Line | Tracking | Color |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Display | `.type-display` | `--font-chrome` | 2rem | 600 | 1.10 | 0.16em | `--color-gold-normal` |
| Page Heading | `.type-page-heading` | `--font-chrome` | 1.5rem | 600 | 1.15 | 0.12em | `--color-text-bright` |
| Panel Heading | `.type-panel-heading` | `--font-chrome` | 0.85rem | 600 | 1.20 | 0.18em | `--color-gold-normal` |
| Section Heading | `.type-section-heading` | `--font-ui` | 0.5625rem | 600 | 1.25 | 0.14em | `--color-bronze-normal` |
| Item Name | `.type-item` | `--font-ui` | 0.9375rem | 600 | 1.25 | 0.01em | `--color-text-primary` |
| Body UI | `.type-body` | `--font-ui` | 0.875rem | 400 | 1.40 | 0 | `--color-text-primary` |
| Compact UI | `.type-compact` | `--font-ui` | 0.8125rem | 400 | 1.35 | 0 | `--color-text-primary` |
| Metadata | `.type-meta` | `--font-ui` | 0.6875rem | 500 | 1.30 | 0.02em | `--color-text-muted` |
| Micro Label | `.type-micro` | `--font-ui` | 0.625rem | 600 | 1.20 | 0.12em | `--color-bronze-strong` |
| Numeric UI | `.type-numeric` | `--font-ui` | 0.875rem | 600 | 1.20 | 0 | `--color-text-bright` |

Numeric UI also sets `font-variant-numeric: tabular-nums`. Currency / key values may add `.type-numeric-gold`. Functional copy uses `.type-positive` / `.type-negative` / `.type-warning`. Display, page heading, panel heading, and section heading are uppercase.

Global mapping only:

- `:root` → Body UI (family / weight / line / tracking / color; html font-size unchanged)
- `body` → Body UI size (rem units stay rooted at 16px)
- `h1`, `h2` → Page Heading
- `h3` → Section Heading
- `.brand` → Display RPG
- `.muted` → Metadata
- `.item-name` → Item / Important Name
- `.stat-value` and shared stat `dd` → Numeric UI
- badges / rarity chips → Micro Label

Feature pages in `game-shell.css` are not migrated in this task. Office Mode remaps display, page, section, item, body, compact, and numeric sizes.

## Surfaces, borders, shadows

Source of truth for values: `frontend/src/ui/tokens.css`.  
Variant classes and shared primitive mapping: `frontend/src/ui/surfaces.css`.

This is Phase 3 UI Task 4, later refined against `docs/ui/VISUAL_FIDELITY_SPEC.md`. The cheap pattern is a flat dark fill plus a uniform brown/gold stroke plus a large drop shadow. Surfaces replace that with a controlled finish:

- dark base (`--color-surface-*`)
- painted plate crops (`--surface-texture-page` / `--surface-texture-panel` / `--surface-texture-raised` / `--surface-texture-inset`)
- per-region crop via `--surface-texture-anchor` so neighboring chrome does not stamp the same plate
- panel/inset veils so the plate stays readable (`--surface-panel-veil` / `--surface-raised-veil` / `--surface-inset-veil`)
- plate lighting that binds the raster to the frame (`--surface-sheen` / `--surface-dust` / `--surface-vignette` / `--surface-luminance`)
- forged bronze edges (top/left lighter than bottom/right)
- faint top highlight (`--surface-highlight`)
- restrained inner shadow (`--surface-inner` / `--shadow-inset`)
- optional outer depth (`--shadow-raised` / `--shadow-floating` / `--shadow-artwork`)

Reusable assets live in `frontend/src/assets/ui/materials/` and `frontend/src/assets/ui/ornaments/`. Do not use heavy blur, bright gold fills, large glow, or glassmorphism (`backdrop-filter`).

### Border tokens

| Token | Color token | Role |
| --- | --- | --- |
| `--border-subtle` | `--color-border-subtle` | Default chrome edge (dim gold at 22%) |
| `--border-default` | `--color-border-default` | Raised / floating edge (dim gold at 40%) |
| `--border-interactive` | `--color-border-interactive` | Hover / interactive (normal gold at 36%) |
| `--border-selected` | `--color-border-selected` | Selected (normal gold at 48%) |

`--line` / `--line-soft` stay as legacy aliases. Do not use `--color-gold-strong` or `--color-gold-highlight` as a border.

### Shadow tokens

| Token | Role |
| --- | --- |
| `--shadow-inset` | Recessed wells |
| `--shadow-raised` | Short lift on raised plates |
| `--shadow-floating` | Menus, dialogs, toasts |
| `--shadow-artwork` | Portraits and art frames |

Legacy `--shadow` stays the previous large drop (`0 18px 40px`) so feature CSS that still references it does not shift. Office Mode sets `--shadow`, `--shadow-raised`, `--shadow-floating`, and `--shadow-artwork` to `none`. Inset highlight tokens remain so wells still read as recessed.

### Surface variants

| Variant | Class | Fill | Border | Depth |
| --- | --- | --- | --- | --- |
| Page | `.surface-page` | `--color-page-bg` + fine cool grit (`ui-material-page.webp`, 128px) | none | gutter / void |
| Base | `.surface-base` | `--color-surface-base` + faint warm grit + dusk | forged bronze | highlight + inner |
| Raised | `.surface-raised` | `--color-surface-raised` + quieter grit + dusk | brighter bronze | highlight + inner + raised |
| Inset | `.surface-inset` | `--color-surface-inset` + nearly flat grit | darker trough edge | inset |
| Interactive | `.surface-interactive` | raised + grain, no gold fill | brighter bronze | highlight + inner |
| Selected | `.surface-selected` | raised + 8% gold wash + grain | gold-bronze + left metal | highlight + inner |
| Floating | `.surface-floating` | `--color-surface-floating` + panel grain | tighter bronze | highlight + inner + floating |

`.panel` (except page) and opt-in `.surface-frame` get a painted metal rim plus L-brackets from `--asset-frame-corners` (raster, not SVG). Nav/rails that are only `.surface-base` stay unbracketed. Ornaments are never automatic on `Section`.

Shared primitive mapping only:

- `.panel` and the existing panel alias list → Base
- shared field / filter / chat / market / auth inputs → Inset
- `.tooltip-panel`, `.ui-dialog`, `.toast` → Floating
- `.portrait` uses `--shadow-artwork` in `components.css`

Feature pages in `game-shell.css` are not migrated in this task. Auth and create-character keep their own chrome.

## Layout primitives

Source of truth for classes: `frontend/src/ui/layout.css`.  
React: `Panel`, `PanelHeader`, `Section`, `SectionHeader`, `Divider`.

This is Phase 3 UI Task 5. A Panel is the framed surface. A Section inside a Panel is **not** another Panel.

Default Panel structure:

1. nameplate (`PanelHeader` / `.type-panel-heading`) when a title is present
2. content (`.panel-body`)

Default Section structure:

1. heading (`SectionHeader` / `.type-section-heading`)
2. divider (plain `.ui-divider` line)
3. content (`.ui-section-body`)

Do not nest `Panel` inside `Panel` for grouping. `.ui-section` has no fill, border, or shadow.

### Panel variants

| Variant | Classes | Surface |
| --- | --- | --- |
| Page | `.panel.surface-page` | Page |
| Base (default) | `.panel.surface-base` | Base |
| Raised | `.panel.surface-raised` | Raised |
| Inset | `.panel.surface-inset` | Inset |
| Interactive | `.panel.surface-interactive` | Interactive |
| Selected | `.panel.surface-selected` | Selected |
| Floating | `.panel.surface-floating` | Floating |

Existing callers without `variant` stay Base. Feature class names on `Panel` are unchanged.

### Divider variants

| Variant | Class | When to use |
| --- | --- | --- |
| Line (default) | `.ui-divider` | Section rules, default `Section` divider |
| Ornament diamond | `.ui-divider-ornament-diamond` | Rare page/chapter break; opt-in only |
| Ornament bar | `.ui-divider-ornament-bar` | Rare page/chapter break; opt-in only |

`Section` never applies an ornament unless `divider` is set to that variant.

## Controls

Source of truth for classes: `frontend/src/ui/controls.css`.  
React: `Button`, `IconButton`, `Tabs`.

This is Phase 3 UI Task 6, aligned to `docs/ui/VISUAL_FIDELITY_SPEC.md` §8–9. Controls use Compact UI type, `--radius-2`, and the surface/edge tokens. Do not use Cinzel, pill radius, glow, gold fill, or `--color-gold-highlight` on these primitives.

Plate tokens: `--control-plate`, `--control-plate-primary`, `--control-plate-danger`, `--control-luminance`.

### Button variants

| Variant | Classes | Finish |
| --- | --- | --- |
| Primary (default) | `.btn.btn-primary` | Warmer dark plate, selected bronze-gold rim, quiet gold label |
| Secondary | `.btn.btn-secondary` | Dark plate, bronze rim, silver label |
| Ghost | `.btn.btn-ghost` | No fill or edge; hover brightens text only |
| Danger | `.btn.btn-danger` | Red-brown metal plate and rim |

States: hover (brighter rim + plate), active (pressed inset), `:focus-visible`, disabled, `loading` (`.btn-loading`, `aria-busy`).  
`IconButton` adds `.btn-icon` and a required `label` (sets `aria-label`). Default variant is secondary. Icon buttons are etched wells; ghost icon buttons stay marks without a well.

### Tabs

| State | Treatment |
| --- | --- |
| Inactive | Secondary caps, no fill |
| Hover | Primary text, no wash |
| Selected | Gold-bronze label + connecting metal tick on the frame |
| Disabled | Disabled text, not clickable |

`kind="filters"` adds `.tabs-filters` (dark strip, still not chips). Feature skins (`.arena-tabs`, `.equipment-subtabs`, `.btn-icon-chrome`) are unchanged.

## Forms, floating, and scrollbars

Source of truth for classes: `frontend/src/ui/forms.css`.  
React: `TextInput`, `SearchInput`, `Textarea`, `Select`, `Dropdown`, `Tooltip`, `Floating`.

This is Phase 3 UI Task 7, aligned to `docs/ui/VISUAL_FIDELITY_SPEC.md` §10. Form controls use Compact UI type, inset wells, and `--radius-2`. Do not use browser-default form chrome, Cinzel, glow, uniform hover borders, or `--color-gold-highlight`.

### Form primitives

| Primitive | Classes | Notes |
| --- | --- | --- |
| Text input | `.ui-control.ui-input` | Native `<input>` well |
| Search input | `.ui-search` + `.ui-search-input` | `type="search"`, bronze mark inside the well |
| Textarea | `.ui-control.ui-textarea` | Deeper well, vertical resize only |
| Select | `.ui-control.ui-select` | Native `<select>`, bronze chevron; closed state is a field |
| Dropdown | `.ui-dropdown` + `.ui-dropdown-trigger` | Listbox in `Floating` (`--z-dropdown`) |

States: default (inset trough + directional bronze), hover (brighter directional metal), `:focus-visible` (`--focus-ring` + selected metal edges), disabled, error (red-brown edges, trough kept).

Shared field / filter / chat / market / auth inputs keep the same state language. Feature skins in `game-shell.css` (chat input, auth) still win where they set their own properties.

### Floating / Tooltip

`Floating` is the shared portaled layer: floating surface, Compact UI type, controlled width, `--shadow-floating`, placement `top` / `right` / `bottom` / `left`, and viewport flip + clamp.

| Density | Class | Width |
| --- | --- | --- |
| Default | `.tooltip-panel` | `--floating-width` (22rem), capped to viewport |
| Compact | `.tooltip-panel-compact` | `max-content`, used by `ChromeHint` |
| Inspector | `.tooltip-panel-inspector` | 16.5rem ledger plate |
| Peek | `.tooltip-panel-peek` | 11.5rem, used by equipment doll |

Inspector content uses `.tooltip-ledger` (name, rarity meta, stat rows, optional local icon light). No glass, no gold header bar.

Tooltip z-index stays `--z-tooltip` (20). Dropdown menus use `--z-dropdown` (30).

### Scrollbar

Global restrained dark-fantasy scrollbar (`scrollbar-width: thin` + WebKit thumb). Tokens: `--scrollbar-size` (6px), `--scrollbar-track` (page ground), `--scrollbar-thumb` / `--scrollbar-thumb-hover` (quiet metal, not gold). Do not use the browser-default light scrollbar.

### Focus

Keyboard focus is one tight metal ring: `--focus-ring` / `--focus-offset` on `:focus-visible` for buttons, tabs, form controls, and dropdown options. Aged brass, not lemon. Mouse `:focus` does not draw a second outline.

## Meters and badges

Source of truth for classes: `frontend/src/ui/meters.css`.  
React: `ProgressBar`, `HealthBar`, `StaminaBar`, `XPBar`, `DurabilityBar`, `Badge`, `StatusBadge`, `CounterBadge`, `RarityBadge`.

This is Phase 3 UI Task 8, aligned to `docs/ui/VISUAL_FIDELITY_SPEC.md` §12 and §17.14. Three meter species share an inset trough and `--radius-2`. Fill may use `--meter-sheen` (the only justified fill gradient). Do not use glow, animation, or `--color-gold-highlight` on these primitives.

### Progress primitives

| Primitive | Classes | Fill token | Default height |
| --- | --- | --- | --- |
| Progress (default XP) | `.progress-bar.progress-xp` | `--meter-fill-xp` | `--meter-height-default` / XP |
| Health | `.progress-bar.progress-health` | `--meter-fill-health` | vital (`HealthBar`) |
| Stamina | `.progress-bar.progress-stamina` | `--meter-fill-stamina` | vital (`StaminaBar`) |
| Durability | `.progress-bar.progress-durability` | `--meter-fill-durability` | hairline (`DurabilityBar`) |

Densities: `vital` (`.progress-vital`), default, `compact`, `hairline`.  
Optional value: `showValue` wraps the native `<progress>` in `.ui-meter`. Placement: `below` (default), `overlay` (`.ui-meter-overlay`), `beside` (`.ui-meter-beside`).  
Optional `segments` draws `.progress-segments` ticks on vitals.  
`.xp-bar` remains an alias for existing XP callers.

### Badge primitives

| Primitive | Classes | Notes |
| --- | --- | --- |
| Badge | `.badge.badge-{neutral,accent,danger,warning}` | Compact metal label, tone on text |
| Status | `.status-badge.status-{safe,danger,neutral,upgrade,downgrade,mixed}` | Same; `icon` + `meta` become `.status-badge-effect` |
| Counter | `.counter-badge.counter-{neutral,accent,danger}` | Red disc + bright numeral by default; `99+` overflow |
| Rarity | `.rarity.rarity-{common…epic}` | Tone on text, no wash |

`.safety-safe` / `.safety-danger` keep color aliases. Feature skins (overview vitals, combat meters, inventory count, equipment durability track) are unchanged.

## Rows

Source of truth for classes: `frontend/src/ui/rows.css`.  
React: `GenericRow`, `ActivityRow`, `NotificationRow`, `CompactDataRow`.

This is Phase 3 UI Task 9, aligned to `docs/ui/VISUAL_FIDELITY_SPEC.md` §13–14 and the full-screen mockups in `docs/mockups/`. A row is a **ledger line**: full width of the parent well, hairline between siblings, no per-row card chrome. Do not use pill radius, glow, Cinzel, `--color-gold-highlight`, inset plates, or tinted ticket fills on these primitives.

### Generic row

| Slot / state | Class | Type |
| --- | --- | --- |
| List well | `.ui-row-list` | Continuous stack, no sibling gap |
| Row | `.ui-row` | Transparent ledger line, hairline top on siblings |
| Icon | `.ui-row-icon` | 1.25rem painted mark, no metal well |
| Primary | `.ui-row-primary` | Compact UI |
| Secondary | `.ui-row-secondary` | Metadata |
| Metadata | `.ui-row-meta` | Metadata, nowrap (prices, time) |
| Action | `.ui-row-action` | Caller-owned control |
| Hover | `.ui-row-interactive` | `--color-surface-interactive` wash |
| Selected | `.ui-row-selected` | Soft gold wash + `--surface-metal-selected` left metal |
| Important / secondary | `.ui-row-tone-important` / `-secondary` | Bright vs muted primary |

Props: `selected`, `interactive`, `tone`. Rarity colors the **name**, not the row fill.

### Activity row

Used for the activity rail (`main.png` Recent Events) and `/dev/ui` activity feed. Variants color the **icon** (and existing `activity-hl-*` words), not a tinted plate.

| Variant | Class | Cue |
| --- | --- | --- |
| Normal (default) | `.ui-activity-row.ui-activity-normal` | Default icon color |
| System | `.ui-activity-system` | `--color-info` icon |
| Reward | `.ui-activity-reward` | `--color-gold-normal` icon |
| Warning | `.ui-activity-warning` | `--color-negative` icon |
| Market | `.ui-activity-market` | `--color-gold-normal` icon |
| PvP | `.ui-activity-pvp` | `--color-negative` icon |
| Completed | `.ui-activity-completed` | Muted primary + faded icon |

### Notification row

Used for claimable rewards, notifications, and alerts (`main.png` right rail). Same ledger as activity. `unread` brightens primary (`.ui-notification-unread`). Header counters stay `CounterBadge` red discs.

### Compact data row

Used for dense marketplace-like lists (`market.png` listings: metadata, prices, selected row) and compact logs (`combat.png` battle log: important vs secondary). Single-line body. Real Market and Combat screens are **not** migrated.

### Reference mapping

| Mockup | Region | Primitive |
| --- | --- | --- |
| `main.png` | Recent Events | `ActivityRow` |
| `main.png` | Claimable / Notifications / Alerts | `NotificationRow` |
| `main.png` | Daily objectives, world events | `GenericRow` |
| `main.png` / `market.png` | Chat / compact log | `CompactDataRow` |
| `market.png` | Listing rows, selected row, prices | `CompactDataRow` (`selected`, gold meta) |
| `combat.png` | Battle log, important vs secondary | `CompactDataRow` (`tone`) |

`/dev/main-shell` consumes these primitives. `/dev/ui` shows generic examples only: activity feed, notification stack, dense marketplace-like list, compact log.

## Iconography and ornaments

Source of truth for values: `frontend/src/ui/tokens.css`.  
Classes: `frontend/src/ui/iconography.css`.  
React: `UiIcon`, `IconWell`, `Ornament`. Constants: `frontend/src/ui/iconography.ts`.

This is Phase 3 UI Task 10, refined by the ornament / icon cohesion pass against `docs/ui/VISUAL_FIDELITY_SPEC.md` §15. UI chrome glyphs sit on a **24×24** grid with a **1.6** stroke and `currentColor`. Ornaments are **1px** metal from the asset pack. Painted `/icons/*` webp is content art — size it, do not restroke it. Do not use emoji as UI icons. Do not add a medieval ornament library. Ornament supports hierarchy; it is not content.

### Icon tokens

| Token | Role | Value |
| --- | --- | --- |
| `--icon-grid` | ViewBox for UI glyphs | `24` |
| `--icon-stroke` | Stroke on the 24 grid | `1.6` |
| `--icon-stroke-ornament` | Frame hardware stroke | `1` |
| `--icon-size-sm` | Meta / search mark | `0.75rem` (12px) |
| `--icon-size-md` | Default UI / well glyph | `1rem` (16px) |
| `--icon-size-lg` | Compact nav / chrome line | `1.25rem` (20px) |
| `--icon-size-xl` | Large nav / empty | `1.5rem` (24px) |
| `--icon-well-size` | Utility icon container | `1.75rem` (28px) |
| `--icon-well-size-lg` | Action tile well | `2.25rem` (36px) |
| `--icon-color` | Default | `--color-text-secondary` |
| `--icon-color-disabled` | Disabled | `--color-text-disabled` |
| `--icon-color-active` | Selected / pressed chrome | `--color-gold-normal` |
| `--icon-disabled-opacity` | Disabled fade | `0.55` |
| `--ornament-color` | Frame hardware | `--color-bronze-normal` |
| `--ornament-opacity` | Quiet metal | `0.62` |
| `--ornament-size-corner` | L-bracket | `1.25rem` |
| `--ornament-size-accent` | Heading pip / diamond | `0.5rem` |
| `--mark-selected` | Left selected metal | `--surface-metal-selected` |
| `--nav-idle-color` | Resting nav label | `--color-text-primary` |
| `--nav-selected-color` | Active nav label | `--color-gold-normal` |
| `--nav-selected-plate` | Pressed nav well | `--color-surface-inset` |
| `--nav-selected-fill` | Pressed trough light | dark from the top |
| `--nav-selected-rail` | Active nav taper | bright metal, fades at ends |
| `--nav-selected-shadow` | Active nav metal rail | left bar + quiet bloom |

Fill only for solid marks (dots, diamonds). Stroke + round joins for line glyphs. Hardcoded illustration fills (combat status, create-character) stay out of this contract.

### Icon primitive

| State | Class | Treatment |
| --- | --- | --- |
| Default | `.ui-icon.ui-icon-md` | Secondary color, md size |
| Small / large / xl | `.ui-icon-sm` / `-lg` / `-xl` | Size tokens only |
| Disabled | `.ui-icon-disabled` | Disabled color + opacity |
| Active | `.ui-icon-active` | Gold-normal, no glow |
| Painted art | `.ui-icon-art` | `object-fit: contain`; no stroke |
| Well | `.ui-icon-well` / `IconWell` | Inset square, thin bronze rim |
| Well large / active | `.ui-icon-well-lg` / `-active` | Action tile / selected rim |
| Selected tick | `.ui-mark-selected` | Left gold-bronze bar |
| Active nav row | `.ui-nav-selected` | Pressed inset button + gold rail |
| Selected slot | `.ui-mark-frame` | Inset gold-bronze rim |

`.btn-icon` and `.btn-icon-chrome` use `--icon-well-size`. `.chrome-icon` uses `--icon-size-lg`; nav art uses `--icon-size-xl`. Nested `.chrome-icon` inside `UiIcon` fills the icon box.

### Ornament pack

Four reusable marks, drawn from `frontend/src/assets/ui/ornaments/`. Opt-in only — never automatic on `Section` / `Panel`. `Section` / `SectionHeader` `accent` adds the heading pip when requested.

| Name | Class | Asset | Use |
| --- | --- | --- | --- |
| Divider | `.ui-ornament-divider` | `--asset-section-divider` | Rare chapter break |
| Corner | `.ui-ornament-corner` | `--asset-corner-accent` | Single L-bracket; flip with `.ui-ornament-tr/bl/br` |
| Diamond | `.ui-ornament-diamond` | `--asset-small-diamond` | Tiny center mark |
| Accent | `.ui-ornament-accent` | `--asset-tiny-pip` | Heading pip |

`.panel` and `.surface-frame` use `--asset-frame-corners` as a 9-slice so brackets stay square. Default `.ui-divider` stays the hairline fade. Task 5 divider classes (`.ui-divider-ornament-*`) stay CSS-only. New screens should prefer `Ornament` when they need a mark.

## UI Showcase

Dev-only route: `/dev/ui` (`frontend/src/pages/UiShowcasePage.tsx`).  
Registered only when `import.meta.env.DEV` is true. Production builds do not expose the route.

The page is the visual QA surface for Tasks 2–10 plus the engine-wide fidelity pass. It does not restyle gameplay screens.

**Part B — Generic Game Composition** (`GenericGameComposition`) is a full-shell synthetic desk (top bar, nav, records workspace, chat, activity). Compare it to `docs/mockups/*.png`. It is not Equipment, Market, or Combat.

**Part A — Component Reference** stays a catalogue:

| Section | What to check |
| --- | --- |
| Typography | All type roles |
| Colors | Semantic color tokens from this document |
| Surfaces | page + workspace composition (base, raised, inset, selected, floating), dividers, marks |
| Buttons | `Button`, `IconButton` |
| Tabs | Inscription tabs on a frame |
| Inputs | `TextInput`, `SearchInput`, `Select`, `Textarea`, scrollbar well |
| Progress | Progress species, vitals / XP / durability |
| Badges | Counters, status, rarity |
| Rows | Ledger primitives plus generic examples |
| Tooltips | Compact hover + inspector ledger |
| States | hover, selected, disabled, error, focus, loading |

Office Mode can be toggled on the page so density remaps are visible without entering the game shell. Compact mode keeps short contact shadows on raised/floating plates.

## Safe replacement rule

Replace a hardcoded value only when it is already the token’s exact computed color or number. Nearby golds (`#c4a45c`, `#b89a5a`, `#c5a059`) stay until a later screen task.
