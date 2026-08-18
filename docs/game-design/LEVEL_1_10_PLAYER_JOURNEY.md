# Greyhaven — Level 1–10 Player Journey

**Status:** Phase 3 Task 04 — authoritative game-design source  
**Governs:** Tasks 05–11  
**Does not:** implement code, change balance config, or invent Levels 11–30

Later implementation must follow this journey unless a documented game-design decision replaces it.

---

## 1. Purpose and authority

Phase 1 and Phase 2 built playable systems. They did not build a first chapter.

This document answers:

> What does the player actually do from character creation through Level 10, what do they learn, what choices appear, which content matters, and why do they continue?

It is the Phase 3 vertical-slice design:

Character Creation → Level 1 → … → Level 10 → first major milestone.

**Change control:** if Tasks 05–11 need a different unlock, climax, or path, update this file first. Do not silently invent a second journey in content or economy work.

**Locations UI:** `docs/game-design/LOCATIONS_SCREEN.md` (Home hub vs Locations panel vs Talk).  
**First session (business spec):** `docs/game-design/STARTING_EXPERIENCE_BUSINESS_SPEC.md`.  
**First session pace:** `docs/game-design/FIRST_FIVE_MINUTES.md`.  
**Index of design docs:** `docs/game-design/README.md`

---

## 2. Design principles

1. **Levels 1–10 are onboarding and the first RPG chapter**, not a low-stat copy of Level 30.
2. **Complexity is introduced on purpose.** Do not teach five statuses, four techniques, mastery, and the auction house before the first win.
3. **Guidance without a railroad.** The game presents a recommended path. Optional systems exist; they must not break progression or replace the chapter.
4. **Reuse existing systems.** Prefer Old Town, Forest, North Road, Harbour, Sewers, existing merchants, Combat 2.0, and Itemization 2.0. Do not invent Combat 3.0 or a second item model.
5. **Office-first.** 2–5 minute stops and 20–30 minute sessions must both work. No mandatory hour-long sits. Leaving mid-fight or mid-expedition must not destroy the character.
6. **XP is feedback, not motivation.** Goals are places, people, fights, and choices.
7. **No class lock.** Weapon family and attributes are reversible through Level 10 (free respec already exists).
8. **Do not rebalance numbers here.** Live XP table, start gold, and item stats stay as implemented until Task 05.

---

## 3. Current-game baseline

This section records what the live game already assumes. Task 04 does not change these values.

### 3.1 Character creation (implemented)

- One character per account; **name only**.
- Start: Level 1, 0 XP, **100 gold**, STR/AGI/END/PER **5/5/5/5**, 0 unspent points.
- Vitals at start: max health **165**, max stamina **85**.
- Location: **City Square**.
- Loadout (live today): **Rusty Sword** (MAIN_HAND), **Worn Leather Armor** (CHEST), **2× Healing Potion**.  
  **Journey decision:** see `WEAPON_FAMILIES_AND_STARTER_QUEST.md` — spawn without a proper weapon; first family is the first-quest rusty reward.
- Mastery: five families at 0; four empty technique slots.
- Professions: Blacksmith, Alchemist, Hunter at rank 1.
- Arena rating 1000; marks 0.
- Inventory capacity 40.

### 3.2 XP and attributes (implemented)

Cumulative XP to reach Level 10: **5,580**.

| Reach level | XP from previous | Cumulative |
| ---: | ---: | ---: |
| 2 | 100 | 100 |
| 3 | 180 | 280 |
| 4 | 280 | 560 |
| 5 | 400 | 960 |
| 6 | 550 | 1,510 |
| 7 | 720 | 2,230 |
| 8 | 900 | 3,130 |
| 9 | 1,100 | 4,230 |
| 10 | 1,350 | 5,580 |

- **+2** unspent attribute points per level. First allocation at Level 2.
- Free respec through Level 10.
- Character XP today: PvE combat and expedition claim only.

### 3.3 What is already live

Thirteen locations, eighteen monsters, one dungeon (Ruined Keep, boss Level 24), player Market, four NPC merchants, Forest Patrol expeditions (20 minutes), twelve crafting recipes, Combat 2.0, five weapon masteries, ranked Arena, activity feed, global chat.

**There is no quest framework, no FTUE, no Training Grounds, and no Level 10 dungeon.**

Travel recommended levels are display-only. Almost every system is reachable from Level 1.

---

## 4. Implementation conflicts

Each conflict states the live state and the **journey decision**. Implementation happens in later tasks.

