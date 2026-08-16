# Visual Fidelity Spec

Date: 2026-08-16  
Status: visual contract for later productization. **No implementation in this document.**

## Purpose

This spec decomposes the approved **full-screen** visual references into one shared visual system.

These references are **not** pixel-perfect component mocks. They are the visual source of truth for:

- how a Greyhaven screen should feel as a whole;
- how surfaces, metal, type, light, and controls relate;
- why the current `/dev/ui` engine reads cheaper than the approved direction.

Existing `/dev/ui` and production CSS are an **implementation reference** only. They do not override the mockups.

Phase 3 already adopted this direction in `docs/PHASE_3_SPEC.md` §9: dark fantasy MMORPG + modern desktop game + high information density.

Product name in the references is **VERIDIA**. Greyhaven keeps that visual language, not the placeholder brand.

---

# 1. Reference hierarchy

## 1.1 Visual source of truth

Approved full-screen references, in this order of authority:

| Order | File | What it teaches |
| --- | --- | --- |
| 1 | `docs/mockups/main.png` | Default shell: top bar, nav, location hero, three work columns, chat, guild, activity rail. The home density and panel grammar. |
| 2 | `docs/mockups/equipment.png` | Character workspace: doll as focal art, recessed slots, selected-item floating plate, tight stat ledgers. |
| 3 | `docs/mockups/inventory.png` | Grid + inspector: inset slots, filter strip, rarity as name/icon language, comparison column. |
| 4 | `docs/mockups/market.png` | Dense table + inspector: search/filter chrome, selected row, listing rows that are **not** cards. |
| 5 | `docs/mockups/combat.png` | Cinematic center + grounded HUD: vitals, skill plates, battle log, danger actions. |

If a later screen task conflicts with a primitive in `/dev/ui`, the mockups win.

## 1.2 Implementation reference (not visual)

Use only to understand what already exists:

- `docs/ui/DESIGN_TOKENS.md`
- `docs/ui/UI_ENGINE_AUDIT.md`
- `frontend/src/ui/tokens.css` and the primitive CSS under `frontend/src/ui/`
- `/dev/ui` showcase

Do not copy a cheap current treatment because it is already tokenized.

## 1.3 How to read the mockups

Decompose the **screen**, then extract the repeating system.

Do **not**:

- measure one button and treat it as a unique widget skin;
- invent a sixth aesthetic that “averages” the five images;
- treat atmospheric art as a fill for every panel.

Do:

- find the same panel, metal, type, and control language on every screen;
- treat hero art, combat scene, and item glow as **local focal lighting**, not as the default chrome finish.

## 1.4 Shared screen grammar (all five)

Every reference is the same desktop shell:

1. **Top bar** — identity, XP hairline, currencies, utility icons.
2. **Left nav** — destination list, then Quick Actions, then Office Mode.
3. **Main workspace** — page heading + one focal region + supporting columns.
4. **Bottom communication** — chat (and sometimes a sibling social panel).
5. **Right rail** — activity, claimable rewards, notifications, alerts.

Combat keeps the chrome and replaces the workspace with a scene + HUD. It does not invent a second visual language.

---

# 2. Color system

Colors below are **observed families** sampled across the five references. They are the target language, not a demand to paste these exact hexes into CSS in this task.

## 2.1 Neutral black

**Observed:** page gutters and deepest wells sit near `#080b0b` … `#0a0c0d`. True `#000000` appears only as crush in grain/vignette, not as a flat page fill.

Role:

- page ground;
- gaps between panels;
- deepest inset troughs (slot wells, chat log, meter tracks).

This is slightly **cool charcoal**, not the current warm brown page `#12100e`.

## 2.2 Warm black

**Observed:** chrome fills cluster around `#12110f` … `#161615`.

Role:

- base panels (nav, activity rail, chat frame, workspace frame);
- default plate behind lists and stats.

Warmth is subtle (a few points of red/brown). It must not read as chocolate brown or as SaaS `#111`.

## 2.3 Dark brown

**Observed:** `#261f16` … `#291f16` as a minority color — raised header metal, worn leather-adjacent plates, some button faces.

Role:

- raised utility plates;
- header / currency strip lift;
- material accent under bronze edges.

Not a second theme. A step **above** warm black, still dark.

## 2.4 Bronze

**Observed mass of metal:** `#634b33` … `#6e5945`.

This is the **structural metal** of the UI:

- default panel edges;
- inactive tab metal;
- section-label color;
- icon rims;
- default button border;
- separators that should feel forged, not CSS-gray.

Bronze is dull, dusty, and brown-olive. It is not yellow.

## 2.5 Gold

**Observed bright metal:** `#a48667` … `#a98b75`. Inventory shows a slightly cleaner brass near `#c09962` on selected frames and important numbers.

Gold is **polished bronze**, used for:

- selected / active metal;
- page-level important numbers (currencies, key stats);
- active nav mark;
- primary heading metal when the heading is meant to gleam;
- XP fill and selected slot rim.

Gold is rare relative to bronze. If gold appears on every border, it stops being gold.

## 2.6 Text colors

| Role | Observed | Use |
| --- | --- | --- |
| Primary | `#c2c0b8` … `#d0c3b5` (cool off-white / pale parchment) | Body, item names when not rarity-colored, button labels on dark plates |
| Secondary | `#90857e` … `#968d82` | Supporting copy, inactive tabs, nav labels at rest |
| Metadata | same family, smaller / dimmer | timestamps, “4m ago”, filter captions, slot counts |
| Disabled | darker than metadata, still readable | unavailable actions |

Primary text is **silver-parchment**, not warm cream `#e8e0d4` and not `#ffffff`.

Bright white is reserved for:

- notification badge numerals;
- the most important combat / currency numbers when they must punch through atmosphere.

## 2.7 Functional colors

Used as **information**, not as decoration.

| Role | Observed character | Where |
| --- | --- | --- |
| Health / danger | deep desaturated red (`#8f3c2c` … `#a02020` family) | HP bars, alerts header, damage log, unread counter fill |
| Stamina | warm amber-orange, metal-adjacent | stamina bars, some XP |
| Positive / safe | muted forest green (`#344e35` … `#4a7c44`) | Safe Zone, +stat deltas, equipped, heal log |
| Info / system | restrained blue, used on names/links, not large fills | system chat, some technical links |
| Rarity | gray / green / blue / purple / orange-gold | **item name + icon halo + thin inner rim**, not a pastel chip wash on every row |

Functional color stays **inside** the dark-fantasy range. It must not look like a Bootstrap status palette.

## 2.8 Color hierarchy rule

From most common to rarest:

1. near-black ground  
2. warm-black panel  
3. bronze structure  
4. silver text  
5. gold emphasis  
6. functional / rarity color  

A screen that inverts this (gold fills, bright semantic chips, cream text on mid-brown cards) will look cheaper than the references regardless of spacing.

---

# 3. Surface system

## 3.1 Page background

A **near-black charcoal field** with fine grain. Not a flat hex. Not a large brown radial wash.

Gutters between panels are this ground showing through (about 8–12px). The ground is darker than every panel. If page and panel are within a few RGB steps, the shell collapses into one cheap slab.

## 3.2 Base panels

The default chrome: nav, workspace frame, chat, activity rail, market table frame.

- fill: warm black, slightly above page;
- edge: thin bronze (see §6);
- finish: faint material, not a visible “card gradient”;
- corners: sharp or 1–2px ease — industrial, not SaaS radius.

Base panels are **heavy and receded into the world**, not floating cards on a dashboard.

## 3.3 Raised areas

Used sparingly:

- top-bar identity / currency cluster;
- primary action plates;
- active skill card;
- selected inspector when it must lift off a table.

Raised is a **small luminance step + brighter top metal + short dark shadow**. It is not a 18–40px drop shadow and not a gold wash.

## 3.4 Inset areas

The most important depth cue after the page/panel split.

Always inset:

- text inputs, search, chat field;
- inventory / equipment slots;
- meter tracks;
- chat log and battle log wells;
- table body relative to its toolbar.

Inset = darker fill + inner shadow (light from above, trough below) + slightly darker edge. Slots should feel **hollowed into metal**, not painted squares.

## 3.5 Floating areas

Item inspector, tooltip, dropdown, dialog.

- darker and more framed than a base panel;
- short, dark, tight shadow (object in a dim room, not a Material card);
- optional **local glow behind the subject** (item icon, portrait), not a glow on the whole frame;
- never glass / `backdrop-filter`.

On equipment and inventory, the selected-item plate is a floating surface **inside** the workspace, not a second page.

## 3.6 Surface ladder (must remain distinct)

