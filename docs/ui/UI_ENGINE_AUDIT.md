# UI Engine Audit

Date: 2026-08-16  
Scope: existing frontend visual architecture only. No production UI was changed.

This document is the baseline for later Phase 3 work:

- Task 12 — Release-Quality UI Design System (token contract: [DESIGN_TOKENS.md](./DESIGN_TOKENS.md))
- Task 13 — Main Shell & Office Mode
- Task 14 — Existing Feature Productization

It does **not** prescribe a new visual language. Phase 3 already adopted the Veridia direction in `docs/PHASE_3_SPEC.md` §9. The current codebase is a mix of an early token/primitive layer and later Veridia chrome that was added screen-by-screen.

---

## Existing architecture

### Stack

The frontend is a Vite + React SPA. Global CSS is imported once from `frontend/src/main.tsx` via `frontend/src/index.css`. There is no CSS-in-JS, no Tailwind, and no component-scoped CSS modules except two page files.

Style entry graph:

```
index.css
  ├── ui/tokens.css
  ├── ui/typography.css
  ├── ui/components.css
  ├── ui/game-shell.css
  ├── pages/authLanding.css
  └── pages/createCharacter.css
```

React primitives live in `frontend/src/ui/`. Feature screens live in `frontend/src/components/` and `frontend/src/pages/`. Screens compose class names; they do not own a second stylesheet except auth and create-character.

### CSS layers in practice

| File | Role today | Approx. size | Intended role |
| --- | --- | --- | --- |
| `ui/tokens.css` | `:root` design tokens + compact-mode token overrides | ~60 lines | Theme source of truth |
| `ui/components.css` | Shared primitives, then equipment doll / item / dialog / toast | ~1,140 lines | Reusable primitives only |
| `ui/game-shell.css` | Game chrome **and** most feature layouts (home, combat, market, inventory, equipment, arena, chat, activity, quests) | ~4,170 lines | Shell / chrome only |
| `index.css` | Reset, leftover pre-shell layout, leftover feature styles, compact-mode overrides | ~650 lines | Reset + app bootstrap only |
| `pages/authLanding.css` | Login / register Veridia landing | ~870 lines | Auth page (keep isolated) |
| `pages/createCharacter.css` | Character creation Veridia screen | ~990 lines | Create-character page (keep isolated) |

There is no dedicated theme file beyond `tokens.css`. Compact / Office Mode is not a second theme file; it is `html[data-ui-mode='compact']` overrides split across `tokens.css`, `index.css`, and `game-shell.css`.

### Runtime theming

- `frontend/src/ui/uiMode.ts` persists `greyhaven.uiMode` (`normal` | `compact`) and sets `document.documentElement.dataset.uiMode`.
- Product copy calls this **Office Mode**. The attribute and most CSS still say `compact`.
- Compact mode remaps a subset of tokens (`--panel`, `--shadow`, `--radius`, some `--space-*` and `--text-*`) and then applies a long list of selector-specific overrides that hide art and tighten padding.

There is no light theme, no per-user color theme, and no CSS custom-property bridge for rarity/status beyond the token set.

### Application chrome vs content

`AppShell` has three visual paths:

1. **Auth / create-character** — header hidden; page CSS owns the full viewport.
2. **Authenticated game** (`/game` with an active character) — header hidden; `GameLayout` renders `.game-shell` (top bar, left nav, main, activity rail).
3. **Fallback header** (`.app-header` / `.app-nav`) — still used when authenticated but not on the game chrome path (for example no active character). This is the older pre-shell header.

`GameLayout` is a view switcher (`frontend/src/ui/gameNav.ts`), not a router of pages. Feature panels mount inside `.game-shell-main`. Combat replaces the three-column body with `.game-shell-body-combat`.

### Fonts

Loaded from Google Fonts in `frontend/index.html`:

- **Cinzel** 400–700 — chrome titles, nav labels, plate CTAs, location hero, equipment, arena. Used as a raw `'Cinzel'` family in CSS, **not** a token.
- **Cormorant Garamond** 400–700 — `--font-display`. Headings, chat text, some meters.
- **Source Sans 3** 400–600 — `--font-body`. Default UI copy.

`:root` in `index.css` sets `font-family: var(--font-body)`. Display headings (`h1`, `h2`, `.brand`) use `--font-display`.

