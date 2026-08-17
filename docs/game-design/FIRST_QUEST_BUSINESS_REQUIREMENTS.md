# First quest — business requirements and microscript

**Status:** Approved game-design / business requirements  
**Date:** 2026-08-16  
**Quest (live code to rework):** `QST_MILITIA_NOTICE`  
**NPC:** Watch-Sergeant Bren (`MILITIA_OFFICER`), City Square  
**Follow-up (out of this file’s copy):** `QST_ARM_THE_WATCH` becomes the Edric **upgrade** beat, not the first family choice  
**Governs:** first-quest start, kit grant, objectives, copy, **first-iteration UI**, acceptance  
**Related:** `docs/game-design/WEAPON_FAMILIES_AND_STARTER_QUEST.md`, `docs/game-design/LOCATIONS_SCREEN.md` (where Bren’s host plate lives)  
**Journey:** `docs/game-design/LEVEL_1_10_PLAYER_JOURNEY.md` (chapter spine; this file owns Level 1 quest copy and rules)

Copy language: **English** (matches live NPC/quest strings).  
This file does not implement code.

---

## 1. Business goal

The first quest must do three jobs in one short session (target **5–12 minutes** of active play):

1. **Atmosphere** — Greyhaven is a working city that is not safe; the watch is thin; the player is a volunteer, not the chosen one.  
2. **Loop** — SAFE hub → DANGEROUS location → Search → fight or leave → return.  
3. **Identity** — player leaves with a **chosen rusty melee kit**, issued by the watch.

It must **not** be a silent fetch (go, click, loot) and must **not** be a lore dump or branching CRPG.

**Thesis the player should be able to repeat:**

> The gates still open. That is habit, not safety. Old Town is already eating people. Bren gave me rust and asked me to come back.

---

## 2. Actors and places

| Actor / place | Role |
| --- | --- |
| Player character | New arrival, name only, no class |
| Watch-Sergeant Bren | Quest giver, kit issuer, turn-in; face of the militia chapter |
| City Square | SAFE hub; quest starts here |
| Old Town | First DANGEROUS location |
| Street Thug | Expected first enemy if Search starts a fight |
| Notice board | Optional flavour / backup; **not** the only start |

---

## 3. Functional requirements

### FR-1 Quest start

1. After character creation, on **first entry into the world**, `QST_MILITIA_NOTICE` (or its successor code) is **ACTIVE**.  
2. The player must not have to discover Bren by accident; tracker and Square UI point at him.  
3. Relog / refresh must **not** replay the opening as a blocking modal. Tracker and Bren remain.  
4. Weapon family is **not** chosen on character creation.

### FR-2 First conversation (kit)

1. The first **Talk** with Bren is the **issue** step. Steel is granted **before** Old Town.  
2. Player chooses exactly one: Sword, Axe, Mace, Daggers.  
3. Grants (exact-once, server-authoritative):

| Choice | Items |
| --- | --- |
| Sword | `RUSTY_SWORD` + `RUSTY_SHIELD` |
| Axe | `RUSTY_AXE` + `RUSTY_SHIELD` |
| Mace | `RUSTY_MACE` + `RUSTY_SHIELD` |
| Daggers | `RUSTY_DAGGER` only — **no shield** |

4. Bow is not offered.  
5. Until dual-wield exists: one dagger and empty off-hand is acceptable; never a shield as “compensation.” If off-hand may hold a dagger, grant two `RUSTY_DAGGER` instances.  
6. Player cannot complete Old Town objectives **and** receive a second rusty kit on turn-in.

### FR-3 Objectives (after kit)

Ordered, all required unless noted:

| # | Type | Target | Player-facing |
| --- | --- | --- | --- |
| 1 | `TALK_TO_NPC` | `MILITIA_OFFICER` | Speak with Watch-Sergeant Bren (includes weapon choice) |
| 2 | `VISIT_LOCATION` | `OLD_TOWN` | Reach Old Town |
| 3 | Search in Old Town | one completed Search | Search the alleys |
| 4 | `TALK_TO_NPC` / turn-in | `MILITIA_OFFICER` | Report back to Bren |

