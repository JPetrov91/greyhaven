# Starting experience — business specification

**Status:** Authoritative business / product spec for the **first session** (create → Issued Steel close)  
**Date:** 2026-08-18  
**Does not implement code.**

This file is the **single business spec** for how a new character enters Greyhaven. Detailed copy, kit numbers, and Locations chrome live in the files below; if they disagree on *door, pace, or clicks*, this file and `FIRST_FIVE_MINUTES.md` win.

| Detail lives in | Owns |
| --- | --- |
| `FIRST_FIVE_MINUTES.md` | Prologue text, highlight recipe, minute clock, FTUE AC |
| `FIRST_QUEST_BUSINESS_REQUIREMENTS.md` | Issued Steel microscript, objectives, kit grant table, quest AC |
| `WEAPON_FAMILIES_AND_STARTER_QUEST.md` | Family identity, rusty ranges, shield soak |
| `LOCATIONS_SCREEN.md` | Home vs Locations, Talk dock, idle inventory |
| `LEVEL_1_10_PLAYER_JOURNEY.md` | Chapter 1–10 spine after this session |

**In-game copy language:** English.

---

## 1. Business goal

The first session must do four jobs:

1. **Chapter** — the player knows they entered a story (militia / open gates), not a loadout menu.  
2. **Place** — they see City Square as a city before anyone talks.  
3. **Loop** — SAFE hub → person → rusty kit → DANGEROUS location → Search → fight or leave → return.  
4. **Identity** — they leave with a **chosen** rusty melee kit issued by the watch.

They must **not** be thrown into Talk by a quest click, a modal, or an auto-dialogue.

**Thesis the player should be able to repeat:**

> I came in late. The watch is thin. Bren gave me rust and sent me to Old Town. Alive is a report.

Target active play for a complete Issued Steel: **5–12 minutes**. The first five minutes do **not** require a kill or a turn-in.

---

## 2. Scope

### In

- Name-only character create  
- Chapter 1 prologue (once)  
- First landing on Locations · City Square · idle  
- Lead to Watch-Sergeant Bren (highlight + Talk verb)  
- Issued Steel (`QST_MILITIA_NOTICE` rework): talk, kit, visit Old Town, one Search, report  
- Talk chrome = same Locations frame (`LOCATIONS_SCREEN.md` §5)  
- Exact-once rusty kit (server)

### Out

- Voice, film, continent map, class / weapon at create  
- Lost Caravan briefing  
- Forced kill  
- Talk-from-tracker, pathfind, teleport to Old Town  
- Hiding nav  
- Combat 3.0, bow in the first kit  
- Relog replay of prologue or Node A  
- Russian ship copy (localisation later)

---

## 3. Actors and systems

| Actor / system | Role |
| --- | --- |
| Player (`{name}`) | Late arrival. No class. Empty main-hand until kit. |
| Gate watch (prologue only) | Lets them in. Does not give the quest. |
| Watch-Sergeant Bren (`MILITIA_OFFICER`) | Only quest face. Kit. Turn-in. City Square. |
| City Square | SAFE hub. First world screen. |
| Old Town | First DANGEROUS place. Search. |
| Street Thug | Expected first fight if Search starts combat. Not a required objective. |
| Quest tracker / log | Soft-guide. **Aims**, never Talks. |
| Notice board | Must **not** list Issued Steel. |
| Home | HQ. Allowed later. Not first landing. People → Bren is a valid Talk door. |

---

## 4. End-to-end flow (binding)

```text
Create (name)
    → Chapter 1 prologue (once; Skip OK)
    → Locations · City Square · idle
    → Lead: Bren glow + TALK + coach line
    → Player clicks TALK on Bren
    → Node A (work / Old Town / rust promised; no weapons)
    → “I’ll walk Old Town” → Node B (four melee kits)
    → Grant exact-once
    → Bren names Safe vs Dangerous; SAFE chip glows (status)
    → “I’ll walk” → Travel sheet (rule line; Old Town offered as Dangerous)
    → First arrival Old Town: DANGEROUS (status) + Search (verb) together
    → Search (flavour + optional fight; retreat legal)
    → Travel lead home → Square; Bren ?
    → Node C (victory / retreat / no-fight); keep rust
```