| Conflict | Live state | Journey decision |
| --- | --- | --- |
| Feature dump at Level 1 | Nav exposes Market, Crafting, Expeditions, Arena, Mastery immediately | **Guide + UI emphasis.** Recommended path and quests point at one next beat. Do not hard-lock travel. Soft-hide or de-emphasize Ranked Arena, Crafting station depth, and dungeon entry until their unlock. Optional systems remain reachable for explorers. |
| No spine | No quests or next-action | **Task 06/07** add a militia / missing-caravan chapter. This doc defines beats only. |
| No Level 10 climax | Only Ruined Keep (L18–24) | **New early dungeon** using existing dungeon infrastructure. Ruined Keep stays post-10 / endgame. |
| Everyone starts Sword | Rusty Sword equipped; other families cheap at Market | **Keep name-only creation.** First weapon-family choice is the **first-quest rusty kit** (sword / axe / mace / daggers). Edric is the first **upgrade**, not the first identity. Bow is not a first-quest option. See `WEAPON_FAMILIES_AND_STARTER_QUEST.md`. |
| Techniques vs teaching | All core combat actions visible in fight 1; techniques after ~17 PvE wins | **First fight uses a reduced action set in presentation** (Task 08). First technique is a Level 4 beat, aligned with mastery 2 on one family. |
| Statuses too early | Wolf BLEED as soon as Forest is entered | **Forest is the first status teacher, at Level 3**, after Defend is known. |
| Merchants replace loot | 100g buys every starter family | **NPC Merchant is the intended first upgrade.** Loot/craft supply rolled / higher-tier pieces. Task 05 prices the second upgrade so merchants do not also replace Iron-tier loot. |
| Player Market at L1 | No gate | **Visit as optional from Level 5.** Not required. Do not make AH the best early upgrade path (Task 05). |
| Iron Ore in Old Mine | Blacksmith blocked for L1–7 | **Required first craft is Hunter or Alchemist.** Blacksmith is optional until Task 07 adds an in-band ore source (expedition or low-rate Forest/Old Town drop). |
| Arena at L1 | No level gate; rating ±400 | **Training Grounds at Level 8. Ranked PvP after Level 10** (or immediately after the L10 climax as optional). Task 09 implements TG. Soft-gate ranked challenges until then. |
| Soft danger | Sewers/Mine/Ruins enterable at L1 | **Keep travel open.** Communicate danger. Quests do not send L1–4 players into Sewers, Mine, Camp, or Ruins. |
| Thin catalog | Few upgrade steps; no L10 unique | **Task 07** adds the L10 dungeon reward and any missing in-band craft/loot step. Do not invent a second item model. |
| Mastery XP | PvE victory only (spec said “participate”) | **Keep victory-only for 1–10.** Do not require PvP mastery. |
| END affixes vs HP | Affix does not change max HP | **Do not teach END-on-gear as a vitality choice in 1–10.** Task 08/item follow-up may fix later. |
| Technique flavor vs code | Pierce/cleave/flurry mostly tags | **Teach techniques by what they actually do** (stamina, status, COUNTER/CLEAVE/ADVANCED tags). |

---

## 5. Answers to the required design questions

1. **First meaningful build choice:** first quest — choose a rusty melee family (Sword / Axe / Mace / Daggers). Not a class. Free to change through Level 10. Edric Varn is the first **upgrade** (and still the place to switch families if the player skipped or wants another rusty-tier replacement). Bow remains a later / merchant option, not the first-quest kit.
2. **When mastery begins to matter:** Level 4 — first technique unlocks (mastery 2 on the chosen family). Until then mastery is a quiet bar, not a lesson.
3. **First technique:** Level 4. It must change a decision (counter window, status, accuracy), not be a slightly stronger Quick Attack.
4. **When statuses become relevant:** Level 3 — Forest Wolf BLEED. Defend/potion matter. Further statuses (OFF_BALANCE, POISON, ARMOR_BREAK) land at Levels 5–7.
5. **First Player Market visit:** Level 5, optional. Browse after the player understands NPC prices and loot.
6. **First NPC Merchant:** Level 2 (weapon **upgrade**). Armor/potion restock may happen the same visit or at Level 3. First family is already chosen via the rusty quest kit.
7. **Expeditions:** Level 4 — first Forest Patrol from Tavern or Forest after the player understands leaving the city.
8. **Crafting:** Level 6 — first *guided* craft (Hunter leather or Alchemist potion). Not required at Level 1. Blacksmith is optional until ore exists in-band.
9. **Training Grounds:** Level 8 — practice Combat 2.0 against generated bots. Not the best XP/gold source.
10. **Real PvP:** After Level 10. Training Grounds is the Arena introduction. Ranked remains a Phase 2 system that stays **unavailable as a recommended activity** until the first chapter ends.
11. **First challenging enemy:** Level 3 Bandit (DEFENSIVE) and/or Forest Wolf (BLEED). The first fight that punishes “Quick Attack only.”
12. **First elite:** Level 5 **Bandit Veteran** on North Road (ARMORED, ARMOR_BREAK).
13. **First boss:** Level 10 **Watch-Captain of the Lost Caravan** (new early-dungeon boss). Not the Warden of the Keep.
14. **What makes Level 10 memorable:** Completing the caravan investigation in a short dungeon that tests Defend, one status, weapon identity, and the first technique — then a unique chapter reward. Not “XP bar filled.”
15. **Phase 2 systems that stay out of the recommended 1–10 path:** Ranked Arena and duels-as-progression; Bandit Camp; Ancient Ruins; Ruined Keep; Iron Plate as expected gear; profession ranks past “first useful craft”; mastery 8–10; clans; extra equipment slots / resistances / set bonuses (Coming Later).

---

## 6. Unlock and complexity curve