**KILL / defeat is not a required objective.** Combat is the expected outcome of Search, not a gate.

**Retreat** from the first fight still allows objective 3 (Search happened) and objective 4.

If a given Search in Old Town does not start combat, Search still counts. Do not force a hidden “must spawn Thug” unless combat content later requires it; flavour copy must not say the quest fails without a kill.

### FR-4 Spawn and guidance

1. Spawn loadout: Worn Leather, 2× Healing Potion, start gold as live (100 unless Task 05 changes it), **no proper weapon** until FR-2.  
2. Recommended path does not send the player to Market, Forest, Sewers, Mine, Ruins, or Arena to complete this quest.  
3. Travel stays open (journey soft-guide). Quest text never requires those places.

### FR-5 Turn-in

1. Turn-in is Bren on the Square.  
2. Copy **branches** on whether the last relevant Old Town combat ended in **victory** or **retreat**. If there was no combat, use the **no-fight report** line.  
3. Rewards besides the kit: XP/gold as data (live notice currently 40 XP / 15 gold / potion). Kit is the **product** reward; do not also grant a duplicate rusty weapon. Extra potion is allowed.  
4. Completing this quest may unlock `QST_ARM_THE_WATCH` as **optional upgrade** (Edric), not as “you still have no weapon.”

### FR-6 Tone and content rules

1. Tone: local, grim, practical. Watch, alleys, rust, thin patrols.  
2. One **foreshadow** of the north road / overdue wagons — a clause, not a briefing.  
3. Forbidden: chosen one, ancient blood, continent war, mandatory moral dilemma, 800-word intro, class names on weapon choices.  
4. Player may skip reading flavour; **location blurbs** still carry atmosphere if Talk is mashed through.

### FR-7 Presentation (summary)

1. Quest log / tracker use the **tracker strings** in §4.8, not raw `KILL 0/1`.  
2. Weapon choice is in-world (Bren Talk), with the four lines in §4.4.  
3. Combat log does not need to quote Bren.  
4. Binding first-iteration UI: **§5**. Bren as Square **host** on Locations: `LOCATIONS_SCREEN.md`.

---

## 4. Microscript (canonical copy)

Speaker is **Watch-Sergeant Bren** unless noted. Keep line breaks as playable paragraphs (one bubble ≈ one paragraph).

### 4.1 City Square — location (always available)

**Square blurb (short, on location panel):**

> Greyhaven’s heart still pretends it is morning. Gates open. Bells ring. The watch is thinner than the crowd.

**Notice (optional board / quest log description):**

> The watch needs eyes in Old Town. Report to Watch-Sergeant Bren in the Square. Rust will be issued. Come back breathing.

### 4.2 Bren — idle greeting (Square, any time)

> The watch has work, if you can follow an order.

*(Replaces the live “if you can follow a notice” once this quest is the order, not a flyer-only start.)*

### 4.3 Node A — first Talk, offer (before kit)

**Bren:**

> Greyhaven still opens the gates at dawn. That is habit, not safety.  
> Old Town has been eating drunks and runners. We are thin. I am not asking for a hero. I am asking for a pair of eyes that come back.  
> The north road has gone quiet again — wagons overdue, same as last week. That is tomorrow’s problem. Tonight I need the alleys walked.  
> There is rust on the rack. Better rust in your hand than an empty one.

**Player replies (UI):**

| Id | Label | Result |
| --- | --- | --- |
| A1 | I’ll walk Old Town | → Node B (weapon rack) |
| A2 | Why me? | → Node A2, then back to A1 / A3 |
| A3 | Not now | Closes Talk; quest stays ACTIVE; kit **not** granted |

**Node A2 — Bren:**

> Because you are standing here and I am short of living names. The watch does not wait for better volunteers.

No other lore. Return to A1 / A3.

### 4.4 Node B — weapon choice (kit grant)

**Bren:**

