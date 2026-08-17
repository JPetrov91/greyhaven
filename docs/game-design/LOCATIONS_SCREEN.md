# Locations screen — canonical contents

**Status:** Authoritative game-design spec for the **Locations** panel (`#world`).  
**Date:** 2026-08-17  
**Visual reference:** `docs/mockups/locations-canonical.png` (generated from this spec; not a pixel-perfect contract).  
**Related:** `LEVEL_1_10_PLAYER_JOURNEY.md`, `FIRST_QUEST_BUSINESS_REQUIREMENTS.md`, `WEAPON_FAMILIES_AND_STARTER_QUEST.md`, `docs/mockups/main.png` (Home shell grammar).

This file is the **single** Locations layout. Target: **wide tall place (left) + NPC strip + tall player list (right)**. World Map does **not** appear on Locations.

---

## 1. Job of the screen

**Locations = you are in a place.** Geography, who stands here, field verbs (move, search, board if it exists here).

**Home = staff HQ.** You, gear, tracker, expedition, **hub shortcuts** into systems.

They share **shell**. They do not share **mid-row** and they do not share a cloned action bar.

---

## 2. Shell (same as Home, required)

| Piece | On Locations |
| --- | --- |
| Top bar | Identity, XP, currencies |
| Left nav | **Locations** selected |
| Right rail | `ActivityPanel` unchanged |
| Bottom of **main** | `ChatPanel` — **required**. Live world view omits it; that is a gap. Same channels as Home: Global / Trade / Guild / Party |

Chat is shell, not a location action. Do not hide it. Do not replace Here now with chat.

Guild plate beside chat: optional, same as Home, if density allows. Chat itself is mandatory.

Do **not** put on Locations: character overview, equipment doll, quest tracker card, expedition card.

---

## 3. Visual hierarchy (the actual middle)

Locations has two jobs of equal product weight: **where** you are, and **who** is here.

Vertical stacking on a 16:9 shell **cannot** honour both. Height is already eaten by top bar, chat, and activity. Then:

| Stack | What the player feels |
| --- | --- |
| Huge art on top, people as captions on the paint | Place is real. Players are a foreign HUD. |
| Short art on top, two panels below | Players are real. Art is a Home-style decoration. |

Flipping the **percentage of the same stack** only oscillates between those two. The middle is a **horizontal split**:

- **Left (~68–72% of main):** the location, **tall and wide**. Home’s hero is wide *and short*; this well is wide *and tall*.  
- **Right (~28–32%):** people. A **short** NPC strip on top (horizontal scroll if more than one). **Here now takes the rest of the column height** — the tall census.

Chat is a thin strip under **both** columns. Activity stays the far-right rail.

Field plates (Travel / Search / Notice) sit on the **bottom of the art well** only — not the player roster.

---

## 4. Main workspace — full inventory

```text
┌─ TOP BAR ──────────────────────────────────────────────────┐
├─ NAV ─┬─ MAIN ──────────────────────────────────┬ ACTIVITY ┤
│ Loc.  │ A. HERO (wide+tall) │ B. NPCS (short,   │ feed     │
│       │                     │    horizontal)    │          │
│       │ art · title         ├───────────────────┤          │
│       │ blurb · Safe/PvP    │ C. HERE NOW       │          │
│       │ [Travel][Search]    │    (tall list)    │          │
│       │ [Notice*]           │                   │          │
│       ├─────────────────────┴───────────────────┤          │
│       │ D. CHAT (thin, full main width)         │          │
└───────┴─────────────────────────────────────────┴──────────┘
```

No World Map. Doors in: Home Travel, Home World Map, left nav.

`*` Notice only if this location has a board.

---

### A. Hero — the place

The hero is a **tall, wide well** (~70% of main width, height down to chat). More width than the previous split; still tall so it is not Home’s banner.

**Always:** art, name, blurb, Safe / PvP.

**No:** World Map, Home hub plates, player list or NPC portraits covering the illustration.

**Place verbs** on the bottom of this well only: Travel, Search (if allowed), Notice (if board). Travel opens the destination list as a sheet on this screen.

| Verb | When | What it does |
| --- | --- | --- |
| **Travel** | Always | Opens the destination list (sheet or slide-over **on this screen**). List is the source of truth. Home Travel only navigates here and opens that list. |
| **Search** | `SEARCH_ENCOUNTER` | Same encounter start as today |
| **Notice** | `NOTICE_BOARD` | Same board workspace as Home Notice |
| **Enter dungeon / Arena / Yard** | That action exists | Existing panels; may replace mid content, hero can stay |

Default view: **art + three plates**, not a permanent full destination grid. Opening Travel reveals destinations without leaving Locations.

**Craft / Market / Expeditions:** left nav, not extra plates on the painting.

---

### B. NPCs — horizontal strip

Top of the **right column**, **short** (one card-row tall). Must not steal Here now’s height.

**All NPCs currently at this location** live here — not a single locked “host” plus a hidden overflow menu.