| Level | Relative luminance | Typical use |
| --- | --- | --- |
| Page | darkest | gutters, world |
| Inset | darker than base | wells, fields, slots, tracks |
| Base | mid chrome | frames, rails |
| Raised | lighter plate | controls, lifted chrome |
| Floating | raised + shadow + tighter frame | overlays |

Current tokens compress page / inset / base into almost one brown. That is the first structural failure.

---

# 4. Material language

The references are **aged metal on dark stone**, not “gold UI theme.”

## 4.1 What creates metal

- bronze/gold **edges** with uneven brightness (top lighter than bottom);
- brushed variation along a long border (not a 1px `#8a6d32` stroke);
- recessed slots and tracks (the metal has thickness);
- painted icons that look cast or embossed (currencies, crests, slot silhouettes);
- small corner brackets / pips on major frames.

## 4.2 What creates aged / dark fantasy

- grain on the page;
- vignette on hero art (location banner, market banner, combat scene);
- desaturated functional colors;
- dustier gold (rose-brass, not lemon);
- leather-adjacent dark brown only as a minority plate, never as the page.

## 4.3 Where texture belongs

| Surface | Texture |
| --- | --- |
| Page | fine grain / grit, always |
| Hero banners / combat scene | painterly art + vignette |
| Major frames | very faint material; may inherit page grain at low opacity |
| Slot wells, inputs | mostly flat dark + inner shadow (the recess **is** the material) |
| Icons, crests, currencies | painted / metallic art |
| Body of a data table | quieter than the frame — texture must not fight numbers |

## 4.4 Where flat is correct

- text blocks;
- table cells;
- chat lines;
- compact filter chips that are really labels;
- Office Mode reductions of art (grain may stay; hero art may go).

Flat does **not** mean “one hex + 1px border.” A flat cell still sits inside a material frame.

## 4.5 Material hierarchy (gold is not a fill)

| Material | Meaning |
| --- | --- |
| Stone / near-black | world, page |
| Warm black plate | usable chrome |
| Bronze edge | default forged structure |
| Gold edge / number | selected, owned, valuable |
| Painted art | content, not chrome |

**Gold fill as a primary button face is outside this language.** The references use dark plates with metal rims. Gold paint as a CTA is mobile-F2P, not these screens.

---

# 5. Typography

Two families, strict roles. Display serif is ceremonial. UI sans does the work.

Observed pattern: **Cinzel-class** (or equivalent high-contrast inscriptional serif) for brand and page titles; a clean humanist sans for everything else.

## 5.1 Page headings

- Inscriptional serif, all caps.
- Large relative to the workspace (bigger and more tracked than current `.type-page-heading` at 1.375rem).
- Color: pale metal or off-white, sometimes a quiet gold. Not lemon `#e4c56a`.
- Sits in the workspace header, often over or beside a thin ornament / art strip.
- Examples: `MARKETPLACE`, `INVENTORY`, `EQUIPMENT`, `COMBAT`.

Brand wordmark (`VERIDIA`) is the only larger ceremonial line.

## 5.2 Section headings

- Small, all caps, **sans**, wide tracking.
- Color: **bronze / muted metal**, not primary text.
- Quieter than body. They label a region; they do not compete with content.
- Examples: `NAVIGATION`, `ITEM DETAILS`, `COMBAT STATS`, `RECENT EVENTS`.

Current `.type-section-heading` (1rem, primary cream, weight 600) is too large, too bright, and too “card title.”

## 5.3 Normal UI

- Sans, 13–15px class, regular weight.
- Primary silver-parchment.
- High density, line-height tight enough for ledgers (stats, market columns) but not compressed into noise.

## 5.4 Numbers

- Tabular, slightly heavier than body.
- Currencies and key combat stats: brighter (white or gold).
- Secondary numbers (bag `78 / 100`, timestamps): metadata color.
- Numbers must **pop**; they are how a player reads the game at a glance.

## 5.5 Metadata

- Smallest readable sans.
- Muted gray-bronze.
- Timestamps, filter captions, “updated 4m ago”, chat clocks, empty hints.

## 5.6 Button text

- Sans, all caps or near-caps, medium weight, tracked.
- On dark plates: primary silver or quiet gold.
- Centered.
- Never display serif on a control.
- Never dark ink on a bright gold fill (that combination does not appear as the default CTA).

## 5.7 Type hierarchy (must stay stacked)

1. Brand / page inscription  
2. Important names (item, character) — rarity color when applicable  
3. Body / compact UI  
4. Numbers (brightness, not size, does the work)  
5. Section labels (small bronze caps)  
6. Metadata  