> What can you hold?

**Player replies:**

| Id | Label | Bren confirm (one line) | Grant |
| --- | --- | --- | --- |
| B1 | Sword | Then you stand, and you answer. Take the shield. It is as tired as the blade. | Sword kit |
| B2 | Axe | Then you finish it. The alley does not want a duel. | Axe kit |
| B3 | Mace | Then you knock sense through whatever they wear. | Mace kit |
| B4 | Daggers | Then you keep both hands busy. No shield. You will feel every mistake. | Dagger kit |

**Bren, after grant (all families):**

> Old Town. Search the alleys. If steel finds you, use it — or come home without pride. Alive is a report. Go.

Quest objectives 2–3 become the tracker focus. Market is not mentioned.

### 4.5 Old Town — location (on arrival)

> The Square’s noise dies in a lane. Wet stone, cheap ale, someone else’s blood already dry. The watch does not own this street after dark. They barely own it at noon.

### 4.6 Search — flavour (first Search while quest ACTIVE)

Show **one** of these (rotate or pick first); do not stack.

- > A shutter slams. Someone ran. The alley pretends it was the wind.  
- > You find a dropped cap, still warm. No owner.  
- > Boots at the corner. Not in a hurry to hide.

If combat starts, enemy is **Street Thug** for the recommended first fight. No extra mid-combat dialogue.

### 4.7 Node C — turn-in Talk

**If player has not finished Search:** *(progress)*

> The notice still stands. Old Town. Walk it. Then my desk.

**If Search done, victory in the last Old Town fight:**

> You came back louder than you left. That will do.  
> Keep the steel. If the north road stays quiet, we will talk again.  
> When the rust starts to embarrass you, Edric in the Market sells things that come back with you. That is not an order. Not yet.

**If Search done, retreated from that fight:**

> Alive is a report. The alley will still be there tomorrow.  
> Keep the steel anyway. I need you walking, not proud.  
> Edric in the Market can put a better edge in your hand when you are ready. The rust stays honest until then.

**If Search done, no combat occurred:**

> You walked it and came back. Most people only do the second part.  
> Keep the steel. Old Town is not finished with anyone.  
> The Market can wait. The alleys will not.

**Player reply:** “I’ll remember” / close. No second kit.

Unlock follow-up quest to Edric **without** implying the player is still unarmed.

### 4.8 Quest log strings

| Field | Copy |
| --- | --- |
| Name | Issued Steel |
| Description | The watch is thin. Bren will put rust in your hands and send you to Old Town. Come back. |
| Offer (log) | Greyhaven’s gates still open. Old Town does not care. Take rust from Bren, walk the alleys, report. |
| Progress | Bren is waiting on a report. Old Town, then the Square. |
| Complete | You came back. The watch noticed. The rust is yours. |
| Tracker after kit, before Old Town | Reach Old Town |
| Tracker in Old Town | Search the alleys |
| Tracker after Search | Report to Bren in the Square |

Live code name `Militia Notice` may remain as `code`; **display name** is **Issued Steel**.

---

## 5. UI requirements (first iteration)

Reuse the live quest chrome. **Do not** ship a new HUD, cinematic, or dialogue engine.

Live pieces to extend, not replace: `NpcDialogue` (People here + Talk + `!` / `?` / `…` badges), `QuestTracker` (max 3), `QuestLogPanel`, location panel blurb, `QuestCompleteSummary`, Equipment empty slots.

### 5.1 In scope vs out (this iteration)

| Ship | Do not ship |
| --- | --- |
| Auto-track Issued Steel; Bren badge; Talk nodes as existing action buttons | Branching CRPG tree, voice, history log, letterboxing |
| Four weapon actions + one-line kit consequence | Illustrated weapon-card carousel / compare DPS sheet |
| Location + Search flavour lines from §4 | Separate notice-board UI (board copy may sit in quest log only) |
| Honest empty main-hand until kit | Fake equipped Rusty Sword |
| Item tooltip: weapon `min–max`; shield soak `1–2` | Combat-log “block rolled 2”; armour-rating fork |
| Quest Complete with **granted** items | Showing all four rusties as rewards before choice |
| Relog without blocking modal | Hiding Market / Arena / Craft from nav |
| Tracker copy from §4.8; hide `1/1` noise | Click-to-pathfind / map ping (nice-to-have later) |