Result: three live typefaces, but tokens only name two. Chrome has drifted to Cinzel while primitives still assume Cormorant for “display”.

### Color system

Canonical tokens in `tokens.css`:

| Token | Value | Use |
| --- | --- | --- |
| `--ink` | `#e8e0d4` | Primary text |
| `--ink-soft` | `#b3a894` | Secondary text |
| `--paper` | `#12100e` | Page ground |
| `--panel` | `rgba(22, 18, 16, 0.94)` | Panel fill (solid `#161310` in compact) |
| `--panel-solid` | `#1c1714` | Inputs, dialogs, toasts |
| `--line` / `--line-soft` | `#8a6d32` / gold 22% | Borders |
| `--accent` / `--accent-strong` / `--accent-soft` | `#c9a227` / `#e4c56a` / gold 16% | Primary gold |
| `--danger` / `--danger-soft` | `#c45c4a` / 18% | Errors, unsafe |
| `--upgrade` / `--downgrade` / `--mixed` | green / red / gold | Item comparison |
| `--health` / `--stamina` | `#b4453a` / `#d4b05a` | Vital bars |
| `--rarity-*` | common / uncommon / rare / epic + backgrounds | Item rarity |
| `--shadow` | `0 18px 40px rgba(0,0,0,0.45)` | Panels, tooltips |
| `--radius` / `--radius-sm` | `10px` / `8px` (`4px` in compact) | Default rounding |
| `--space-1` … `--space-5` | `0.25rem` … `1.5rem` | Sparse usage |
| `--text-xs` … `--text-xl` | type scale | Partial usage |
| `--motion` | `150ms ease` | Rarely used |
| `--focus-ring` / `--focus-offset` | accent outline | Global `:focus-visible` |

Auth and create-character **re-declare** a parallel gold/ink set (`--auth-gold`, `--cc-gold`, etc.) instead of using `--accent` / `--ink`.

`body` in `index.css` does not use `--paper` as a flat fill. It paints hardcoded radial/linear browns (`#161310`, `#12100e`, `#1a1510`). Compact mode flattens this to `--paper`.

### Shadows, borders, spacing

**Shadows**

- Token: `--shadow` (large drop).
- Many chrome surfaces ignore it and use one-off stacks: inset gold hairlines, `0 10px 22px`, combat HUD `0 10px 28px` + inset gold, equipment figure `drop-shadow`, location hero inset ring + `--shadow`.
- Compact mode sets `--shadow: none` and also forces `box-shadow: none` on a hardcoded panel list.

**Borders**

- Primitive panels: `1px solid var(--line-soft)`, radius `--radius`.
- Chrome / Veridia surfaces: `1px solid rgba(201, 162, 39, 0.12–0.42)`, often `border-radius: 2px–4px` or `0`.
- Chat uses a brown line (`rgba(139, 115, 85, …)`) instead of gold.
- Equipment uses a brass line (`rgba(184, 148, 78, …)` / `#b89a5a`).
- Quest NPC dialogue uses `var(--border, #3a3a3a)` — token `--border` does not exist.

**Spacing**

- Tokens `--space-*` exist but most CSS still uses raw `rem` (`0.35rem`, `0.55rem`, `0.65rem`, `0.85rem`, `1.1rem` …).
- Compact mode shrinks `--space-2` through `--space-5` and then **also** hardcodes smaller padding on named panels. Components that already use raw rem do not pick up the token change.

### Z-index

No token scale. Observed values: tooltips `20`, collapsed nav flyout `25`, chrome menus `30`, toasts `40`, combat overlays `2–5`. Feature CSS invents local stacking as needed.

### Media / assets

Chrome and feature CSS reference static files under `frontend/public/`:

- `/auth/*` — landing and create-character
- `/equipment/*` — doll, metal panel, slot silhouettes
- `/ui/overview-cta-plate.webp` — shared “ornate plate” button (home CTA, activity, chat send)
- `/icons/nav`, `/icons/activity`, `/icons/chat`, `/chrome/currency-*`

React helpers (`chromeIcons.tsx`, `activityMedia.ts`, `locationMedia.tsx`, `itemMedia.ts`, `equipmentMedia.ts`, `combatMedia.ts`, `merchantMedia.ts`, `npcMedia.ts`) map domain codes to those URLs. This is the right pattern; CSS still hardcodes several of the same paths.