| Level | New complexity (teach this) | Newly emphasized | Still de-emphasized |
| ---: | --- | --- | --- |
| 1 | Locations, Search, HP, stamina, Quick Attack, potion, retreat | World, Character vitals, Inventory (read) | Attributes, Market, Craft, Arena, Mastery, Expeditions |
| 2 | Equipment swap, weapon families, first gold sink | NPC Merchants, Equipment | Player Market, Craft, Arena |
| 3 | Attribute allocation, Defend, armor vs damage | Character attributes, Forest | Techniques, Expeditions, Craft |
| 4 | First technique, stamina as a plan, expedition as offline work | Mastery loadout, Expeditions | Player Market, Arena, Craft |
| 5 | Elite enemy, armor interaction, optional AH browse | North Road, Player Market (optional) | Ranked PvP, dungeon |
| 6 | Status diversity (poison / off-balance), first craft | Harbour or Sewers edge, Crafting | Ranked PvP |
| 7 | Shielded / defensive enemy, accuracy and control | Sewers | Ranked PvP, Old Mine |
| 8 | Build check, Training Grounds | Arena **Training Grounds** | Ranked ladder |
| 9 | Prepare loadout, consumables, dungeon rules | Quest turn-in / dungeon brief | Ranked PvP, Ruined Keep |
| 10 | Multi-room dungeon, boss, chapter close | Early dungeon | Post-10 zones and ranked PvP |

---

## 7. Master journey table

Target times are **active play**, office-first. They assume Task 05 adds quest XP so the live fight-count (~80–150 searches) is not the only path. Until then, treat times as design intent.

| Level | Target time (cumulative) | Main goal | New system | Location | Enemy lesson | Gear state | Quest beat | Unlock |
| ---: | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | 10–20 min | Survive first street fight and return to the Square | World + basic combat | City Square, Old Town | Aggressive thug: hit, heal, leave | Chosen rusty melee (+ rusty shield except daggers), Worn Leather, 2 potions | Militia: issue steel, trouble in Old Town | Search, fight, inventory |
| 2 | 25–40 min | Buy a real weapon and wear it | NPC Merchant + equipment | Market | Same enemies, better tool | Chosen family COMMON, starter chest | Arm the watch: visit Edric | Merchants, equip |
| 3 | 45–70 min | Hunt the Forest edge; spend first attributes | Attributes + Defend | Forest | Wolf BLEED: Defend / potion | Light set start or buckler | Wolves on the timber road | Attribute panel |
| 4 | 1–1.5 h | Use first technique; send a patrol | Technique + Expedition | Forest / Tavern | Bandit DEFENSIVE: don’t face-tank | Family weapon + light pieces | Caravan overdue — patrol the woods | Mastery loadout, expeditions |
| 5 | 1.5–2.5 h | Face the Veteran on North Road | Elite + optional Market | North Road | Armored elite: armor / Heavy / technique | UNCOMMON weapon or mail path | Tracks lead north | Player Market (optional) |
| 6 | 2.5–3.5 h | Make or brew something useful | Crafting (Hunter/Alchemist) | Craftsmen Ward, Harbour | Marksman / brawler: accuracy | Mixed COMMON/UNCOMMON | Harbour rumor or leather for the road | Crafting guided |
| 7 | 3.5–4.5 h | Clear the sewer grate the caravan used | Status + shielded enemy | Sewers | Poison + shield: don’t ignore GUARDED | Mail or padded + potions | Down the grate | Greater potion *available* (not required) |
| 8 | 4.5–5.5 h | Test the build in Training Grounds | Training Grounds | Arena | Bot archetypes replay lessons | Near-complete light/medium set | Watch wants proof you can stand | Training Grounds |
| 9 | 5.5–6.5 h | Gather the last evidence; kit for the hideout | Dungeon briefing | North Road / Forest edge | Veteran / bandit mix | Consumables stocked | Hideout found | Early dungeon *unlock* |
| 10 | 6.5–8 h | Finish the Lost Caravan dungeon | Dungeon + boss | New early dungeon | Boss tests the chapter | Chapter unique + Iron-tier | Caravan accounted for | Chapter complete; ranked PvP *opens* |

---

## 8. Milestones

### 8.1 First Combat (Level 1, Old Town)

Teach: Search, start fight, Quick Attack, read HP/stamina, Use Potion, Retreat, return to a SAFE location.

Do **not** require: techniques, mastery, affixes, armor penetration, five statuses.

Enemy: **Street Thug** (AGGRESSIVE) preferred. Giant Rat is acceptable as a second fight (faster, ASSASSIN — keep it simple).

### 8.2 First merchant upgrade (Level 2, Market)

Family identity is taught at the **first-quest rusty choice** (`WEAPON_FAMILIES_AND_STARTER_QUEST.md`). Edric Varn stocks one COMMON **upgrade** per family. The player spends gold and equips. The lesson is **shop as a person and a better tool**, not the first identity.

| Family | Identity |
| --- | --- |
| Sword | Control / counter (Riposte) |
| Axe | Pressure / bleed / highest white damage (widest rusty range) |
| Mace | Armor and stun (high floor, modest ceiling) |
| Dagger | Tempo / poison / off-balance; no rusty shield |
| Bow | Accuracy, two-handed (no shield) — **not** in the first-quest kit |

Mara Helden may sell a buckler (upgrade over rusty shield) or a piece of light armor on the same visit. That is support, not the main choice.

### 8.3 First Attributes (Level 3)

The player has unspent points from Levels 2 and 3. Teach: four attributes, opportunity cost, free respec through 10.

Do not force a “correct” spread. A short militia hint may say Strength hits harder, Agility helps escape and stamina, Endurance lives longer, Perception lands hits.

### 8.4 First Technique (Level 4)

Unlock when mastery 2 is reached on the equipped family (live: 200 mastery XP, 12 per PvE win ≈ 17 wins). Task 05/07 may add a quest mastery grant or extra wins on-path so this lands at Level 4 rather than grinding.