Office-first: Talk is the existing panel, not a fullscreen takeover. Skipping flavour = pressing the next action. No session-start modal.

### 5.2 First load (City Square)

1. Issued Steel is **ACTIVE** and **tracked** without a player Track click.  
2. No modal, toast wall, or forced Talk overlay on load or refresh (AC-2).  
3. Tracker shows name **Issued Steel** and the current §4.8 line (first: speak with Bren / equivalent of objective 1).  
4. On Square, Bren in People here shows badge **`…` (ACTIVE)** until kit+Search are done; then **`?` (TURN_IN)** when ready to report. Do not leave him with **`!` (AVAILABLE)** if the quest is already ACTIVE — that implies Accept of an unstarted quest.  
5. Character creation UI unchanged (name only).

### 5.3 Talk flow (Bren)

Sequential nodes in **one** Talk panel (`npc-talk-text` + action buttons). Labels = §4.3–4.4.

| Node | Buttons | Notes |
| --- | --- | --- |
| A | I’ll walk Old Town · Why me? · Not now | Not now = CLOSE, quest stays ACTIVE, **no kit** |
| A2 | I’ll walk Old Town · Not now | After Why me?; no extra lore button |
| B | Sword · Axe · Mace · Daggers | Primary actions. **No Bow.** |

Weapon row must communicate the off-hand **before** confirm (subtitle or muted line under the button is enough):

- Sword / Axe / Mace: `Rusty weapon + shield`  
- Daggers: `No shield`

Do not open Market from this Talk. No Shop action on Bren.

After B grant: Talk shows Bren’s “Old Town. Search…” paragraph. Actions: Close (or equivalent). Equipment/inventory queries refresh so the slot fills without a full reload.

If the player Talks again before Old Town: do not replay Node B; short reminder (progress text) + Close.

### 5.4 Tracker and Quest Log

1. Binary objectives (`requiredAmount` 1): show **display text only**, not `Reach Old Town 0/1`.  
2. After kit, tracker lines follow §4.8 in order. Ready to turn in: live pattern `Return to Watch-Sergeant Bren` is acceptable if it matches READY_TO_TURN_IN.  
3. Quest Log **ACTIVE**: name Issued Steel, description §4.8, current objective, recommended location **Old Town** then **City Square**.  
4. **Reward preview before kit:** XP + gold (+ potion if granted). Item line: `Rusty kit — chosen with Bren`, **not** a list of all four weapons. After grant: preview may name the actual items.  
5. Completed tab uses complete string §4.8.

### 5.5 World and Search

1. Square location panel uses §4.1 blurb.  
2. Old Town panel uses §4.5 on arrival.  
3. First Search while quest ACTIVE: one flavour line from §4.6 on the Search result / location feedback — not a second modal.  
4. First iteration does **not** grey out other travel actions. Tracker is the guide.  
5. Combat UI unchanged; no Bren quotes in the combat log.

### 5.6 Equipment and tooltips (after grant)

1. Until Node B: main-hand **empty** (and off-hand empty). Do not render a ghost sword.  
2. After melee+shield: main-hand rusty weapon, off-hand rusty shield. After daggers: main-hand dagger, off-hand empty **or** second dagger if granted; never a shield.  
3. Weapon tooltip: damage as **`min–max`** (e.g. `4–8`), not a single integer.  
4. Shield tooltip: stable armour if present (`Armor 1`) and **Block 1–2** (or equivalent wording). No `0–3` armour fork.

### 5.7 Turn-in and complete