---

## Existing reusable primitives

These are the current design-system surface. Later work should extend them rather than add a parallel kit.

### React (`frontend/src/ui/`)

| Primitive | File | CSS contract | Notes |
| --- | --- | --- | --- |
| `Button` | `Button.tsx` | `.btn`, `.btn-primary/secondary/ghost/danger` | No `icon` / `plate` / `link` variants. Links often copy the classes by hand. |
| `Panel` | `Panel.tsx` | `.panel`, `.panel-header` | Also used as a class dump: callers add `game-column`, `chat-panel`, `activity-rail`, etc. |
| `Tabs` | `Tabs.tsx` | `.tabs`, `.tab`, `.tab-active` | Supports `kind="filters"`. Visual is pill/chip. Arena and equipment restyle the same classes. |
| `Field` | `Field.tsx` | `.field` | Label + children only. Input look is global descendant CSS. |
| `Tooltip` | `Tooltip.tsx` | `.tooltip-anchor`, `.tooltip-panel`, placement modifiers | Absolute to trigger, not portaled. Placement flips in `useLayoutEffect`. |
| `ChromeHint` | `ChromeHint.tsx` | `.chrome-hint` | Hover/focus wrapper around `Tooltip` for top-bar icons. |
| `ProgressBar` | `ProgressBar.tsx` | `.progress-bar`, `.progress-{xp,health,stamina}` | Native `<progress>`. Extra `.xp-bar` class still applied by character XP. |
| `Badge` | `Badge.tsx` | `.badge`, `.badge-{tone}` | Tones: neutral, accent, danger, warning. |
| `StatusBadge` | `StatusBadge.tsx` | `.status-badge`, `.status-{tone}` | Tones: safe, danger, neutral, upgrade, downgrade, mixed. Shares pill geometry with `.badge` / `.rarity`. |
| `RarityBadge` | `RarityBadge.tsx` | `.rarity`, `.rarity-{common…epic}` | |
| `Dialog` | `Dialog.tsx` | `.ui-dialog` | Native `<dialog>`. |
| `ToastProvider` / `useToast` | `ToastRegion.tsx` | `.toast-region`, `.toast` | Single message type, 2.8s timeout. |
| `ComingLaterButton` / `ComingLaterChip` | `ComingLater.tsx` | `.coming-later` | Disabled placeholder affordance. |
| `EmptyState` / `LoadingState` / `ErrorState` | `*State.tsx` | `.ui-state`, `.muted`, `.form-error` | Loading and empty are visually identical. |
| `StatRow` | `StatRow.tsx` | `dt`/`dd` + `.stat-value`, `.stat-delta-*` | Requires a parent `dl.stat-list` / `.character-summary` / `.derived-stats`. |
| `CharacterPortrait` | `CharacterPortrait.tsx` | `.portrait` | |
| Item stack | `ItemCard`, `ItemDetail`, `ItemPeek`, `ItemTooltip`, `InventoryItemSlot`, `InventoryItemRow` | item / inventory / tooltip classes | Shared item presentation. |
| `EquipmentLayout` | `EquipmentLayout.tsx` | large doll/slot block in `components.css` | Feature-sized primitive. |
| Navigation helpers | `gameNav.ts`, `navCollapse.ts`, `hashFocus.ts`, `uiMode.ts` | — | Behavior, not visuals. |
| `classNames` | `classNames.ts` | — | Local utility. |

### CSS primitives that have no dedicated React wrapper

- `.btn-icon`, `.btn-icon-chrome`
- `.visually-hidden`
- `.currency-chip`
- `.inventory-badge`
- `.item-icon-frame` / `.item-icon-face-*`
- `.vital-block` / `.vital-meter`
- Plate CTA pattern (`.character-overview-cta`, `.activity-claim`, `.chat-send`) — same `border-image` asset, three copies

### Shared behavioral conventions worth keeping

- Server-authoritative data; UI sends intent only.
- `data-testid` on interactive chrome and states.
- Compact mode must keep the same information architecture (`PHASE_3_SPEC.md` §10).
- `ComingLater*` for unimplemented chrome, not fake services.

---

## Duplicated components/styles

### Panel surfaces

`components.css` applies the same padding / fill / border / radius / shadow to:

`.panel`, `.game-column`, `.chat-panel`, `.combat-panel`, `.encounter-prompt`, `.expedition-panel`, `.crafting-panel`, `.pvp-panel`, `.market-panel`, `.auth-page`

Then `game-shell.css` immediately undoes this for chrome and several features (chat padding 0, combat HUD transparent, activity rail custom fill, equipment metal panel, location hero). Compact-mode lists in `index.css` repeat the same class roster.

**Keep one panel primitive.** Feature skins should be modifiers (`.panel-chrome`, `.panel-hero`, `.panel-flat`), not sibling copies.

### Buttons

Four visual families:

1. Token `.btn` variants (rounded `--radius-sm`, gold/line).
2. Chrome icon buttons (`.btn-icon-chrome`) — square, no `.btn` required.
3. Ornate plate buttons via `border-image` (home CTA, activity claim/view-all, chat send).
4. Equipment inspector actions — square brass buttons, Cinzel, hardcoded `#2a2218` / `#b89a5a`.

Links frequently use `className="btn btn-secondary"` instead of a shared `Button`/`Link` primitive.

### Tabs

- Default `Tabs`: pill, bordered, `--accent-soft` fill when active.
- `.arena-tabs .tab`: underline, no border, inset gold bar.
- `.equipment-subtabs .tab`: Cinzel, no border, inset bottom gold.
- `.market-tabs`: extra wrapper around the same pill tabs.

One component, three skins. The pill look no longer matches Veridia chrome.

### Inputs

Shared rule in `components.css` styles `.field input/select`, `.auth-form input`, `.market-create-form input/select`, `.chat-form input`, `.inventory-filters select`.

Then chat restyles its input to 2px radius, brown border, `#12110f`, Cormorant. Auth restyles through `.auth-input` in `authLanding.css`. Inventory filters are defined twice (`index.css` and `game-shell.css`).

### Progress / meters

| Implementation | Where | Look |
| --- | --- | --- |
| `<progress class="progress-bar">` | `ProgressBar` | 0.72rem pill, inset shadow, gold/health/stamina gradients |
| `.xp-bar` alias | Character XP | Same as XP tone |
| Overview overrides | `.character-overview-card .progress-bar` | 0.28rem flat, `#8b1e1e` / `#b8860b` |
| Combat stage | `.combat-stage-vital .progress-bar` | 0.62rem, 2px radius |
| `.arena-meter` | Arena rank | Custom div + width style, gold gradient |
| `.item-durability-track` | Equipment inspector | 0.32rem brass fill |
| `.vital-block` | Character panel | Uses `ProgressBar` at default density |

Health/stamina therefore appear in at least two palettes (`--health`/`--stamina` vs overview `#8b1e1e`/`#b8860b`).

### Tooltips

- Default `.tooltip-panel`: 22rem, `--panel-solid`, `--line`, `--shadow`.
- Compact: narrower, no shadow.
- `.chrome-hint .tooltip-panel`: compact label chip.
- `.equipment-layout-doll .tooltip-panel`: 11.5rem peek.

Same component, three densities. No portal, so overflow/clipping is a recurring risk inside `overflow: hidden` shells (equipment page, combat HUD).

### Lists and rows

Repeated “icon + copy + meta” rows:

- `.activity-row`
- `.chat-panel .chat-list li`
- `.inventory-row`
- `.destination-list li` / `.action-list li` / `.nearby-list li` (still in `index.css`)
- `.arena-history li`

Each invents its own grid, type size, and divider color.

### Stat blocks

`.stat-list`, `.character-summary`, and `.derived-stats` share one rule block. Equipment footer then restyles `.derived-stats` again. Overview uses a fifth-column `.overview-stat-*` grid instead of `StatRow`.

### “Coming later” and unread badges

- `.coming-later` opacity 0.55
- `.inventory-later` dashed border, opacity 0.5
- Equipment later buttons force `opacity: 1` and look like real secondary buttons
- Unread/count chips: `.inventory-badge` (`--danger`, `#fff`, pill), `.activity-badge` (`#7a1616`), `.chat-unread` (`#7a2d2d`, 0.15rem radius)

### Auth / create-character token twins

`--auth-gold` (`#c4a45c`) and `--cc-gold` (`#c4a45c`) are the same hex, close to but not equal to `--accent` (`#c9a227`). Metal-text gradients are copy-pasted. Plate/metal grain asset is shared. This is a third mini design system beside tokens + game chrome.

