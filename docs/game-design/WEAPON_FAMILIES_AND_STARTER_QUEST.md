# Weapon families, combat verbs, and the first-quest rusty kit

**Status:** Game-design decision (human + design chat)  
**Date:** 2026-08-16  
**Authority:** Supersedes the listed Level 1–10 journey beats below. Does not implement code.  
**Governs:** first quest rewards, rusty itemization, melee family identity, per-hit weapon ranges, rusty shield block  
**Does not:** implement Combat 3.0, bow/ammo, dual-wield resolution, extra weapon families, or a second combat RNG layer (glance/engine noise) beyond what this file specifies

**Copy / first-quest BR:** `docs/game-design/FIRST_QUEST_BUSINESS_REQUIREMENTS.md`  
**Locations / Talk chrome:** `docs/game-design/LOCATIONS_SCREEN.md`  
**Index:** `docs/game-design/README.md`

---

## 1. What this replaces in the journey

| Journey assumption | Decision here |
| --- | --- |
| Spawn loadout includes **Rusty Sword** equipped; no family choice until Level 2 | Spawn has **no proper weapon**. Family choice is the **first-quest reward** |
| First meaningful build choice = buy at **Edric Varn** (Level 2) | First choice = **first Talk with Bren** (four melee families). Edric sells the **first upgrade**, not the first identity |
| Everyone starts Sword | Nobody starts a family. Sword is one of four rewards |
| Weapon damage is a single integer | Weapons have a **per-hit min–max**. Starter rusties use the table in §6 |
| Wooden Buckler is the early shield story | **Rusty Shield** is the quest off-hand for sword/axe/mace. Buckler remains the merchant upgrade |
| Bow is a Level 2 merchant option alongside melee | **Bow is out of scope** for this kit and for the first quest |

Live catalog today: `RUSTY_SWORD` (damage 6), `OLD_DAGGER` (4), `KNOBBED_CLUB` (7), `WOODSMAN_AXE` (8), `WOODEN_BUCKLER` (armor 2). There is no rusty axe/mace/dagger/shield. Monsters already use `damage_min` / `damage_max` (Street Thug 5–8).

---

## 2. First quest — grant, loop, reward

### 2.1 When it starts

After character creation, on **first entry into the world** (City Square):

1. Quest is **ACTIVE** immediately. Do not require the player to find a hidden NPC.
2. Militia officer on the Square is the face (tracker + UI emphasis).
3. Do **not** re-open a modal on every login. Tracker only.
4. A notice board may exist as flavour / backup, not as the only start.

Creation stays **name-only**. Weapon family is not chosen on the create-character screen.

### 2.2 What it teaches

One thesis:

> You live in a safe city. Danger is the next location. You Search, you may fight or leave, you return. The watch gives you a role and steel.

Do not teach attributes, mastery, player Market, crafting, expeditions, or Arena.

### 2.3 Objectives (keep short: 5–12 minutes)

Recommended shape (**steel, then prove it**). Binding copy and AC: `FIRST_QUEST_BUSINESS_REQUIREMENTS.md`.

1. First Talk with Bren includes **weapon choice**; rusty kit is granted **here**, before Old Town.
2. Visit **Old Town**.
3. Complete **one Search**. Combat (Street Thug) is expected, not required. Retreat still counts.
4. Report to Bren. No second kit.

### 2.4 Spawn loadout (before / without the kit)

- **Worn Leather Armor** (chest), **2× Healing Potion**, start gold unchanged (100) unless Task 05 documents otherwise.
- **No proper weapon** until the quest issues rusties.
- Do not send the player to Market before this quest completes.

### 2.5 Rewards (exact-once, server-authoritative)

Player picks **one** melee family:

| Choice | Items granted |
| --- | --- |
| Sword | `RUSTY_SWORD` + `RUSTY_SHIELD` |
| Axe | `RUSTY_AXE` + `RUSTY_SHIELD` |
| Mace (club) | `RUSTY_MACE` + `RUSTY_SHIELD` |
| Daggers | `RUSTY_DAGGER` only — **no shield**. Off-hand is the second dagger when dual-wield exists; until then do not give a shield “as compensation” |