If section labels are as bright as body, the screen becomes a pile of equal titles.

---

# 6. Borders

## 6.1 Thickness

Default structure is **1px**. Major frames may read as 1–2px because of a **double edge** (dark outer + metal inner), not because of a fat stroke.

Do not use 2–3px bright gold as “premium.”

## 6.2 Brightness (not one swatch)

A metal edge is a **range**:

- top (and often left): lighter bronze / pale highlight;
- bottom (and often right): darker, almost disappearing into the page;
- corners: slightly brighter pips or brackets.

A uniform `1px solid rgba(gold, 0.22)` on every side is the cheap tell. All four sides the same brightness looks printed, not forged.

## 6.3 Inner borders

Used to split a panel without creating a new panel:

- chat channel list vs log;
- inspector header vs stats vs actions;
- table toolbar vs rows;
- nav list vs Quick Actions.

Inner rules are **dimmer** than the outer frame. Often a hairline that fades at the ends.

## 6.4 Selected borders

Selected is a **brighter metal**, still bronze-gold, plus one structural cue:

- nav: left metal bar / glow along the active row;
- table row: full-row wash **barely** lighter + stronger left or outer rim;
- inventory slot: gold frame around the cell;
- skill card: brighter rim + slight lift.

Selected is not a 22% yellow overlay on the whole plate.

## 6.5 Separators

List and table separators are quieter than frames:

- 1px, low-contrast bronze or warm gray;
- sometimes a fade at the ends;
- they divide **rows**, they do not box each row.

If every row has a full border, separators have been mistaken for frames.

---

# 7. Shadows / lighting

## 7.1 Depth model

Depth is mostly **stacked darks**, not drop shadows.

Order of cues:

1. page darker than panel  
2. inset trough  
3. 1px top highlight (light from above)  
4. short dark contact shadow only on raised / floating  
5. local focal light on art and selected objects  

A large soft `0 18px 40px` under every panel is web-card language. The references do not do that.

## 7.2 Inner shadows

Mandatory on wells: inputs, slots, meter tracks, logs.

The trough is how metal reads as thick. Without it, slots are stickers.

## 7.3 Top highlights

Almost every raised plate and many frames have a **hairline of lighter metal or pale dust on the top edge**.

This is lighting, not a second brand color. Keep it dim (a few percent of parchment or pale bronze).

## 7.4 Focal lighting

Each screen has **one** bright atmospheric center:

| Screen | Focal light |
| --- | --- |
| Main | Location banner (city art + vignette) |
| Market | Market banner (warm firelight in the painting) |
| Combat | Encounter scene (characters lit, UI around it stays darker) |
| Inventory / Equipment | Item icon / doll, with a **local** glow behind the subject |

Chrome around the focus stays darker on purpose. Lighting the entire UI to match the hero art flattens the hierarchy.

## 7.5 Floating surfaces

Float with a **tight dark umbra**. The object is close to the table. No wide haze, no colored glow on the frame, no glass.

---

# 8. Buttons

Derived from the same plates on all five screens: `View Character`, `World Map`, `Claim`, `Buy`, `Send`, `Unequip`, `Flee Encounter`.

## 8.1 Default (secondary / most buttons)

- Sharp rectangle (0–2px radius).
- **Dark plate** (raised or base), not gold fill.
- Thin bronze rim.
- Top highlight + slight vertical darkening toward the bottom.
- Label: compact caps, silver or pale gold.
- Hover: brighter metal rim + slightly lighter plate. No lemon wash.
- Active: becomes inset (pressed into the metal).
- Disabled: lower opacity, rim goes dull bronze, no extra gray theme.

## 8.2 Primary

Primary is **hierarchy**, not a different material.

Observed primaries (`Claim`, `Buy`, `Send`, `World Map`) are still dark metal plates. They win by:

- brighter bronze/gold rim;
- slightly more highlight;
- sometimes a warmer plate (dark brown, not yellow);
- stronger label.

They do **not** become a solid gold lozenge with dark text.

## 8.3 Danger

`Flee Encounter` and similar: same plate geometry, **red-brown tint** in fill and/or rim. Still metal. Not a flat `#c45c4a` text button and not a filled scarlet pill.

## 8.4 Ghost / icon

Utility icons in the top bar are quiet metal marks. Badge dots sit on them. They are not large rounded icon-buttons.

## 8.5 What `/dev/ui` does instead