Quest is **ACTIVE** on first Square load without extra clicks. Weapon family is **not** chosen at create.

---

## 5. Chapter 1 prologue — BR

| Rule | Spec |
| --- | --- |
| When | Immediately after name submit, **first character only** |
| Relog | **Never** again |
| Skip | Same destination as finish: Square idle |
| Chrome | Full interstitial, dark + type. No HUD. Locations not visible yet |
| Advance | Continue / click / space through 3–4 beats |
| Last action | `Enter the Square` |
| Name | Insert created `{name}` |
| Bren | **Not named** |

**Canonical copy:** `FIRST_FIVE_MINUTES.md` §2.1 (*CHAPTER 1 / THE OPEN GATES*, gate scene).

**Must teach:** late arrival; thin watch; asked for work; Square is next; this is a chapter.  
**Must not teach:** Bren, Talk, weapons, Search, HUD, Lost Caravan.

Forbidden: hooded-destiny eyes, ancient blood, class picker, voice.

---

## 6. First Square — BR

After the prologue (or Skip):

- Screen = **Locations** (`#world`), **City Square**, **idle** (not Home, not Talk).  
- Issued Steel tracked automatically.  
- Shell: top bar, left nav (Locations), activity rail, **chat**.  
- Hero: art, **City Square**, Safe, blurb from Issued Steel §4.1.  
- Coach line (gold, under blurb): *The watch-sergeant will speak if you talk to him.*  
- Tracker: **Issued Steel — Talk to Watch-Sergeant Bren** (no `0/1`).

### 6.1 Lead / highlight (before first Talk or kit)

Visual contracts: `docs/mockups/locations-bren-lead.png`, `docs/mockups/locations-bren-lead-card.png`.  
Recipe: `FIRST_FIVE_MINUTES.md` §3.0.

| Must | Must not |
| --- | --- |
| Bren first; bronze rim + soft gold glow | Dim the painting / nav / chat into a tutorial cave |
| `…` on **frame** | Yellow `!` |
| **TALK** plate on the card (click target) | Portrait-only with no verb |
| Other NPC cards ~35–40% brightness | Giant finger, arrow across the art |
| ~8s idle: glow pulses **once** | Auto-open Talk |
| Tracker click: pulse Bren again | Talk-from-tracker |

Lead **off Bren** when Talk opens. After kit, lead **transfers** (§6.2–6.4). Later Square visits: normal rim, no FTUE glow, no coach line.

### 6.2 Lead transfer (kit → home)

One new law per gesture. Status chips are **not** click targets. Verb plates **are**.

**Highlight types**

| Kind | What | Motion |
| --- | --- | --- |
| **Status** | SAFE, DANGEROUS (and any later non-click callout on a chip/panel) | **Looping pulse** (мерцание): ~1.2–1.6s, glow opacity ~0.4↔1.0, ease-in-out. Soft, not a strobe. |
| **Verb** | Talk, Travel, Search, Go | **Steady** bronze rim + glow. Optional **one** pulse after ~8s idle, then still. |

`prefers-reduced-motion`: status uses a single static glow (no loop). Verb unchanged.

Never make a pulsing chip look like a button (no TALK-style plate on SAFE/DANGEROUS).

| Beat | Status (glow, not a button) | Verb (glow + click) | Coach / chrome |
| --- | --- | --- | --- |
| Bren names the Square vs Old Town | **SAFE** on Square hero | none yet | Mock: `locations-ftue-safe.png` |
| I’ll walk / Travel | off SAFE | **Travel** plate, then sheet | Mock: `locations-ftue-travel.png` |
| First Old Town idle | **DANGEROUS** | **Search** | Mock: `locations-ftue-oldtown.png` |
| After first Search (quest still ACTIVE) | DANGEROUS may stay as normal pill | **Travel** (Square offered) | Return loop |
| Square, ready to turn in | none | Bren `?` (normal Talk) | No starter glow |