Do not also require buying the same rusty tier at Edric. Edric’s commons (Militia Shortsword, Woodsman Axe, Knobbed Club, Old Dagger, …) become the **next** step.

Bow is **not** a first-quest option.

XP/gold on this quest: Task 05/07. This file does not retune the 5,580 XP table.

### 2.6 Next beat (unchanged chapter)

After this kit, the journey continues: Old Town unrest → later Edric as upgrade → Forest / caravan spine. Level 2 merchant beat becomes **“a weapon with a better name”**, not “buy your first family.”

---

## 3. Families in scope

**In this document:** Sword, Axe, Mace (club), Daggers.

**Parked:** Bow (two-handed, ammo, separate combat design).

**Do not add** extra families for roster size (rapier, hammer-as-distinct-from-mace, scythe, staff, unarmed, spear). More gameplay comes from **grips inside a family** and from bow later.

**One future candidate only if a new combat noun exists:** spear/polearm (reach / who strikes first). Crossbow only after bow is real. Dual sword/axe is **not** in this scope while daggers own two hits.

---

## 4. Combat verbs (engine by family)

Family = **what you do on a turn**, not a class and not a crit/dodge lock. Do not build Combat 3.0. Use Combat 2.0 statuses and techniques (BLEED, STUN, ARMOR_BREAK, OFF_BALANCE, GUARDED; Riposte / Rending Chop / Crushing Blow / Feint).

| Family | Player question | Wins by | Pays with | Native status / tech |
| --- | --- | --- | --- | --- |
| **Sword** | Who controls the reply? | Counter, GUARDED, stable hits | Mid damage, no “cheat” status | Riposte, control |
| **Axe** | Who wins the blood race? | Pressure, BLEED, highest **white** damage | Holes in defence, stamina on swings | Rending Chop, BLEED |
| **Mace** | How do I break the rule they live by? | Armor, Guard break, short STUN | Slow, little DoT, weaker vs naked HP | Crushing Blow, ARMOR_BREAK, STUN |
| **Daggers** | How do I steal the turn? | Two hits, poison / off-balance, dodge | No shield, fragility, misses hurt | Feint, POISON, OFF_BALANCE |

### 4.1 Grips (second axis — not all required on the rusty kit)

| Grip | Who | Role |
| --- | --- | --- |
| One-hand + shield | Sword, axe, mace | Militia default. Survival vs damage ceiling |
| Two-handed | Same three, **later** | More damage / break; no shield. Two-hand **axe** is the lottery damage ceiling |
| Dual wield | **Daggers only** for now | Two strikes per turn. Do not give sword/axe full dual-strike while daggers exist |

Rusty quest kit: sword/axe/mace = **1H + rusty shield**. Daggers = **no shield**. Two-handers are a later itemization layer, not the first reward.

**Shield-and-board identity must still differ** when all three have a shield: sword answers, axe pressures, mace breaks. If three Street Thug fights feel identical, families are not done.

### 4.2 Raw damage ranking (same grip, no statuses, no crit)

1. **Axe** — highest base (white) damage and best STR scaling  
2. **Mace** — slightly less vs flesh; **better effective** vs armor / GUARDED  
3. **Sword** — middle  
4. **Daggers** — lowest per hit and worst STR efficiency  

A STR “max damage” set is **axe**, and the true ceiling is **two-handed axe** once that grip exists. Mace is not a bigger axe.

Dagger two-hit **sum** must not beat sword/axe white DPS on STR alone. When dual-wield ships, the second hit needs its own pass (weaker die, penalty, or similar). The rusty dagger range in §6 is **per single strike**.

### 4.3 Crit, dodge, and other modifiers

**No hard lock** (daggers ≠ dodge class, sword ≠ crit class, mace ≠ “the damage class”).

- Family = verb and constraints.  
- Crit / dodge / raw damage = **orthogonal** axes (attributes, armor weight, affixes, later techniques).  
- Each family has an **affinity ceiling**, not a ban:

| Family | Natural lean | Valid off-meta | Weak on purpose |
| --- | --- | --- | --- |
| Daggers | Tempo + dodge (no shield); crit **frequency** (two rolls) | Crit/poison as a main dagger build | STR + heavy armour |
| Sword | Control + shield; crit as Riposte payoff | Dodge duelist | Best-in-all-three (crit, dodge, armour) |
| Mace | Per-hit force vs armor; crit as rare slam | Dodge mace (allowed, awkward) | Best dodge in the game |
| Axe | Pressure + white damage; crit and STR both native | — | Better dodge than sword duelist |

Affixes may roll across families. Do not forbid crit on daggers or dodge on maces.

Heavy armour + daggers should feel like a mistake. Light armour + mace should feel like a crutch, not the secret best tank.

---

## 5. Randomness layers (do not stack blindly)

| Layer | What it is | Starter rusties |
| --- | --- | --- |
| **Per-hit weapon range** | How clean this swing was | **Yes** — §6 |
| **Item instance 95–105%** (existing loot roll) | How good this *item* is | **Off** for quest rusties. Canonical forks only |
| **Monster `damage_min`–`damage_max`** | Already live | Keep |
| **Shield block effectiveness** | How well the shield was used this hit | **Yes** — §7. Not an armour-rating fork |
| **Engine extra RNG** (glance, etc.) | Separate future pass | **Not** in this file |

Weapon range is rolled **on the weapon**, then STR is added (`physicalDamagePerStrength: 1.5`; start STR 5 → **+7.5**). Do not put 3–9 on the whole hit including STR.

Distribution for rusties: **uniform inclusive integers**. Bell-curve “mostly average” can wait for the engine pass.

Show the fork on the item (`4–8`). Combat log does not need to print “rolled 6 of 4–8”. The outgoing damage number is enough.

Upgrades (militia / woodsman / iron) should raise **min and max**, not only max.

---

## 6. Rusty weapon per-hit ranges

Shape encodes family. **Do not give every rusty 3–9.**

| Item | Range | Avg | Width | Read |
| --- | ---: | ---: | ---: | --- |
| `RUSTY_DAGGER` | **3–5** | 4 | 2 | Tight, small. Matches live Old Dagger centre 4 |
| `RUSTY_SWORD` | **4–8** | 6 | 4 | Honest. Matches live Rusty Sword 6 |
| `RUSTY_MACE` | **5–8** | 6.5 | 3 | High floor, modest ceiling |
| `RUSTY_AXE` | **4–9** | 6.5 | 5 | Same average as mace; wilder |

**Lottery 3–9 (or 3–10)** belongs to **two-handed axe**, not rusty 1H. Rusty axe **4–9** is already the swingiest starter.

With start STR, before armour, rough outgoing bands:

- Dagger 10.5–12.5  
- Sword 11.5–15.5  
- Mace 12.5–15.5  
- Axe 11.5–16.5  

Axe is not strictly better than sword every hit (same floor). Mace matches axe **on average**, wins consistency, loses spikes.

`OLD_DAGGER` / merchant commons keep their own later tables; do not silently retune Iron-tier in this pass except to adopt min–max **shape** (axe widest, mace high floor, sword mid, dagger tight).

---

## 7. Rusty shield

New item: `RUSTY_SHIELD`, off-hand, sword/axe/mace only.

**Not** a 0–3 armour-rating roll in `armorK: 50` (invisible and easy to read as “the shield vanished”).

**Yes:** **block effectiveness** — extra **flat soak** on incoming hits while the shield is equipped, rolled per hit:

| Item | Block soak per hit | Armour rating (stable) |
| --- | --- | --- |
| `RUSTY_SHIELD` | **1–2** | Optional **1** if a visible armour number is wanted; do not fork that rating |
| `WOODEN_BUCKLER` (upgrade) | Keep as today (**armour 2**) until a later shield pass; then give a stronger soak than rusty (e.g. 2–3) |