`.btn-primary` mixes ~72% `#c9a227` as a fill and sets text to page-bg. That single treatment is the strongest “cheap mobile CTA” signal in the engine.

---

# 9. Tabs

Observed: chat (`GLOBAL` / `TRADE` / …), market listing modes, inventory filters (`All Items`), combat log (`All` / `Player`), equipment sub-views.

## 9.1 Rules

- Tabs are **text on the frame**, not chips and not pills.
- Inactive: secondary/muted, no fill.
- Active: brighter label (primary or gold) + a **connecting** cue (underline that meets the panel edge, or a brighter metal tick).
- Optional unread: small red counter on the label, same as chrome badges.
- Filter tabs may sit in a dark strip, still not rounded capsules.
- Display serif is not used on tabs.

## 9.2 What `/dev/ui` does instead

`.tab-active` paints `--color-surface-selected` (gold at 22%) and a 1px gold underline. The fill turns tabs into yellow chips. The references never fill the tab face with gold.

---

# 10. Inputs

Observed: chat compose, inventory search, market search, rarity/type filters, equipment preset.

## 10.1 Text / search

- Deep inset well (darker than the parent panel).
- Thin bronze rim, dimmer than a button rim.
- Inner shadow (etched).
- Placeholder in metadata color.
- Search: small bronze magnifying mark **inside** the well, not a separate SaaS icon button.
- Focus: rim brightens to selected metal. No thick outer glow. Keyboard focus may add a tight ring for a11y; it must stay metal, not electric yellow.

## 10.2 Select

- Same well as input.
- Trailing chevron in bronze.
- Closed state looks like a field, not like a button.
- Open list is a **floating** surface (see §3.5), aligned to the field, dense rows, no large radius.

## 10.3 What `/dev/ui` does instead

Inset tokens exist and are directionally right, but they sit on a page that is already as light as a panel, so the well barely recedes. Focus uses `--color-gold-normal` (`#c9a227`) — too yellow versus aged metal.

---

# 11. Tooltips

Full-screen refs show the **inspector plate** more often than a hover tooltip. Treat tooltip as a compact floating inspector.

## 11.1 Rules

- Floating surface: dark, framed, tight shadow.
- Width constrained; content is a ledger (name, rarity, stats), not a paragraph card.
- Name uses item/rarity color; body uses compact UI; extras use metadata.
- Optional local glow **behind the icon**, not around the whole tooltip.
- No glass, no large radius, no gold fill header bar.

Chrome hints (top-bar utilities) are the compact end of the same language: smaller, still metal-framed.

## 11.2 What `/dev/ui` does instead

`.tooltip-panel` is the generic floating card: same raised brown, same uniform border, same drop shadow recipe as dialogs and toasts. It does not read as an item plate.

---

# 12. Progress bars

Three observed species. Do not collapse them into one flat `<progress>` skin.

## 12.1 Vitals (health / stamina / mana)

- Thick enough to read as a trough (not a 4px hairline).
- Track is inset, near-black.
- Fill is **desaturated** red / amber / muted blue-green.
- A glassy or liquid **top highlight** on the fill (the only place a real highlight gradient is justified).
- Combat/character vitals may show **segment ticks**.
- Value is a bright number on or beside the bar, not a pale caption.

## 12.2 XP / guild / bag

- Thinner than vitals.
- Gold/amber fill in a dark trough.
- Often under a portrait or in a header.
- No animation required for fidelity.

## 12.3 Durability

- Quietest meter.
- Thin, muted green-metal or dull brass fill.
- Must not scream like health.

## 12.4 What `/dev/ui` does instead

All meters share one inset channel and a **solid unshaded fill**. No sheen, no segments, no species difference except color and height. Health uses `#b4453a` on a mid-brown page — less trough, less blood, more “UI bar.”

---

# 13. Rows

Market listings, inventory list mode, stat lines, notification lines, filter results.

## 13.1 Rules

A row is a **line in a ledger**, not a card.

- Full width of the parent well.
- Separated by a hairline, not by a gap + four-sided border.
- Icon (optional) left, aligned to a column.
- Primary text; secondary/meta trailing or under.
- Hover: slight lift in luminance, not a new gold plate.
- Selected: see §6.4 — rim / left metal / faint wash, still one row in the table.
- Rarity colors the **name and icon**, not the row background.

Stat rows (equipment combat stats) are even tighter: label muted, value bright, almost no chrome.

## 13.2 What `/dev/ui` does instead