The first equipped technique must be **the family’s Level-2 active** (Riposte, Rending Chop, Crushing Blow, Feint, Aimed Shot). Loadout: one slot filled is enough.

### 8.5 First Enemy Counter (Level 3–5)

- **Wolf BLEED** → Defend and potions are not optional flavor.
- **Bandit DEFENSIVE** → GUARDED / patience.
- **Bandit Veteran ARMOR_BREAK / ARMORED** → armor and Heavy/technique matter.

### 8.6 First Elite (Level 5)

**Bandit Veteran** on North Road. Mechanically heavier (HP, armor, ARMOR_BREAK). Quest should *point* here; random search may still find them (existing 15% weight).

### 8.7 First Economy Decision (Level 2, refined at 5)

Level 2: spend gold on a guaranteed merchant weapon vs keep gold for potions.  
Level 5 (optional): buy a Player Market roll vs trust NPC commons vs wait for loot.

Merchants = reliable baseline. Loot/craft = chance and identity. AH = player-driven extras, not the tutorial.

### 8.8 First Crafting Interaction (Level 6)

Guided: **Cure Leather** (Hunter, Wolf Pelts from Forest) **or** **Brew Healing Potion** (Alchemist, River Herb from the Level 4 expedition).

Blacksmith is **not** the first required craft until Task 07 places Iron Ore in-band.

### 8.9 First Arena Interaction (Level 8)

**Training Grounds only.** Generated bots, levels 1–10, same Combat 2.0 engine, clearly not human. Must not be the best XP/gold farm (Phase 3 spec).

### 8.10 Level 10 Milestone

Short linear dungeon (existing dungeon flow): 3–5 rooms, one optional side room, one boss.

Tests: stamina plan, Defend, one relevant status, equipped technique, retreat as a valid fail.

Reward: unique **chapter** item usable at Level 10 (not Warden’s Signet, req 18). Exact stats are Task 05/07.

---

## 9. Level-by-level design

Gold and attribute numbers below are **expected bands**, not Task 05 formulas. Live start is 100g and +2 points per level (18 unspent by Level 10 if none spent).

### Level 1 — First blood in Old Town

**Player state**

- Playtime: 10–20 minutes.
- Gear: Worn Leather, 2 potions; rusty family kit from the first quest (shield except daggers).
- Weapon: chosen at quest issue (sword / axe / mace / daggers). No default Sword.
- Attributes: 5/5/5/5, 0 unspent.
- Mastery: 0, no techniques.
- Gold: ~100 plus a few coins from thugs (live 4–10).
- Unlocked (emphasized): World movement, Search, combat basics, Inventory.

**Goal:** Follow the militia notice, search Old Town, win or retreat from the first fight, return to City Square.

**New concept:** Locations (SAFE vs DANGEROUS), Search, HP, stamina, Quick Attack, potion, retreat.

**Locations:** City Square, Old Town. Tavern may be visible; do not start expeditions.

**Enemies:** Street Thug — aggression and reading the enemy bar. Giant Rat — shorter fight; do not lead with it if it teaches dodge before attack.

**Equipment:** Starter only. Do not send the player to Market before the first fight.

**Quest beat:** First quest **Issued Steel** — BR and microscript in `docs/game-design/FIRST_QUEST_BUSINESS_REQUIREMENTS.md`.

**Stop point:** Back in City Square after a fight outcome. Recovery can tick while the tab is closed.

---

### Level 2 — A weapon with a name

**Player state**

- Playtime: 25–40 minutes cumulative.
- Gear: merchant COMMON of chosen family; Worn Leather still fine.
- Attributes: 5/5/5/5 plus **2 unspent** (from hitting 2). Spending may wait until Level 3.
- Mastery: a few wins on starter Sword; may reset identity if they switch family.
- Gold: leftover after one weapon (live merchant prices are low vs 100g).
- Consumables: 1–4 potions.

**Goal:** Buy and equip an **upgrade** over rusty (or switch family at the shop).

**New concept:** NPC shop as gold sink; equipment as a *better* tool. Families were chosen at Level 1.

**Locations:** Market (Edric; optional Mara / Calia). Return to Old Town to try the weapon.

**Enemies:** Same Old Town table. Lesson: the tool changed the fight, not the map.

**Equipment:** Militia Shortsword / Arming Sword / Woodsman Axe / Knobbed Club / Old Dagger / Hunting Bow. Bow teaches two-handed (no buckler).

**Quest beat:** “Better steel before you leave the walls.” Optional if rusty is enough; intended upgrade at Edric.

**Unlock:** NPC Merchants, Equipment panel as a *decision* screen.

**Optional:** ignore Market and keep rusty — allowed, slower.

**Stop point:** Equipped, in City Square or Market.

---

### Level 3 — The timber road

**Player state**

- Playtime: 45–70 minutes.
- Gear: chosen weapon; maybe buckler or one leather piece.
- Attributes: first spend (e.g. +2/+2 or a 4-point lean). Still near even is valid.
- Mastery: climbing toward 2 on one family.
- Gold: modest; potions are the sink.

**Goal:** Investigate wolf sign on the Forest road (caravan used this timber).

**New concept:** Attribute allocation; Defend; BLEED.

**Locations:** Forest. City Square as home. Do not require Sewers or Harbour.

**Enemies:** Forest Wolf — BLEED teaches why Defend and potions exist. Bandit (20% Forest table) if it appears: bonus lesson, not the scripted teacher.