1. Bren badge `?` when report is available.  
2. Node C body uses victory / retreat / no-fight copy.  
3. Existing **Quest Complete** block: name Issued Steel; XP; gold; potion if any; **the rusty items actually granted**; Unlocked/Next may name Arm the Watch **without** “you are still unarmed.”  
4. One complete summary is enough. No extra congratulations modal on top of Talk.

### 5.8 Failure / edge UI

1. Double-click grant: still one kit (server); UI does not flash two complete banners.  
2. Player walks to Old Town before Bren: tracker still “Speak with Bren”; Search may work (world stays open). Do not block Search with a hard UI lock in this iteration.  
3. Talk errors: existing error pattern; do not swallow grant failure as success.

---

## 6. Acceptance criteria

| ID | Criterion |
| --- | --- |
| AC-1 | New character has this quest ACTIVE on first Square load without extra clicks. |
| AC-2 | Refresh/relog does not pop a blocking intro; quest progress intact. |
| AC-3 | Kit is granted only from Node B, once, matching the chosen family table. |
| AC-4 | Daggers never receive `RUSTY_SHIELD`. Sword/axe/mace always do (with the rusty weapon). |
| AC-5 | Bow is not selectable. |
| AC-6 | Old Town visit + one Search + report completes the quest **without** a recorded kill. |
| AC-7 | Retreat from Street Thug still allows turn-in; complete_text uses the retreat branch. |
| AC-8 | Victory uses the victory branch; no-combat Search uses the no-fight branch. |
| AC-9 | Duplicate HTTP / double turn-in cannot duplicate rusty items (exact-once). |
| AC-10 | Recommended quest text never names Sewers, Mine, Ruins, Arena, or ranked PvP. |
| AC-11 | Character creation cannot select weapon family. |
| AC-12 | After completion, player can ignore Edric and keep rusty; `QST_ARM_THE_WATCH` must not hard-require a purchase to continue the chapter. |
| AC-UI-1 | No blocking intro modal on first load or refresh. |
| AC-UI-2 | Issued Steel is tracked automatically; tracker never shows `0/1` / `1/1` for this quest’s binary objectives. |
| AC-UI-3 | Bren badge is `…` while ACTIVE (not `!`); `?` when READY_TO_TURN_IN. |
| AC-UI-4 | Weapon buttons include shield vs no-shield consequence; Bow is absent. |
| AC-UI-5 | Main-hand is empty until grant; after grant, equipment matches the chosen kit. |
| AC-UI-6 | Quest log does not list all four rusty weapons as rewards before choice. |
| AC-UI-7 | Quest Complete lists the items actually granted. |
| AC-UI-8 | Weapon tooltip shows a damage range; rusty shield shows block 1–2. |

---

## 7. Non-goals

- Full caravan mystery, dungeon, or Veteran.  
- Branching reputation, romance, refuse-the-watch campaign start.  
- Teaching Defend, techniques, attributes, crafting.  
- Rewriting Edric’s full shop dialogue (only the “not an order yet” pointer in Node C).  
- Russian in-game copy (unless a later localisation task).  
- First-iteration UI listed as **Do not ship** in §5.1 (nav hiding, weapon carousel, notice-board screen, combat-log block rolls, pathfind from tracker).

---

## 8. Traceability

| Live (today) | This BR |
| --- | --- |
| `QST_MILITIA_NOTICE` + KILL Street Thug | Rework: no mandatory kill; kit on first Talk; display name Issued Steel |
| Reward: 40 XP, 15 gold, potion | Keep currency/XP/potion if Task 05 agrees; **add** rusty kit; do not leave “potion only” as the fantasy reward |
| `QST_ARM_THE_WATCH` “rust is a habit, buy a weapon” | Re-copy later: rust is already issued; Edric is upgrade |
| Bren greeting “follow a notice” | “Follow an order” |

---

## 9. Open (do not block copy)

1. Search-objective type in the framework (`VISIT` + hook vs new `SEARCH` type). Behaviour in FR-3 is binding; schema is implementation.  
2. Two `RUSTY_DAGGER` vs one — kit doc preference; Bren’s B4 line stays valid either way.