`.ui-row` is an inset **plate**: padding, radius, four-sided border, 2px left accent, gap between siblings. A list of these looks like stacked SaaS tickets. That is the opposite of the market/activity ledgers in the references.

---

# 14. Activity rows

Observed on the right rail (`Recent Events`, `Claimable Rewards`, `Notifications`, `Alerts`) and in combat/chat logs.

## 14.1 Structure

Same on every screen:

1. small leading icon (painted, not a rounded app glyph);
2. one primary line (what happened);
3. optional secondary / category;
4. trailing timestamp **or** a compact action (`Claim`).

Rows share a **continuous rail**, divided by hairlines. Section headers above a group are small bronze caps (`RECENT EVENTS`), sometimes with a functional tint on the **header** (Alerts reads red) rather than on every row.

## 14.2 Variant language

Variants are **icon + a few words of color**, not a tinted card:

| Kind | Cue |
| --- | --- |
| Normal | default silver line |
| Reward / claimable | gold number or gold icon; action button on the right |
| Warning / alert | red header or red icon; not a pink row wash |
| Market | coin icon; gold price |
| Combat / PvP | weapon icon; damage numbers in red/white |
| System | quieter, sometimes blue name |
| Completed | faded primary, no loud green plate |

## 14.3 What `/dev/ui` does instead

`.ui-activity-row` reuses the generic bordered plate and differentiates with left accent + faint tint. The rail becomes a stack of colored tickets. The references use a newspaper/log, not tickets.

---

# 15. Ornament usage

## 15.1 What the references actually use

Industrial medievalism:

- straight frames;
- small **corner brackets** or brighter corner pips on major workspace frames;
- occasional diamond / winged / crest mark in a header (location shield, guild crest, loadout helm);
- circular rune / zodiac **behind** the equipment doll (content art, not a repeating CSS ornament);
- hairline rules that may fade at the ends.

There is **no** scrollwork library, no filigree corners on every panel, no ornamental divider between every section.

## 15.2 Rules

- Ornament marks a **major frame or a chapter break**, not a Section by default.
- Corner brackets belong on the workspace and other primary frames, at low opacity bronze.
- Crests and painted marks are content.
- If a screen has more than a handful of ornaments, it is decorated, not designed.

## 15.3 What `/dev/ui` does instead

Four opt-in SVGs (divider, corner, diamond, accent) exist and are the right *kind* of restraint. They are not yet the metal-bracket language of the mockups, and the showcase treats them as a catalog, not as frame hardware.

---

# 16. Density / spacing

## 16.1 Screen density

The references are **high-density desktop MMORPG chrome**.

- Almost every region carries information.
- Gutters between major panels: about **8–12px** of page ground.
- Inner padding is tight (8–16px class), larger only around hero art and the doll.
- Negative space is **structural** (gutter, column, art stage), not lifestyle whitespace.

Equipment is the only screen that “breathes,” and it breathes **around the doll**, not around every label.

## 16.2 Internal density

| Region | Density |
| --- | --- |
| Nav, activity, chat, market table | highest — compact sans, tight row pitch |
| Stat ledgers | highest — label/value columns |
| Inventory grid | high — small slots, thin gutters |
| Inspector / selected item | medium — still a ledger, not a blog card |
| Hero / combat scene | low — art needs air |

## 16.3 Office Mode

Office Mode must **keep this architecture** and remove art/animation (`PHASE_3_SPEC.md` §10). It may tighten spacing further. It must not become a SaaS admin theme (flat gray, no metal, no grain).

## 16.4 What `/dev/ui` does instead

The showcase is a documentation page: max-width ~72rem, `24px` chapter gaps, large section titles, swatches with air. That is acceptable for a catalog, but it trains the eye on the wrong density. The **primitives** also pad like cards (rows, tabs, primary buttons), so even a dense screen built from them will look sparse and boxed.

---

# 17. Current UI problems

Comparison target: `/dev/ui` + `tokens.css` / primitive CSS, not a vague “needs polish.”

These are the concrete reasons the engine looks cheaper than `docs/mockups/*`.

### 17.1 Page is too light and too brown

`--color-page-bg: #12100e` is already in the **panel** luminance of the references (`#12110f`). Gutters cannot read as void. The whole page becomes one warm slab.

### 17.2 Surface contrast range is too narrow