**SAFE during Bren’s post-grant speech.** Hero stays visible in Talk. Only the SAFE chip **pulses**. Travel / Search / Bren card do not. Chip does not open a panel. Pulse lasts until Talk closes or the Travel sheet opens.

**I’ll walk** closes Talk and opens the Travel sheet **on this screen**. No teleport.

**Close** instead: Square idle; Travel plate gets the verb glow and subtitle **Travel — leave this place.** Tracker click pulses Travel, not Bren.

### 6.3 First Travel sheet

Header **TRAVEL**. First time only, one rule:

> Leave this place. Arrive in another. You walk; the city stays.

**Old Town** is offered: **Dangerous** badge, line *The watch does not own this street.*, **Go**. Other destinations visible, ~35–40% brightness. Map stays open (soft-guide).

Tracker: **Travel to Old Town**.

### 6.4 First Old Town — Dangerous + Search

Do **not** teach Travel and Search in the same second. Travel is already done.

On first arrival (Issued Steel ACTIVE, Search not yet done):

1. New art, blurb §4.5. **No NPC strip** (Old Town v1 has no NPCs; Here now uses the full right column).  
2. **DANGEROUS** chip **pulses** (status, not a click). Optional: start that pulse ~0.4s before Search glow so the eye reads place then verb. Still **one** beat.  
3. **Search** plate: **steady** rim + glow + subtitle **Search — look here**. Travel / Notice quiet.  
4. Coach: *Dangerous ground. Search can find steel. You stay in this street.*  
5. Tracker: **Search the alleys.** Tracker click pulses Search.

First Search **must** show one flavour line (`FIRST_QUEST` §4.6) **before** combat or empty-alley resolve. Empty Search still counts.

**Law the player should hold:**

- **Safe** — stand, talk, market. Search is not the lesson.  
- **Dangerous** — you are already here. Search is how the street answers.  
- **Travel** — change of place (and of Safe/Dangerous). Not a search.

### 6.5 Return Travel

After Search counts, Search glow off. Travel glows; sheet offers **City Square**. Bren `?` on arrival. No SAFE lecture on the way back.

---

## 7. Click contracts (first session)

| Click | Result |
| --- | --- |
| **TALK / Bren card** | Node A. Only legal start of Talk. |
| **Tracker / quest name / recommended location** | Aim at the **current verb** (Bren → Travel → Search → Bren). No Talk. No auto-travel. |
| **I’ll walk** (after kit) | Close Talk. Open first Travel sheet. SAFE glow off. |
| **Close** (after kit) | Travel plate lead. No teleport. |
| **Travel** | Destination sheet **on this screen**. First time: §6.3. |
| **Go → Old Town** (first trip) | Arrive Old Town idle with §6.4 lead. |
| **Search** on Square | No encounter (SAFE). Do not teach Search here. |
| **Search** on first Old Town | Flavour, then fight or empty alley. |
| **Notice** | Board. Issued Steel absent. |
| **Home / other nav** | Allowed. Do not lock. |
| **Home People → Bren** | Same nodes, plate over dashboard. |
| **Here now row** | Inspect. Not Talk. |
| **Not now** (Node A) | Close Talk. Quest ACTIVE. No kit. Lead may stay until kit. |

If they never Talk: no kit. Do not escalate to a modal.

---

## 8. Issued Steel — quest BR (summary)

Full microscript and item codes: `FIRST_QUEST_BUSINESS_REQUIREMENTS.md`.

| # | Player-facing | Notes |
| --- | --- | --- |
| 1 | Talk to Watch-Sergeant Bren | Includes weapon choice |
| 2 | Reach Old Town | After kit; player Travel |
| 3 | Search the alleys | One completed Search. Kill **not** required |
| 4 | Report back to Bren | Branch: victory / retreat / no-fight |

**Kit (exact-once, Node B only):**

| Choice | Grant |
| --- | --- |
| Sword / Axe / Mace | Matching rusty weapon + `RUSTY_SHIELD` |
| Daggers | `RUSTY_DAGGER` only. No shield. Bow not offered. |

Spawn until Node B: Worn Leather, 2 potions, start gold, **empty hands**.

Recommended path never requires Market, Forest, Sewers, Mine, Ruins, Arena. Travel stays open.