### CSS rule duplication

- `.inventory-filters` in `index.css` and `game-shell.css`
- Compact panel padding lists in `index.css` and again in `game-shell.css`
- `.equipment-footer-col { overflow: hidden; }` declared twice
- Visually-hidden clip pattern copied into collapsed nav and collapsed chat instead of reusing `.visually-hidden`

---

## Hardcoded values

Counts of hex/rgb/hsl literals (ripgrep, including token definitions):

| File | Approx. color literals |
| --- | --- |
| `pages/createCharacter.css` | 116 |
| `pages/authLanding.css` | 111 |
| `ui/game-shell.css` | 107 |
| `ui/components.css` | 45 |
| `ui/tokens.css` | 28 (expected) |
| `index.css` | 6 |
| `pages/CreateCharacterPage.tsx` | SVG stops/fills |
| `ui/locationMedia.tsx`, `ui/combatStatusIcons.tsx` | SVG fills |

### Highest-traffic hardcoded families

**Gold / brass** (should map to `--accent*` or a new `--chrome-line`):

- `#c9a227`, `rgba(201, 162, 39, 0.06–0.70)` throughout nav, activity, location, arena
- `#e2c57d`, `#e8c86a`, `#d7b45a` in arena
- `#c4b49a`, `#b89a5a`, `rgba(184, 148, 78, …)`, `rgba(138, 109, 50, …)` in equipment
- `#c4a45c` / `#e0c57a` in auth and create-character

**Ink / parchment** (should map to `--ink` / `--ink-soft`):

- `#efe7d8`, `#f4efe4`, `#f7f1e4`, `#e8e2d8`, `#d8cfc0`, `#c8c2b8`, `#c8beae`, `#9a9184`, `#8d877c`

**Grounds**:

- `#0d0b0a`, `#100e0c`, `#12100e`, `#12110f`, `#14110e`, `#14110f`, `#161310`, `#1a1613`, `#1a1714`

**Semantic colors that bypass tokens**:

- Overview HP/STA: `#8b1e1e`, `#b8860b`
- Activity highlights: `#c5a059`, `#7fb3d5`, `#a93226`
- Chat names / links: `#a68a56`, `#4e8b69`, `#4f8d8a`, `#5dade2`
- Online / office switch: `#5f8f62`, `#2d5a38`, `#8fd49a`
- Location safety pills: `#8fd18c`, `#e08a7c` (also reused as arena win/loss)
- Equipment set bonus: `#9ec27a`
- Compare placeholder: `#7ea0d4`, `#b36a6a`
- Inventory badge text: `#fff`
- Primary button text: `#16120e`
- Danger button text: `#f7faf7`
- Quest border fallback: `#3a3a3a`

**Type**: `'Cinzel'` appears dozens of times in `game-shell.css`, `authLanding.css`, `createCharacter.css`, and once in `components.css`. Missing `--font-chrome` (or similar).

**Radius**: chrome uses `2px`, `3px`, `4px`, `6px`, `999px` while tokens say `10px` / `8px`. Veridia chrome is visually sharper than the primitive kit.

**Motion**: equipment slots use `130ms ease`; token `--motion` is `150ms ease`. Compact mode nukes all transitions with `!important`.

**Inline styles** (art URLs or meter width only — acceptable, but meters should become the shared progress primitive):

- `LocationPanel`, `ArenaPanel` — `backgroundImage`
- `ArenaPanel` rank meter — `style={{ width: ... }}`

SVG icon colors in `locationMedia.tsx` / `combatStatusIcons.tsx` / create-character ornaments are hardcoded. That is reasonable for illustrations, but combat status red already exists as `--danger` / `--health`.

---

## Visual inconsistencies

These are player-visible, not just CSS hygiene.

1. **Two interface dialects in one session**  
   Left nav, activity, location hero, home overview, chat, and equipment speak Veridia (Cinzel, tight radius, plate CTAs, brass lines). Inventory, market tables, mastery, expeditions, crafting, quest log, and default `Button`/`Tabs` still speak the older rounded token kit.

2. **Office Mode is incomplete**  
   Token shrink + art hiding is real, but many chrome screens keep custom min-heights, plate buttons, and Cinzel display sizes. Compact rules are a selector allow-list, so new classes silently miss the mode.