Current ladder: inset `#100e0c` → page `#12100e` → base `#161310` → raised `#1c1714`.  
That is roughly one material. References jump from ~`#080b0b` ground to ~`#12–16` chrome to ~`#1d–21` raised plates. Without that range, inset wells and raised plates are theoretical.

### 17.3 Surfaces are too flat and share one recipe

Every variant is “hex + 5% luminance gradient + 8% top highlight.” There is no page grain, no metallic edge variation, no local vignette, no slot hollow that reads as thickness. The finish is a polite dark card, repeated.

### 17.4 Gold is jewelry-yellow, not aged metal

`--color-gold-normal: #c9a227` and `--color-gold-strong: #e4c56a` sit far toward lemon brass. Reference metal is dusty rose-bronze (`#634b33` … `#a98b75`). Yellow gold on brown cards is the generic “fantasy skin” look.

### 17.5 Gold is used as fill, not as material hierarchy

Primary buttons, selected surfaces (`gold 22%` wash), active tabs, and interactive hovers all dump gold into the **face**. References keep gold on **edges, numbers, and rare marks**. Fill-gold is F2P CTA language.

### 17.6 Borders are too uniform

Four sides, one opacity, one hue, no top/bottom lighting, no inner/outer pair, no corner pips. Selected only raises opacity of the same yellow. The frame cannot look forged.

### 17.7 Missing local highlights / focal lighting

No hero vignette language in the engine, no icon-back glow, no glassy meter sheen, no brighter metal on the top rim that differs from the bottom rim. Lighting is even, so nothing is precious.

### 17.8 Typography roles are flattened

- Section headings use primary cream at 1rem — they shout.  
- Page headings are smaller and less inscribed than the mockups.  
- Display uses `#e4c56a`.  
- Body `#e8e0d4` is warmer and brighter than the reference silver-parchment.  
- Section labels are not bronze small-caps.

Result: everything is “a title,” nothing is a stamped plate label.

### 17.9 Primary button is a gold lozenge

Dark text on mixed `#c9a227` is the single cheapest control. It does not appear as the default CTA in the references.

### 17.10 Tabs read as chips

Gold wash + gold underline = capsule tab. References are inscription tabs on a frame.

### 17.11 Rows and activity rows are ticket cards

Bordered, radiused, accent-barred, gapped plates. References are hairline ledgers. This one mismatch will make Market, Activity, and Notifications look like a web app even if colors improve.

### 17.12 Meters are flat pills of paint

Solid fill, no trough drama, no segment ticks, no fill highlight. Vitals cannot feel like blood/oil in a channel.

### 17.13 Semantic colors are dashboard-pastel

`#7dba7a`, `#7fb3d5`, `#c45c4a`, rarity pastels on chip backgrounds. References use deep dried-blood red, forest green, and rarity **on the item**, not as bootstrap badges.

### 17.14 Badges are chips, counters should be wax seals

Notification counts in the refs are **small saturated red discs, white numerals**. The engine’s `.badge` / `.status-badge` / `.rarity` share compact-chip geometry and muted washes. Status chips are a product of the token set, not of the mockups.

### 17.15 Radius and shadow leftovers still say “web component”

Legacy `--radius: 10px`, `--shadow: 0 18px 40px`. Office Mode then **deletes** outer shadows entirely. The refs need **short** shadows on floating plates, not “big shadow or none.”

### 17.16 Showcase density trains the wrong eye

`/dev/ui` is a wide, airy catalog. It can stay a catalog, but it must not be mistaken for the visual target. The target is the five full-screen shells.

---

# 18. Visual rules that MUST be preserved

These are non-negotiable for later UI tasks. Breaking one of them reopens the “cheap” gap.

1. **Mockups outrank tokens.** If a token fights `docs/mockups`, change the token in a later task — do not “fix” the mockup.
2. **Page is darker than chrome.** Gutters must read as void.
3. **Five surface levels stay distinct:** page, inset, base, raised, floating.
4. **Metal is an edge, not a fill.** Bronze default, gold selected/valuable.
5. **Aged brass, not lemon gold.**
6. **Light comes from above.** Top highlight, bottom contact, inset troughs.
7. **One focal light per screen.** Art and selected objects; chrome stays darker.
8. **Inscription serif only for brand and page titles.** Never on buttons, tabs, inputs, rows.
9. **Section labels are small bronze caps.** They do not use primary text color.
10. **Body text is silver-parchment, not white and not warm cream.**
11. **Rows are ledgers.** No per-row card chrome.
12. **Buttons are dark plates with metal rims.**
13. **Tabs are text on the frame.**
14. **Inputs and slots are wells.**
15. **Vitals are troughs with material fill;** XP is a thinner gold trough.
16. **Rarity lives on name + icon halo**, not on row fills.
17. **Notification counts are red discs.**
18. **Ornament is rare frame hardware**, not a section default.
19. **Density is desktop-MMORPG.** 8–12px panel gutters; Office Mode tightens, it does not SaaS-ify.
20. **No glassmorphism, no pill radius on chrome, no glow-as-style, no second visual system for combat.**

