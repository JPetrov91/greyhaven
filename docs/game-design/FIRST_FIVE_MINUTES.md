# First five minutes — arrival and Issued Steel pace

**Status:** Authoritative game-design for the **first session after create**  
**Date:** 2026-08-18  
**Owns:** landing screen, click contracts, beat order, what the player learns when  
**Does not own:** Bren copy, kit table, Locations chrome inventory (those stay in their files)  
**Related:** `STARTING_EXPERIENCE_BUSINESS_SPEC.md`, `FIRST_QUEST_BUSINESS_REQUIREMENTS.md`, `LOCATIONS_SCREEN.md`, `WEAPON_FAMILIES_AND_STARTER_QUEST.md`, `README.md`

The first quest stays **short**. The first **minute** must not skip the city.

---

## 1. Problem this file fixes

Issued Steel is a 5–12 minute loop. That is correct.

What felt rushed was the **door**: player lands, does not know the shell, clicks the quest, and is inside Talk. Place never happens. Dialogue reads as a system popup.

Rule:

> After create: **Chapter 1 prologue, once** → **look at City Square**.  
> The game **leads** to Bren (attention + verb).  
> Talk starts only when the player takes that verb.  
> The tracker **never** opens Talk; it **aims** at Bren.

---

## 2. Landing (locked)

After name submit:

1. **Chapter 1 prologue** — once, first character only. Not on relog. Skip allowed.  
2. Then **Locations** (`#world`) · **City Square** · **idle** (not Talk).

Not Home. Home is HQ; it teaches the doll and hub plates. That is minute 6+, or a later tab the player opens themselves.

No auto-Talk. No toast wall. Relog: never replay the chapter card or Node A as a block.

### 2.1 Chapter 1 prologue

A **short scene**, not an encyclopedia and not three taglines. Shape: dusk, gate, name, work, city. Tone: grim, practical. You are a late arrival, not a chosen one.

**Chrome:** full interstitial (dark + type). Locations is not visible yet. `{name}` = the name just created. Advance by **Continue** (or click/space through beats). **Skip** jumps to the Square. One sitting, never on relog.

May break into 3–4 beats (title → road → gate talk → enter). Do not animate a film. Do not voice it.

**Copy (canonical English):**

> **CHAPTER 1**  
> **THE OPEN GATES**
>
> Dusk was already settling on Greyhaven.
>
> Last light hung on the old teeth of the wall when a single traveler reached the gate. The cloak was road-dust. The pack was worn. Nothing about them asked to be remembered.
>
> The watch saw them only when they stopped at the grate.
>
> “Late,” one said. “Name.”
>
> “{name}.”
>
> A finger moved down a list that did not have them. The man shrugged. The watch was thin. Lists were a habit, like opening the gates at dawn.
>
> “Business.”
>
> “Work.”
>
> They looked at each other a moment. Then the watch stepped aside.
>
> “Welcome to Greyhaven. Try not to find trouble before the work finds you.”
>
> {name} went under the arch. The gate came down hard behind.
>
> Ahead, past narrow streets and tavern light, the Square was already waiting.

**Button after the last beat:** `Enter the Square`

Do not add: class, portrait picker, continent map, Lost Caravan, hooded-destiny eyes, ancient blood. Do not name Bren here — the Square does that.

After the button: Square idle with Point + Verb (§3). The prologue answers *how you entered the city*. The Square answers *who to press*.

Bren’s Node A must **not** retell the gate. He starts at Old Town / eyes / rust (`FIRST_QUEST_BUSINESS_REQUIREMENTS.md` §4.3).

---

## 3. How the game leads (first Square)

Seeing Bren is not a lead. A portrait without a verb is furniture.

The game must do three things **on the idle Square**, before any Talk:

| Layer | What the player gets |
| --- | --- |
| **1. Place** | Tall Square. Safe. Blurb. They know they are in a city. |
| **2. Point** | Bren is the offered person: first card, bronze rim already on, others quieter. Badge `…`. |
| **3. Verb** | The card shows **Talk** (label on the card, not a mystery face). Tracker line is the same verb: **Talk to Watch-Sergeant Bren**. Under the Square blurb, one directed line (not a modal): *The watch-sergeant will speak if you talk to him.* |

Lead = **point + verb**. Not auto-open Node A. Not “click the quest and skip the square.”

**Visual:** `docs/mockups/locations-bren-lead.png` (full frame), `docs/mockups/locations-bren-lead-card.png` (card).

### 3.0 Highlight recipe (first Square, before kit)

Do **not** dim the city painting, nav, chat, or Here now into a tutorial cave. Only the NPC strip is ranked.