3. **Accent gold is not one color**  
   `--accent` `#c9a227`, auth/create `#c4a45c`, equipment brass `#b89a5a` / `#c4b49a`, arena `#e2c57d`, chat inset `#8b7355`. Gold reads as “warm metal” but does not lock to a palette.

4. **Progress bars change personality by screen**  
   Pill + gradient in character/combat vs hairline flat bars on the home overview vs a custom arena meter.

5. **Primary actions are not one control**  
   `.btn-primary` (filled gold, dark text) vs plate image buttons (parchment text on carved metal) vs equipment `#2a2218` brass. Same intent, three finishes.

6. **Tabs are not one control**  
   Pills vs underlines vs equipment subtabs.

7. **Chat is a third micro-theme**  
   Brown channels, Cormorant body, plate send, unique name colors. It does not use `--line` / `--accent` the way nav does.

8. **Type hierarchy drifts**  
   Section labels are sometimes Source Sans uppercase (`vital-block-header`), sometimes Cinzel 0.62–0.78rem tracked (`activity-head`, overview, location kicker). Display titles mix Cormorant (`--font-display`) and Cinzel.

9. **Focus**  
   Global `:focus-visible` uses `--focus-ring`. Many chrome buttons rely on hover fills only. Compact disables transitions but not focus.

10. **Legacy header vs game top bar**  
    Players who hit the fallback `.app-header` see the old blur bar and Cormorant wordmark, not the game crest/top bar.

11. **Safety / status color**  
    Location hero pills use brighter greens/corals than `StatusBadge` (`--accent-strong` / `--danger`). Arena win/loss copies the hero pills, not the badges.

12. **PHASE_3_SPEC tension**  
    Spec asks to avoid excessive decorative borders and oversized cards. Location hero (22rem), ornate plates, and equipment metal panel are already decorative. Later design-system work should decide which of these are the product language vs leftover exploration — without restyling screens in the token-extraction task.

---

## Components requiring refactor

Refactor here means “bring onto the shared engine”, not “redesign the game”. Order is in the last section.

### Design tokens

- Add missing tokens: `--font-chrome` (Cinzel), chrome line/fill, plate CTA, z-index scale, sharp radius, online/success, chat channel line, overview vital colors **or** retire those one-offs in favor of `--health`/`--stamina`.
- Align auth/create local tokens to the same names once a single gold/ink set is chosen.
- Document Office Mode as token-first so new CSS inherits density without selector lists.

### Primitives (React + CSS)

- `Button` — plate, chrome-icon, and link variants; stop copying class strings onto `<Link>` / `<button>`.
- `Tabs` — add an underline/chrome appearance used by arena/equipment, keep filters as a kind.
- `Field` + a real `TextInput` / `Select` so chat/auth/market are variants, not descendant overrides.
- `ProgressBar` — density (`default` | `compact` | `hairline`) and allow non-`<progress>` only if accessibility is preserved.
- `Panel` — variants for chrome rail, hero, flat inspector. Stop aliasing ten feature class names to the same rule.
- `Tooltip` — shared density props; consider a portal in a later shell task if clipping remains.
- Badge family — one geometry, tones for rarity/status/count.

### CSS packaging

- Split `game-shell.css` into chrome vs feature files **after** tokens exist, without visual change.
- Move equipment doll CSS out of `components.css` into a feature stylesheet.
- Shrink `index.css` to reset, focus, and leftover layout that is still mounted.

### Chrome that must join the engine before screen productization

- `GameTopBar`, `GameLeftNav`, activity rail, chat chrome, location hero chrome, plate CTA.
- Office Mode toggle currently exists in both `AppShell` (fallback header) and `GameLeftNav` (office switch).

### Do not treat as engine work

Gameplay-specific layouts (combat HUD grid, equipment doll coordinates, arena dashboard, market merchant workspace, create-character column layout) should stay feature CSS. The engine should only supply tokens and primitives those screens consume.

---

## Styles that can later be removed

Do not delete these in the audit or in a blind cleanup. Remove only after a later task proves no callers.

