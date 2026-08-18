# Game design (Greyhaven)

Authoritative product-design docs for Phase 3 play. **Not code.** Implementation follows these unless a later dated decision replaces them.

| File | Owns |
| --- | --- |
| [STARTING_EXPERIENCE_BUSINESS_SPEC.md](./STARTING_EXPERIENCE_BUSINESS_SPEC.md) | **Business spec** for the first session (prologue → Issued Steel close) |
| [FIRST_FIVE_MINUTES.md](./FIRST_FIVE_MINUTES.md) | Prologue copy, Bren highlight, click contracts, minute clock |
| [FIRST_QUEST_BUSINESS_REQUIREMENTS.md](./FIRST_QUEST_BUSINESS_REQUIREMENTS.md) | Issued Steel microscript, objectives, kit table, quest AC |
| [LOCATIONS_SCREEN.md](./LOCATIONS_SCREEN.md) | Home vs Locations, panel inventory, NPC strip, Here now, **Talk mode** |
| [WEAPON_FAMILIES_AND_STARTER_QUEST.md](./WEAPON_FAMILIES_AND_STARTER_QUEST.md) | Melee families, rusty kit, damage forks, rusty shield soak |
| [LEVEL_1_10_PLAYER_JOURNEY.md](./LEVEL_1_10_PLAYER_JOURNEY.md) | Chapter 1–10 spine after the first session |

## Mockups

| File | What |
| --- | --- |
| `docs/mockups/main.png` | Home shell grammar (approved visual fidelity) |
| `docs/mockups/locations-canonical.png` | Locations **idle** (art + NPC strip + Here now + chat) |
| `docs/mockups/locations-talk.png` | Locations **Talk** — same frame; Here now swapped for dialogue |
| `docs/mockups/locations-bren-lead.png` | First Square: Bren glow + TALK + coach line |
| `docs/mockups/locations-bren-lead-card.png` | Close-up of the offered Bren card |
| `docs/mockups/locations-ftue-safe.png` | Post-kit Talk: SAFE chip glow |
| `docs/mockups/locations-ftue-travel.png` | First Travel sheet; Old Town offered |
| `docs/mockups/locations-ftue-oldtown.png` | First Old Town: DANGEROUS + Search |

Generated Locations mocks are layout contracts, not pixel-perfect vs `main.png`.

## Decision order when docs disagree

1. `STARTING_EXPERIENCE_BUSINESS_SPEC.md` for first-session business rules  
2. `FIRST_FIVE_MINUTES.md` for prologue text, highlight, and pace  
3. `LOCATIONS_SCREEN.md` for Home / Locations / Talk chrome  
4. `FIRST_QUEST_BUSINESS_REQUIREMENTS.md` for Issued Steel copy and quest rules  
5. `WEAPON_FAMILIES_AND_STARTER_QUEST.md` for kit and combat identity  
6. `LEVEL_1_10_PLAYER_JOURNEY.md` for the rest of 1–10  

Live code (e.g. Locations without chat, Talk as a people directory) is **behind** these specs until implemented.