| Piece | Treatment |
| --- | --- |
| Bren card | First slot. Bronze rim (~2–3px) + **steady** gold glow. Portrait full brightness. |
| Badge | `…` on the **frame** (top-right). Never `!`. |
| Verb | Metal **TALK** plate on the card (bottom). This is the click target. **Steady** glow. |
| Status chips (SAFE / DANGEROUS) | **Looping pulse**, not a steady rim. Not clickable. See starting-experience §6.2. |
| Other NPC cards | ~35–40% brightness, no glow, no Talk plate. Peek still visible. |
| Coach line | Under Square blurb, gold: *The watch-sergeant will speak if you talk to him.* |
| Tracker | Same verb: **Talk to Watch-Sergeant Bren**. |
| Idle ~8s | Glow pulses **once** (intensity, not a bounce). |
| Tracker click | Same pulse again. Still no Talk. |

Off after first Talk opens (or after kit). Later Square visits use normal selected-rim only, no glow, no coach line.

Refuse: giant finger, fullscreen dim, yellow `!`, arrow drawn across the painting, auto-Talk.

### 3.1 If they hesitate

| They do | Game does |
| --- | --- |
| Nothing for ~8s (first Square only) | Bren card pulses once. Directed line stays. Still no Talk. |
| Click tracker / quest name | **Aim, do not Talk.** Pulse Bren, keep the directed line. Log may open *behind* or as a side sheet — the square and Bren stay visible. Never jump into Node A. |
| Click Travel / nav / Home | Allowed. Tracker and Bren `…` remain. When they return to Square before kit, Point + Verb are still on. |

If they never take Talk, kit is not granted. Do not escalate to a blocking modal. One idle pulse is the last shove.

### 3.2 First frame checklist

| Question | Answer on screen |
| --- | --- |
| Where am I? | Tall Square art + name **City Square** + **Safe** |
| What is this place like? | Blurb from Issued Steel §4.1 |
| Who is offered? | Bren singled out (rim + first slot). Not a grey equal among five. |
| What do I press? | **Talk** on Bren’s card. Same words on the tracker. |
| Why him? | Directed line under the blurb + `…` (work in progress, not “new quest”). |
| Is anyone else here? | Here now (or “The square is quiet.”) |
| Is this a chat game? | Chat strip visible, not in the way |

Empty main-hand is **not** on this frame (no doll).

---

## 4. Click contracts (first session)

| Player clicks | Result |
| --- | --- |
| **Bren card / Talk on Bren** | Talk Node A. Only start of the conversation. |
| **Tracker / quest name** | **Aim at the current verb** (Bren → Travel → Search → Bren). Do not open Talk. Do not travel. |
| **Quest Log “recommended: City Square”** | Already here. Aim at Bren. Do not Talk. |
| **Travel** | Destination sheet **on this screen**. Player picks Old Town. |
| **Search** on Square | No encounter (SAFE). Do not start the quest loop here. |
| **Notice** | Board workspace. Issued Steel is **not** on the board. |
| **Home / other nav** | Allowed. Soft-guide only. Do not lock nav. |
| **Home People → Bren** | Same Talk nodes (plate over dashboard). Same kit rules. |
| **Here now row** | Inspect. Not Talk. |

---

## 5. Beat sheet (target clock)

Times are **design targets** for a player who reads and clicks. A masher can finish faster. A reader who opens Home first can take longer. Do not script a timer in code.

Turn-in is **after** this sheet. First five minutes teach **place → person → kit → leave the hub → Search**. Coming back is the close of the *quest*, not of the first five minutes.

### 0:00 — Name

Create: name only. No class, no weapon, no lore crawl.

### 0:00–0:40 — Chapter 1 prologue

**Player does:** reads the gate scene or skips.

**Teaches:** you arrived late; the watch is thin; you asked for work; the Square is next. This is a chapter.

**Does not teach:** Bren’s name, Talk, weapons, Search, HUD.

**Success:** they press **Enter the Square**. Relog never shows this again.

### 0:40–1:00 — Square (idle, led)

**Player does:** sees the city; the UI points at Bren and names **Talk**.

**Teaches:** Locations shell. This is a city. The next verb is Talk to a person here.

**Does not teach:** weapons, Search-as-fight, Market, attributes.

**Success:** they know *who* and *what to press*. They have not been thrown into dialogue.

### 0:45–2:00 — Talk Node A (player-started)

**Player does:** clicks Bren.

**Teaches:** who Bren is; Old Town; you are a pair of eyes, not a hero; rust exists; north road is tomorrow (one clause). Does not retell the gate.

**Weapon row is not on this node.** “I’ll walk Old Town” is the commit. “Why me?” is optional. “Not now” returns to **idle Square** (same frame, Here now back). Kit not granted.