| Candidate | Why it looks removable | Gate |
| --- | --- | --- |
| `.auth-page` rules in `components.css` / `index.css` | No TSX uses `auth-page` | Confirm no test HTML / future route |
| Compact overrides targeting `.auth-page` | Auth pages hide `AppShell` header and use their own CSS | Same |
| `.inventory-filters` block in `index.css` | Superseded by `game-shell.css` | After inventory is on one stylesheet |
| `.xp-bar` selectors | `ProgressBar` already has `progress-xp` | After `CharacterSummaryPanel` drops the extra class |
| `.equipment-stage-halo { display: none }` | Dead hook | After equipment layout cleanup |
| `.coming-later-hint { display: none }` | Unused reveal | Confirm no TSX |
| `.chat-unread { display: none }` plus re-enable on equipment | Confusing leftover | Chat unread task |
| Duplicate `.equipment-footer-col { overflow: hidden }` | Copy-paste | Equipment CSS split |
| `var(--border, #3a3a3a)` | Token does not exist; SaaS gray | Quest UI pass |
| Fallback `.app-header` styles | Only for non-game authenticated states | When create-character / empty-character uses Veridia chrome consistently |
| Pre-shell list styles in `index.css` (`.destination-list`, `.action-list`, `.nearby-list`, `.activity-list`, `.chat-list`) | Largely replaced by shell-specific rules | After location/activity/chat no longer need the old defaults |
| `html[data-ui-mode='compact']` art-hiding lists that name every panel | Should become token + a few layout hooks | Office Mode task |

Auth landing and create-character CSS should **not** be deleted. They are the current Veridia reference for out-of-game screens.

---

## Recommended migration order

This order is for **later tasks**. This audit implements none of them.

### 1. Token extraction (Task 12 start)

- Inventory every repeated gold, ink, ground, radius, shadow, and font into `tokens.css`.
- Add `--font-chrome`.
- Map Office Mode to tokens (`--radius`, `--space-*`, `--shadow`, art visibility hooks) before adding more compact selectors.
- Do not restyle screens. Hex replacements must be visual no-ops.

### 2. Primitive CSS split (Task 12)

- Keep `components.css` to buttons, fields, tabs, badges, progress, dialog, toast, tooltip, panel, states.
- Move equipment doll / item-frame / inventory-slot blocks to feature CSS.
- Add missing primitive variants (plate button, underline tab, hairline progress) by **codifying what already exists**, not inventing a fourth look.

### 3. React primitive adoption (Task 12)

- Route links and raw `.btn` class strings through `Button` (or a `ButtonLink`).
- Give `Tabs` an appearance prop and point arena/equipment at it without changing layout.
- One input primitive for chat/market/inventory filters.

### 4. Shell + Office Mode (Task 13)

- Treat `game-shell.css` chrome (top bar, left nav, rails, compact body grid) as the product shell.
- Collapse duplicate Office Mode lists.
- Decide whether fallback `.app-header` remains or create-character/auth stay the only non-shell chrome.

### 5. Feature productization, one surface at a time (Task 14+)

Recommended screen order after the engine exists, because they already share chrome or already drifted farthest:

1. Activity + Chat (chrome-adjacent, heavy hardcoded color)
2. Home overview / location hero (already Veridia; align to tokens)
3. Inventory + Market (still old kit; high player time)
4. Character / Mastery / Expeditions / Crafting / Quests (still primitive kit)
5. Combat HUD and Arena (feature-specific; consume tokens only)
6. Equipment doll (already visually ahead; token-align, do not rebuild)

Auth and create-character stay isolated until an onboarding task. Do not “helpfully” restyle them during engine extraction.

### Explicit non-goals for the next engine task

- No new component library, CSS-in-JS, or Tailwind.
- No second item/combat/inventory model.
- No gameplay-specific screen redesign unless that screen’s later task asks for it.
- No deletion of unused CSS until callers are proven gone.

---

## Source map

Primary files inspected:

- `frontend/index.html`
- `frontend/src/index.css`
- `frontend/src/ui/tokens.css`
- `frontend/src/ui/components.css`
- `frontend/src/ui/game-shell.css`
- `frontend/src/pages/authLanding.css`
- `frontend/src/pages/createCharacter.css`
- `frontend/src/ui/*.tsx` primitives
- `frontend/src/main.tsx`, `App.tsx`, `components/AppShell.tsx`, `components/GameLayout.tsx`
- `docs/PHASE_3_SPEC.md` §2.4, §9, §10
- `docs/PHASE_3_EXECUTION.md` tasks 12–14