Node A does **not** retell the gate prologue. Starts at Old Town / eyes / rust.

---

## 9. Talk chrome — BR

`LOCATIONS_SCREEN.md` §5.

- Same Locations frame. Hero, Travel/Search/Notice, nav, chat, activity unchanged.  
- NPC strip stays; Bren selected rim.  
- **Here now** becomes dialogue. Close restores Here now.  
- No second “People here” directory.  
- Node B: four plates; subtitle `Rusty weapon + shield` vs `No shield`.  
- Quest Complete inside the dock. No extra modal.

---

## 10. Persistence

| Event | Behaviour |
| --- | --- |
| Refresh / relog | No prologue. No forced Talk. Quest progress intact. |
| Tab close mid-fight | Existing interruption rules. Character not destroyed. |
| Double grant / double turn-in | One kit. Server exact-once. |
| Old Town before Bren | Tracker stays Talk to Bren. Search allowed. No second kit later. |

---

## 11. Acceptance (master)

Quest/item ACs: `FIRST_QUEST_BUSINESS_REQUIREMENTS.md` §6. Pace ACs: `FIRST_FIVE_MINUTES.md` §8.

| ID | Criterion |
| --- | --- |
| SX-1 | After create: prologue once (or Skip) → Locations Square idle. Relog skips prologue. |
| SX-2 | Tracker / log never opens Talk; it aims at the current verb. |
| SX-3 | Talk starts only from Bren (Locations TALK or Home People). |
| SX-4 | First Square shows glow + `…` + TALK + coach line (`locations-bren-lead.png`). |
| SX-5 | Node A has no weapon plates. Kit is Node B after “I’ll walk Old Town”. |
| SX-6 | Kit grant does not change location. I’ll walk opens Travel sheet; it does not teleport. |
| SX-7 | Kill is not required. Retreat and no-fight Search can turn in. |
| SX-8 | Daggers never get rusty shield. Bow absent. Exact-once kit. |
| SX-9 | Issued Steel is not on the Notice Board. |
| SX-10 | Talk uses idle Locations chrome; Close restores Here now. |
| SX-11 | Post-grant Talk: only the SAFE chip **pulses** (not clickable). |
| SX-12 | First Travel sheet: one-line rule + offered Old Town as Dangerous. |
| SX-13 | First Old Town: DANGEROUS **pulses**; Search **steady** glow (`locations-ftue-oldtown.png`). Travel quiet. |
| SX-14 | First Search shows flavour before combat or empty resolve. Search on Square does not teach the verb. |
| SX-15 | Zero NPCs: People Here omitted; Here now uses the full right column. |

---

## 12. Mockups

| File | Contract |
| --- | --- |
| `docs/mockups/locations-canonical.png` | Locations idle |
| `docs/mockups/locations-bren-lead.png` | First Square lead (full frame) |
| `docs/mockups/locations-bren-lead-card.png` | Offered Bren card |
| `docs/mockups/locations-ftue-safe.png` | Post-kit Talk: SAFE chip glow only |
| `docs/mockups/locations-ftue-travel.png` | First Travel sheet; Old Town offered |
| `docs/mockups/locations-ftue-oldtown.png` | First Old Town: DANGEROUS + Search |
| `docs/mockups/main.png` | Home shell grammar |

Lead mocks are **layout / highlight** contracts, not pixel-perfect vs `main.png`.

---

## 13. Traceability (live → this spec)

| Live today | This spec |
| --- | --- |
| Land on Home / world dump | Land Locations Square after prologue |
| Quest click → Talk | Quest click → aim at Bren |
| No chapter framing | Chapter 1 gate prologue, once |
| `QST_MILITIA_NOTICE` + required kill | Issued Steel; Search + report; kill optional |
| Equipped rusty sword at create | Empty hand; kit from Bren |
| Locations without chat | Chat required on Locations |
| Talk as people directory | Talk = Here now swap |

---

## 14. Non-goals

Same as Issued Steel §7 plus: no second Talk layout, no prologue-on-login, no “click quest = skip the city,” no clickable Safe/Dangerous pills, no Search lesson on the Square, no auto-travel.