**Success:** they chose to take the walk, or they closed and the city is still there.

### 2:00–2:30 — Node B (kit)

**Player does:** picks Sword / Axe / Mace / Daggers. Sees shield vs no-shield on the plate.

**Teaches:** I have a tool. Daggers are a different contract. The watch issued this.

**Does not teach:** Edric, DPS sheet, bow.

After grant: Safe/Dangerous speech (`FIRST_QUEST` post-grant). SAFE chip glows. **I’ll walk** opens Travel sheet. Tracker → **Travel to Old Town**.

### 2:30–3:15 — Travel

**Player does:** Go on offered **Old Town** (Dangerous).

**Teaches:** Travel = leave this place, arrive in another. Square is Safe; Old Town is not. Other names exist, dimmed.

**Does not:** auto-move on kit grant. Do not pathfind from the tracker.

Sheet rule and mock: `STARTING_EXPERIENCE_BUSINESS_SPEC.md` §6.3 / `locations-ftue-travel.png`.

### 3:15–3:30 — Old Town idle (Dangerous + Search)

**Player does:** sees the lane; DANGEROUS (status) and Search (verb) glow.

**Teaches:** this ground is dangerous; Search looks here; you do not move.

Mock: `locations-ftue-oldtown.png`.

### 3:30–5:00 — Search, then fight or leave

**Player does:** Search.

**Teaches:** this button finds trouble (or an empty alley). First flavour line §4.6. If a fight starts: HP, stamina, Quick Attack, potion, **retreat is legal**.

**Does not teach:** Defend, techniques, mastery, Arena.

If combat is still running at 5:00, that is a good stop. Tab-close must not destroy the character (existing interruption rules).

Victory, retreat, or empty Search all count for the quest. Kill is not a gate.

### After 5:00 — report (quest close, not FTUE close)

Travel back to Square. Bren `?`. Node C. Keep rust. Edric is a pointer, not an order.

A complete Issued Steel in one sitting is **5–12 minutes**. The first five only owe the player: **I know the Square, I spoke to Bren, I have rust, I have left the hub at least once** (or I am in the first fight).

---

## 6. Soft-guide vs railroad

Travel stays open. A player may open Home, Market, or Forest.

| If they wander | UI |
| --- | --- |
| Old Town before Bren | Tracker stays **Speak with Bren**. Search still works. No second kit later. |
| Home first | Doll, empty hand, hub plates. Tracker still does not Talk. People → Bren is valid. |
| Market / Forest first | Allowed. Quest text never requires them. Do not grey the map in v1. |

Do not hide nav. Do not lock Search behind kit (world stays open). The *recommended* path is Square → Bren → kit → Old Town.

---

## 7. What we refuse

- Auto-Talk or Talk-from-tracker  
- Leading with a portrait and no verb  
- Landing on Home as the first post-create screen  
- Voice, film, 800-word crawl, or a prologue that retells itself in Bren’s first line  
- Showing the Chapter 1 card on every login  
- Forced weapon at create  
- Teleport to Old Town on kit grant  
- Clickable Safe / Dangerous pills  
- Teaching Search on the Square  
- Two equal verb-glows on arrival (Dangerous is status; Search is the click)  
- “Click quest = skip the city”  
- Teaching five systems in the first Talk  

---

## 8. Acceptance (pace / door)

| ID | Criterion |
| --- | --- |
| FTUE-1 | After create: Chapter 1 prologue once, then Locations · City Square · idle. Relog skips the prologue. |
| FTUE-2 | Tracker never opens Talk; it aims at the current verb (Bren / Travel / Search). |
| FTUE-3 | Talk Node A opens only from Bren (Locations strip Talk or Home People). |
| FTUE-4 | Node A has no weapon plates; kit is Node B after “I’ll walk Old Town”. |
| FTUE-5 | Kit grant does not change location. I’ll walk opens the Travel sheet. |
| FTUE-6 | Relog does not force Talk or a blocking intro. |
| FTUE-7 | First five minutes do not require a kill or a turn-in. |
| FTUE-8 | First Square idle shows Point + Verb: Bren offered, **Talk** on the card, directed line under the blurb. |
| FTUE-9 | Prologue is the gate scene in §2.1 (or Skip). `{name}` is the created name. No Talk, no HUD lesson, no Bren named. |
| FTUE-10 | Post-grant Talk: only SAFE **pulses** (`locations-ftue-safe.png`). |
| FTUE-11 | First Travel sheet: one-line rule + offered Dangerous Old Town. |
| FTUE-12 | First Old Town: DANGEROUS pulses + Search steady glow; no empty People Here. |