**Equipment:** Leather pieces from Mara or lucky loot. Armor category (LIGHT dodge) can be mentioned once.

**Quest beat:** “Wolves hit the timber road. Look for wagon sign.”

**Unlock:** Attribute allocation is now *expected*, not hidden.

**Stop point:** After a wolf fight or after allocating points in town.

---

### Level 4 — Steel and a long watch

**Player state**

- Playtime: ~1–1.5 hours.
- Gear: family weapon; partial light set.
- Mastery: **first technique** on-path.
- Techniques: one active equipped.
- Gold: enough for potions and the cheap expedition start (live gold costs are small).
- Attributes: 4–6 points spent.

**Goal:** Use the new technique in a real fight; send a Forest Patrol so the world works while you are away.

**New concept:** Combat technique (loadout); expedition (start → leave → claim).

**Locations:** Forest and/or Tavern (both can start Forest Patrol today).

**Enemies:** Bandit (DEFENSIVE) as the “first counter” if not seen yet; wolves remain common.

**Equipment:** Iron-tier (req Level 2) may appear from loot/craft later; not required here.

**Quest beat:** “The caravan is overdue. Walk the woods — or send a patrol.”

**Unlock:** Mastery/technique UI as actionable; Expeditions.

**Office-first:** Starting the 20-minute patrol is the intended “I have a meeting” beat.

**Stop point:** Expedition running, or technique used and character in a SAFE location.

---

### Level 5 — North Road

**Player state**

- Playtime: ~1.5–2.5 hours.
- Gear: UNCOMMON weapon (Iron Sword/Axe/Mace) *or* still merchant COMMON; Mail Hauberk possible (req 4, STR 6) for STR leans.
- Mastery: 2+, one technique.
- Gold: first real surplus or first real shortage (potion/mail).
- Consumables: potions matter for the elite.

**Goal:** Follow tracks onto North Road and survive the **Bandit Veteran**.

**New concept:** Elite; armor / ARMOR_BREAK; optional Player Market.

**Locations:** North Road. Market optional after.

**Enemies:** Bandit Veteran (ELITE) — first elite. Bandits and leftover thugs fill the table.

**Equipment:** Meaningful upgrade is *survival kit* (potion, armor category) as much as weapon DPS.

**Quest beat:** “Someone marked the wagons. The north road knows more.”

**Unlock:** Player Market *as a place to look*, not a requirement.

**Optional:** Harbour can be peeked; not required.

**Stop point:** After Veteran victory or a clean retreat to the Square.

---

### Level 6 — Hands that make

**Player state**

- Playtime: ~2.5–3.5 hours.
- Gear: mixed COMMON/UNCOMMON; jewelry optional (Tomas).
- Crafting: first claimed job.
- Attributes: a visible lean (damage vs dodge vs live).
- Gold: craft gold + materials.

**Goal:** Turn a pelt or herb into something you will use.

**New concept:** Professions, timed craft, claim.

**Locations:** Craftsmen Ward. Harbour if the rumor beat is used (Dock Brawler / Smuggler).

**Enemies:** If Harbour: Dock Brawler (DEFENSIVE refresh), Smuggler (MARKSMAN, OFF_BALANCE) — accuracy and positioning of Precise Attack.

**Equipment:** Crafted leather piece or extra potions. Rolled rarity on crafted gear is a bonus lesson (COMMON–RARE), not the point.

**Quest beat:** “The road eats boots and blood. Make what the watch will not issue.”

**Unlock:** Crafting guided. Blacksmith recipes may show as locked/missing ore.

**Stop point:** Craft claimed, or Harbour fight done and back in town.

---

### Level 7 — Under the city

**Player state**

- Playtime: ~3.5–4.5 hours.
- Gear: toward a full light set or padded/mail chest.
- Techniques: still 1, maybe approaching mastery 4 (second technique) if focused.
- Consumables: Greater Healing Potion *available* at char Level 5 + Alchemist 5 — nice, not required.
- Gold: potions and repairs-by-replace.

**Goal:** Search the sewer grate the caravan used to slip goods — or that something used to drag them.

**New concept:** POISON; SHIELDED enemy; why accuracy and not-only-Heavy matter.

**Locations:** Old Town → Sewers. Not Old Mine.

**Enemies:** Plague Rat (POISON). Sewer Watchman (SHIELDED) — GUARDED / Cleave-tag lesson if axe; otherwise patience and Precise.

**Equipment:** Buckler vs two-handed bow is now a felt tradeoff in tight fights.

**Quest beat:** “The grate behind Old Town still smells of wet grain and blood.”

**Optional:** Old Mine is visible from Forest; recommended band 8–15. Do not quest-gate through it.

**Stop point:** Out of Sewers, SAFE, after a poison fight.

---

### Level 8 — Proof for the watch

**Player state**

- Playtime: ~4.5–5.5 hours.
- Gear: most slots filled with COMMON/UNCOMMON; Iron Plate is **not** expected (STR 14 / END 10).
- Mastery: 2–4 on main family; 0–2 on a side family if they experimented (free respec still on).
- Techniques: 1–2 actives.
- Attributes: ~14 points available from levels; a clear if still flexible build.

**Goal:** Prove the build in **Training Grounds** (bots that replay thug / wolf / armored lessons).

**New concept:** Arena as a *place*; practice vs people; defense profile can wait.