---

# 19. Anti-patterns

Do not introduce or keep these, even if they are already in `/dev/ui`:

- Flat page hex with no grain and no darker gutter.
- One brown used for page, panel, and well.
- Solid gold / yellow primary buttons with dark text.
- Gold wash as hover, selected, and active-tab fill.
- Uniform 1px border on all four sides with no lighting.
- Bright `#c9a227` / `#e4c56a` as default metal.
- Large drop shadows under every panel.
- `backdrop-filter` glass panels.
- Pill chips for tabs, badges, and filters.
- 8–10px radius on game chrome.
- Cinzel / Cormorant on controls.
- Per-row bordered cards for market, activity, or notifications.
- Pastel semantic fills (mint, sky, coral) on chrome.
- White `#fff` body text.
- Pure black `#000` flat page with no grain.
- Filigree / scrollwork kits, medieval ornament spam.
- Lighting every panel to the same brightness as the hero art.
- Treating `/dev/ui` spacing and catalog layout as the game density target.
- Inventing a “Combat 3.0” or “Inventory 3.0” look that is not this shell.

---

# 20. Acceptance criteria

A later visual pass is acceptable only when **all** of the following are true. Compare against the five mockups, not against “more polish.”

## 20.1 Screen-level

- [ ] A full-screen composition using the engine reads as the same shell as `main.png` (top bar, nav, workspace, chat, activity).
- [ ] Page gutters are visibly darker than panels.
- [ ] One focal atmospheric region exists; the rest of the chrome is darker and quieter.
- [ ] Panel gutters stay in the 8–12px class at normal density.
- [ ] Combat still uses this language (scene + HUD), not a new kit.

## 20.2 Material and color

- [ ] Default edges read bronze; selected edges read brighter gold-bronze; fills stay dark.
- [ ] Gold is not the primary button fill.
- [ ] Metal is aged (dusty brass), not lemon jewelry.
- [ ] Functional red/green are deep and desaturated.
- [ ] Rarity is on item identity, not on row backgrounds.

## 20.3 Type

- [ ] Page titles are inscriptional, large, tracked caps.
- [ ] Section labels are small bronze caps.
- [ ] Body is silver-parchment sans.
- [ ] Numbers out-read adjacent labels.
- [ ] No display serif on buttons, tabs, fields, or rows.

## 20.4 Controls

- [ ] Buttons are dark metal plates; danger is a red-brown plate, not a scarlet pill.
- [ ] Tabs are frameline text; active has metal emphasis, not a gold chip fill.
- [ ] Inputs/selects are inset wells with bronze chevrons/icons.
- [ ] Tooltips/inspectors are floating dark frames with optional local icon light.
- [ ] Vitals show trough + fill highlight (and segments where the mockups do); XP is thinner gold.
- [ ] Counters are red discs with white numerals.
- [ ] Generic rows and activity rows are hairline ledgers, not ticket cards.

## 20.5 Lighting and ornament

- [ ] Inset troughs and top highlights are visible without glow.
- [ ] Floating plates use short dark shadows, not 18–40px haze.
- [ ] Ornaments appear only as rare frame hardware or content crests.

## 20.6 Process

- [ ] `/dev/ui` remains the implementation sandbox, but visual QA of fidelity is done against `docs/mockups/*.png`.
- [ ] No gameplay architecture change is required to meet this spec.
- [ ] Office Mode preserves information architecture and this material language at higher density.

---

## Related documents

- `docs/mockups/main.png`
- `docs/mockups/equipment.png`
- `docs/mockups/inventory.png`
- `docs/mockups/market.png`
- `docs/mockups/combat.png`
- `docs/PHASE_3_SPEC.md` §9–10
- `docs/ui/DESIGN_TOKENS.md` (implementation tokens — subordinate to this spec)
- `docs/ui/UI_ENGINE_AUDIT.md` (architecture baseline)