- Floor **1**: the shield always participates. **0** is reserved for later (crit through shield, ARMOR_BREAK, “broke your guard”) so zero has a cause.  
- Apply soak **after** body armour (or as a distinct shield step). It must move the number the player sees. Thug 5–8 minus 1–2 is felt; armour rating 1 vs 2 in K=50 is not.  
- Combat log does **not** need “block rolled 2”. Optional later flavour: “covered” / “glancing cover” without numbers.  
- Defend / GUARDED stay the **chosen** defence verb. Shield soak is the passive texture of occupying off-hand. Do not require a shield to press Defend.  
- No block-chance %, reflect, bash, accuracy tax, or crit/dodge on rusty.

Worn Leather remains the main armour piece.

---

## 8. Items to add or change

| Code | Action |
| --- | --- |
| `RUSTY_SWORD` | Keep; change 6 → **4–8** per hit. Quest reward option |
| `RUSTY_AXE` | **Add.** 4–9. Quest reward |
| `RUSTY_MACE` | **Add.** 5–8. Quest reward |
| `RUSTY_DAGGER` | **Add.** 3–5. Quest reward. Distinct from `OLD_DAGGER` (merchant) |
| `RUSTY_SHIELD` | **Add.** Soak 1–2; optional armour 1. Quest reward with sword/axe/mace |
| `OLD_DAGGER`, `KNOBBED_CLUB`, `WOODSMAN_AXE`, `MILITIA_SHORTSWORD`, … | Stay merchant/loot upgrades, not first-quest rewards |
| Starting equipment grant | Remove guaranteed equipped Rusty Sword from character creation |

Quest rusties: **no 95–105% instance roll**. Merchant/loot items may keep it.

Schema implication (implementation, not this file’s job): item definitions need `weapon_damage_min` / `weapon_damage_max` (or equivalent). Monsters already have min/max. Shield soak needs a place that is **not** the same field as chest `armor_value` if both exist on the shield.

---

## 9. Implementation mapping (tasks)

| Task | Work implied |
| --- | --- |
| **06 Quest/NPC** | Auto-active first quest; Square officer; objectives in §2.3; exact-once rewards; family choice UI at NPC/armory |
| **07 Content** | Quest copy; issue rusties; do not send L1 to Sewers/Mine/Ruins; Street Thug as first teacher |
| **05 Economy** | Edric no longer sells “the only first weapon.” Price militia/woodsman/club/old dagger as upgrades over rusty. Start gold default 100 |
| **08 Combat** | Per-hit weapon range; family verbs without Combat 3.0; shield soak; do not dual-wield swords; dagger two-hit when that work ships |
| **Itemization / migration** | New rusty codes; sword range; starting kit without weapon |
| **UI** | First iteration: `FIRST_QUEST_BUSINESS_REQUIREMENTS.md` §5 (reuse Talk/tracker/log). Item tooltip `min–max`; shield block 1–2. Do not hide Market/Arena this pass. |

Tests: exact-once quest reward; cannot receive rusty + same-tier Edric as double starter; daggers never receive rusty shield; retreat still completes the search/report beat if designed that way; deterministic combat tests inject the damage/soak rolls.

---

## 10. Out of scope

- Bow, ammo, two-hand bow vs buckler teaching  
- Two-handed melee items and 3–9 two-hand axe  
- Dual dagger second-hit formula  
- Extra engine randomness beyond weapon range + shield soak  
- Spear, crossbow, new families  
- Ranked PvE/PvP rebalance  
- Full merchant min–max tables for every live weapon (follow the **shapes** when those items are touched)

---

## 11. Open questions (do not block the rusty kit)

1. ~~Quest id / officer name~~ → Bren + rework `QST_MILITIA_NOTICE` / display name **Issued Steel** (`FIRST_QUEST_BUSINESS_REQUIREMENTS.md`).  
2. ~~Before Old Town vs turn-in~~ → **First Talk grants kit.** Locked.  
3. Until dual-wield exists, one vs two `RUSTY_DAGGER` — prefer two if the slot allows; Bren copy stays valid.  
4. Rusty shield: soak **1–2** + optional armour **1**.