**Locations:** Sparring Yard (Training Grounds as shipped). City Square home. Ranked Arena stays a later building. See `docs/tasks/phase-3/sparring-yard.md`.

**Enemies:** Generated bots (Task 09). Must not pretend to be players.

**Equipment:** Swap and respec are encouraged; this is the experiment beat.

**Quest beat:** “The captain will not send you into the hideout untested.”

**Unlock:** Training Grounds. Ranked list stays de-emphasized / soft-gated.

**Stop point:** After a TG match, in Arena or Square.

---

### Level 9 — The hideout is real

**Player state**

- Playtime: ~5.5–6.5 hours.
- Gear: best in-band set they have; potions stocked.
- Consumables: 3+ healing potions expected.
- Gold: reserved for potions, not luxury AH.

**Goal:** Confirm the Lost Caravan hideout and kit up. Last free-respec chapter moment.

**New concept:** Dungeon rules (rooms, persist, leave and return).

**Locations:** North Road / Forest edge for the last overland fights. Briefing in Tavern or Square.

**Enemies:** Bandit / Veteran mix. No Ruin Guardian, no Warden.

**Equipment:** Fill empty slots (boots/gloves/amulet) from Mara, Tomas, loot, or craft.

**Quest beat:** “We know where they took the wagons. Come back ready.”

**Unlock:** Early dungeon entrance becomes the recommended next action.

**Stop point:** At the entrance, or in town with full potions — dungeon can wait overnight.

---

### Level 10 — The Lost Caravan

**Player state**

- Playtime: ~6.5–8 hours active (several office days).
- Gear: complete early set + chapter reward after boss.
- Mastery: 2–6 typical; 8+ is extra credit.
- Techniques: 1–2 (3 if very focused).
- Attributes: up to 18 points allocated; respec still free **at 10**, paid from 11.
- Gold: whatever Task 05 targets; enough to not be stuck without potions.

**Goal:** Enter the hideout, finish the dungeon, account for the caravan.

**New concept:** Boss encounter; chapter ending; what “after 10” means (ranked PvP opens, Camp/Ruins remain later).

**Locations:** New early dungeon only. Not Ancient Ruins.

**Enemies:** New in-band rooms (Task 07/10). Prefer remixes of taught archetypes (aggressive, bleed, armored, shielded) plus a **boss** that telegraphs a heavy hit (Defend/control).

**Equipment:** Chapter unique (amulet/weapon/token sink). Do not use Warden’s Signet.

**Quest beat:** Close the militia charter. The city is not saved; the first chapter is.

**Unlock:** Chapter complete. **Ranked Arena becomes a recommended optional system.** Bandit Camp / Ruins / Ruined Keep remain post-chapter content.

**Stop point:** Boss dead, rewards claimed, character in City Square. Natural end of the vertical slice.

---

## 10. Recommended path vs optional activities

### Recommended path

City Square → Old Town fight → Market weapon → Forest (attributes, wolf, technique) → Expedition → North Road Veteran → first craft → Sewers → Training Grounds → Lost Caravan dungeon.

### Optional (never required, must not be optimal XP/gold)

- Extra Old Town / Forest searches.
- Harbour as a side story at 5–6.
- Player Market browse/list from Level 5.
- Blacksmith if the player finds ore (Mine or future in-band source).
- Second weapon family / respec experiments.
- Old Mine tourism at 8+ (dangerous, not on the quest spine).
- Chat, activity feed, Office Mode — always available, not lessons.

### Out of recommended path until after Level 10

- Ranked Arena and treating duels as progression.
- Bandit Camp, Ancient Ruins, Ruined Keep.
- Expecting Iron Plate or EPIC dungeon jewelry.
- Clan play (Task 15+).

Deviating must not brick the character. Soft danger and free respec exist so curiosity is safe.

---

## 11. Full journey walkthrough (player’s eyes)

### Character creation

You register, pick a unique name, and enter Greyhaven. Nobody asks your class. You appear in **City Square** with a notched sword, scuffed leather, two red flasks, and a hundred coins. The Square is a hub: streets to Old Town, Market, Forest, North Road, Arena, Craftsmen Ward, Harbour. A militia notice is the thing that *wants* you.

### First 10 minutes

You walk to **Old Town** and Search. A Street Thug. You Quick Attack, watch stamina, drink a potion if you panic, or Retreat if you must. You win or limp home. The Square is safe. Health ticks back. You understand: this game is decisions and waiting, not WASD.

### Level 2

The notice says you are under-armed. **Edric Varn** lays out a shortsword, an axe, a club, a dagger, a bow. You buy one and equip it. The rusty blade is yesterday. You go back to Old Town and the same street feels different because *you* chose the tool.

### Level 3

You have points you did not have at creation. You put them somewhere — or you don’t, yet. The Forest is the next street the wagons used. A wolf opens you up. Bleeding. You Defend. You use a flask. You learn fights have a plan.

### Level 4

After enough work with one weapon, a technique appears. You put it on the loadout. The next fight has a button that is not “slightly more damage” — a riposte window, a bleed, a crush, a feint, an aimed shot. At the Tavern you send a **Forest Patrol** and close the tab. Twenty minutes later the activity feed has something to claim.

### Level 5

North Road. Among thugs and bandits, a **Veteran** in real armor. This is not a rat. You spend a potion on purpose. Afterward you may glance at the player boards in the Market. You do not need them.