| Element | Rule |
| --- | --- |
| Card | Portrait, name, title; quest mark on frame (`!` / `…` / `?`, one per NPC) |
| Click / Talk | Opens `NpcDialogue` for that NPC |
| Order | Featured first (Square: Bren), then others at this location |
| One NPC | Show that card; no fake empty slots |
| Several NPCs | **Horizontal scroll** (snap to cards). Peek of the next card so scroll is obvious |
| None | Hide the strip; Here now uses the full right column |

Do not put NPCs and players in one list. Do not put this strip on the painting.

Quest marks: not on Here now, not on the Notice Board. Issued Steel: Bren `…`, never `!`.

**Who appears (v1, examples)**

| Location | NPCs in the strip |
| --- | --- |
| City Square | Bren first; others if present |
| Market | Edric, Mara, Calia, Tomas — scroll |
| Tavern | Ohlan; others if present |
| Arena / Yard | Vesk |

---

### C. Here now — players

**Most of the right column’s height** (everything under the NPC strip). Tall census: many rows before scroll.

| Element | Rule |
| --- | --- |
| Header | HERE NOW · count |
| Row | Small avatar, name, level |
| Click | Inspect (existing public character) |
| Cap | Live truncation + “Showing the first N” |
| Empty | “The square is quiet.” (wording may follow location name) |
| Quest marks | None |

Do not mix Bren into this list. Do not show the local player in the list.

---

### D. Chat

Same `ChatPanel` as Home. Thin strip under the **whole** main split (art + people). Must not be as tall as the hero.

---

## 5. Overlays and modes (opened from Locations)

| Mode | How | Notes |
| --- | --- | --- |
| Talk | Card in the NPC strip | Same `NpcDialogue` as Home People |
| Notice board | Place verb Notice on Square | Same board as Home Notice **mode** |
| Inspect player | Row in Here now | Existing inspect |
| Sparring / dungeon | Place verbs when at that location | Existing panels; hero may shrink like Home yard mode |

Closing a mode returns to the split layout (or Home if the player opened board from Home).

---

## 6. Home vs Locations (complete)

| Content | Home | Locations |
| --- | --- | --- |
| Job | Dashboard + hub | Standing in the world |
| Location art | Wide **short** banner | **Tall + wide** well, ~70% of main width, height down to chat |
| NPCs | People hub → Talk | Short **horizontal** strip (scroll if more than one) |
| Here now | No | **Tall** list under the strip |
| **Travel** | Shortcut → Locations + destination list | Plate on the art → same list |
| World Map | → Locations | **No button** |
| Tavern | Hub button | No plate |
| Local Market | Hub → Market panel | No plate (nav Market) |
| Notice Board | Hub → board mode | Square place verb → same board |
| Guild Hall | Hub → Guild | No plate |
| Search | No hub plate | Yes if location allows |
| Chat | Bottom | Bottom, required |
| Activity | Right rail | Right rail |

---

## 7. First quest (Issued Steel) on this screen

- Quest ACTIVE + tracked on Home.  
- Locations at Square: Bren first in the NPC strip, mark `…`, Talk = Node A/B.  
- After Search in Old Town: Bren `?`.  
- Notice Board must **not** list Issued Steel (already live).  
- Home People still opens the same Talk so the player is not forced onto Locations for FTUE.

---

## 8. Empty and edge

| Case | UI |
| --- | --- |
| Alone | Here now empty copy; NPC strip still shows if NPCs exist |
| No NPCs | Strip hidden; Here now uses full right column |
| Move in progress | Existing disable / moving state on destinations |
| Nearby truncated | Footer line, not an infinite scroll of 200 |

---

## 9. Acceptance (Locations panel)

| ID | Criterion |
| --- | --- |
| LOC-1 | Locations has top bar, nav, activity, **and chat**. |
| LOC-2 | No Home hub row (Tavern / Market / People / Guild) on Locations. |
| LOC-3 | Destinations list is the Travel source of truth; Home Travel only navigates here. |
| LOC-4 | Square NPC strip shows Bren first; Talk works; Issued Steel uses `…` / `?`. |
| LOC-5 | Here now lists other players only; inspect works; cap respected. |
| LOC-6 | NPCs and players stay separate; NPC strip is short; Here now is the tall remainder. |
| LOC-7 | Search available only where the location allows encounters. |
| LOC-8 | Hero is a **tall wide** left well (~70% width). |
| LOC-9 | No World Map button on Locations. |
| LOC-10 | Two or more NPCs: horizontal scroll with a peek of the next card. |

---

## 10. Rework from live

Live Locations: full action dump + nearby names, **no chat**, Talk buried. Live Home: location hero with many actions.

1. Add chat to Locations.  
2. Split nearby vs host; Bren plate on Square.  
3. Stop rendering Home-style hub plates on Locations; keep destinations + field verbs.  
4. Home Travel / World Map → `#world`.  
5. Home keeps Tavern, Market, People, Notice, Guild.

---

## 11. Rejected

- Default **vertical stack** (banner art, then people) — that is Home’s grammar  
- Overlaying Here now on the painting  
- World Map button on Locations  
- Locations without chat  
- Two different Travel UIs  
- Deleting Home’s hub bar because Locations exists  
- Cloning that hub bar onto Locations  
- NPC gallery on Home