### Level 6

Pelts or herbs become a cap, gloves, or another flask in the **Craftsmen Ward**. You wait out a short job. You own something you made. Harbour rumors are a detour if you like docks more than needles.

### Level 7

Old Town’s grate. Sewers. Poison. A watchman who hides behind a shield. You do not spam Heavy. You come up for air in the Square.

### Level 8

The Arena is not a ladder yet. **Training Grounds**: faceless drills that fight like things you already met. You respec for free and try the other idea. You are still you.

### Level 9

The watch knows where the wagons went. You fill empty slots, buy flasks, and stand at a door you can walk away from until tomorrow.

### Level 10

The hideout is a handful of rooms, not the ancient keep. You use the chapter: Defend, a status you recognize, your weapon’s first technique. The boss is a person with a job — a watch-captain gone wrong, a caravan master, a bandit who kept the books. You win. You take a reward that belongs to *this* story. Greyhaven is larger (Camp, Ruins, ranked steel), but this chapter is finished.

---

## 12. Narrative spine (beats only)

**Premise:** A militia charter. A caravan due in Greyhaven never arrived. The city still functions; the road does not.

**Tone:** Local, grim, practical. Match existing copy (watch, bandits, sewers, merchants). No second continent, no world war.

| Beat | Level | Intent |
| --- | ---: | --- |
| Notice posted in the Square | 1 | Why leave the hub; issue rusty kit |
| Better steel | 2 | Merchant as a person (Edric) — upgrade, not first family |
| Timber road / wolves | 3 | Forest as the wagon path |
| Overdue / send a patrol | 4 | Expedition belongs to the story |
| Tracks north; Veteran | 5 | Elite is a named threat, not a rare spawn only |
| Kit the road (craft) | 6 | Economy is survival |
| Grate and sewers | 7 | City underside |
| Proof in the yard | 8 | Training Grounds as watch trial |
| Hideout located | 9 | Dungeon is a place you chose to enter |
| Lost Caravan concluded | 10 | Chapter end |

**NPCs needed (Task 06):** militia officer (quest hub, Square or Tavern); Edric Varn (shop + talk); optionally Mara, Calia, Tomas as talk-and-shop; one Forest/Tavern expedition contact; one Arena drill instructor (TG); dungeon is environmental + boss.

**Not required:** branching CRPG dialogue, romance, faction wars.

Exact quest text is Task 06/07.

---

## 13. Content reuse map

### Use as-is (recommended path)

- Locations: City Square, Old Town, Market, Tavern, Forest, North Road, Craftsmen Ward, Sewers, Arena, Harbour (optional).
- Enemies: Street Thug, Giant Rat, Forest Wolf, Bandit, Bandit Veteran, Dock Brawler, Smuggler, Plague Rat, Sewer Watchman.
- Merchants: all four, especially Edric and Mara.
- Systems: Combat 2.0, equipment, mastery, Forest Patrol, crafting jobs, activity feed.
- Items: starter gear, merchant family weapons, light armor set, buckler, potions, Wolf Pelt, River Herb, Iron-tier weapons as loot upgrades.

### Task 07 / 10 must add

- Quest/NPC wiring (Task 06 framework).
- **Lost Caravan early dungeon** (rooms, in-band enemies, Level 10 boss, Level 10 unique reward).
- In-band **Iron Ore** source if Blacksmith should be optional-but-possible before Old Mine (expedition roll or low-rate drop).
- Training Grounds bots (Task 09).
- Soft-gate / UI emphasis for ranked PvP and out-of-band zones.
- Optional: one extra in-band upgrade step if Task 05 finds the Rusty → Merchant → Iron curve too flat.

### Do not use as the 1–10 climax

- Ancient Ruins, Ruined Keep, Warden of the Keep, Warden’s Signet.
- Bandit Camp roster (Cutthroat, Shielded Raider, Lieutenant) except as post-10.
- Pit Overseer / Cave Brute as *required* 1–10 bosses (Mine is optional tourism at 8+).

---

## 14. Office-first session model

| Session | What it is for | Safe stop |
| --- | --- | --- |
| 2–5 minutes | One Search, one combat round or finish, allocate a point, claim a craft/expedition, buy a potion | SAFE location or persisted combat (return later) |
| 20–30 minutes | A quest beat: Market visit, Forest block, Veteran attempt, TG match, dungeon rooms | After a beat, in town |
| Away from keyboard | Forest Patrol (20 min), craft timers, passive recovery | Claim when back |

**Rules**

- No mandatory scheduled events.
- Expeditions and crafts complete while offline; claim is exact-once (already implemented).
- Combat and dungeon progress must remain interruption-safe (existing persist). Defeat is 50% vitals, not a tutorial wipe of progress.
- Natural stops: quest objective done → Square / Tavern.

---

## 15. Risks and mitigations

| Risk | Mitigation |
| --- | --- |
| Feature overload | Complexity curve §6; UI emphasis; quests point at one beat |
| Excessive grind | Task 05 adds quest XP/gold so 5,580 XP is not 100+ empty searches |
| No reason to change gear | Level 2 family choice; Iron-tier and dungeon unique as later steps; Task 05 keeps merchant from selling the whole curve |
| Weapon families feel identical | Teach each family’s Level-2 technique; Task 08 keeps identities mechanical |
| Crafting irrelevant | Level 6 required *interaction*; materials on the path (pelts, herbs) |
| Merchant replaces loot | Merchants = COMMON baseline; rolled UNCOMMON+ from loot/craft/AH |
| Loot replaces merchant | First upgrade is guaranteed at Edric so unlucky drops cannot block the chapter |
| Arena too early | Ranked after 10; TG at 8; TG not best farm |
| Arena too late | TG at 8 still introduces the building and Combat 2.0 vs a “build” |
| Quests as checklist | Few beats; optional Harbour/Mine/AH; goals are places and fights |
| Combat repetition | New lesson per band; dungeon remixes archetypes; TG is practice not a new grind loop |
| Insufficient choice | Family, attributes, armor category, optional Harbour, respec through 10 |
| Iron Plate / Mine lure | Communicate requirements; do not quest into Mine |
| AH twink gear | Task 05: listing filters and prices so L1 cannot skip the chapter |

---

## 16. Handoff to later tasks

### Task 05 — Progression and economy

- Size quest XP/gold so target times in §7 are plausible on the live 5,580 curve.
- Keep start **100g** unless a documented change is required; if changed, update this file.
- Price merchant vs loot vs craft: Edric sells the first **upgrade** over rusty; Iron-tier is not cheaper than fighting. First family is the quest kit (`WEAPON_FAMILIES_AND_STARTER_QUEST.md`).
- Expected gold bands per level; potion sink; craft costs.
- Level 10 unique reward budget (not Warden’s Signet).
- Player Market must not outclass the recommended path before Level 5+.
- Training Grounds rewards: practice, not farm.

### Task 06 — Quest and NPC framework

Objective types this journey needs: `VISIT_LOCATION`, `TALK_TO_NPC`, `DEFEAT_ENEMY` / `KILL`, `ACQUIRE_ITEM`, `COMPLETE_EXPEDITION`, `CRAFT_ITEM`, `COMPLETE_DUNGEON`. `WIN_ARENA_MATCH` only if defined as Training Grounds, not ranked.

NPCs: militia officer, Edric, drill instructor; merchants as Talk+Shop.

Exact-once rewards; server authority.

### Task 07 — Level 1–10 content

- Author beats in §12.
- Lost Caravan dungeon + boss + unique.
- In-band ore if Blacksmith should work.
- Do not retune Ruined Keep into the L10 climax.
- Encounter tables may stay; quests must *point* at Veteran / wolf / sewers.

### Task 08 — Combat refinement

- First-fight presentation: emphasize Quick, Potion, Retreat; introduce Defend by Level 3, Precise/Heavy as soon as they help a taught lesson.
- Make the five Level-2 techniques feel like the identities in §8.2 and `WEAPON_FAMILIES_AND_STARTER_QUEST.md` (bow still later). Per-hit weapon ranges and rusty shield soak are specified there; do not invent Combat 3.0.
- Elite and boss readability (Veteran, L10 boss).
- Do not build Combat 3.0.

### Task 09 — Training Grounds

- Unlock emphasis at Level 8.
- Bots level 1–10, archetypes from this chapter, immutable snapshots, deterministic RNG.
- Clearly not human.
- Ranked remains separate and post-chapter.

### Task 10 — Early PvE and dungeon

- Implement/tune the **Lost Caravan** dungeon to this chapter’s lessons.
- Leave Ruined Keep as post-10.
- Veteran remains the first elite.

### Task 11 — Balance validation

- Play the walkthrough in §11 against this document.
- Confirm optional paths cannot skip the chapter’s teaching.

---

## 17. Open questions

Resolved by this document unless a human overrides:

- Climax = **new early dungeon**, not Ruined Keep.
- Gating = **guide + UI / soft-gate ranked PvP**, not hard travel locks.
- Time = **several office days, ~6.5–8 hours active**.
- Creation = **name only**; first weapon family = first-quest rusty kit; Market at Level 2 is the upgrade.
- Crafting = **yes, Level 6 Hunter or Alchemist**; Blacksmith optional pending ore.
- PvP = **TG at 8, ranked after 10**.
- Premise = **militia / Lost Caravan**.

**Still open (need a human only if they reject the above):**

1. Boss identity and name (watch-captain vs caravan master vs bandit bookkeeper) — flavor, same mechanical role.
2. Whether Task 05 may change the live 5,580 XP table. Default: **keep the table**, add quest XP.
3. Whether ranked PvP soft-gate is UI-only or a server rule. Default: **server rule** (cleaner, office-safer).

---

## 18. Out of scope

- Levels 11–30 design.
- Clan gameplay, territory, wars, alliances.
- Full economic formulas and loot tables (Task 05).
- Final quest prose and dungeon room-by-room scripts (Tasks 06–07, 10).
- Production UI system (Tasks 12–14).
- Code, migrations, and rebalance of `game-balance.yml`.

---

## 19. Acceptance checklist (Task 04)

- [x] This file exists at `docs/game-design/LEVEL_1_10_PLAYER_JOURNEY.md`.
- [x] Levels 1–10 have an explicit gameplay purpose.
- [x] System unlock order is defined.
- [x] Enemy-mechanic learning curve is defined.
- [x] Equipment/build learning curve is defined.
- [x] First major milestones are defined.
- [x] Level 10 has a meaningful climax.
- [x] Recommended vs optional path is defined.
- [x] Office-first constraints are respected.
- [x] Implementation conflicts are identified with decisions.
- [x] Remaining human questions are listed.
- [x] No production code was changed.
