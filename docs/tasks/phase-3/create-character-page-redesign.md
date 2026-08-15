# Create Character Page Redesign

## Status

Phase 3 — UI/UX productization of the post-registration character creation screen.

PLAN documented. Implementation follows this file.

## Purpose

Replace the current single-card “name + submit” create-character screen with the three-column dark-fantasy layout from the approved mockup, without changing starting combat stats, gold, loadout, or location.

## In scope

1. Full visual redesign of `/create-character` to match the mockup (Greyhaven branding, not the mockup wordmark “VERIDIA”).
2. Ten cosmetic portraits: five male, five female. Generated art files live under `frontend/public/character/avatars/`.
3. Gender toggle swaps the five-avatar set.
4. Horizontal avatar carousel (three visible thumbnails, arrow navigation, selected highlight). Clicking a thumbnail updates the large preview, title, and summary.
5. Thematic name randomizer is gender-aware (male pool vs female pool). Names may use letters, digits, and spaces (3–24); special characters are rejected.
6. Live “Your Character” summary (name, gender, selected avatar + thumbnail).
7. Primary **Enter Greyhaven** button is disabled until the name field is non-empty (and while submitting).
8. Client name hints: length/pattern validity. Optional authenticated name-availability check against the existing unique-name rule.
9. Persist `gender` and `avatar_code` on the character so the chosen portrait is shown in-game. Appearance is cosmetic only and does not affect stats, loot, or combat.
10. Keep existing `data-testid` contracts used by Selenium (`create-character-page`, `create-character-form`, `character-name`, `create-character-submit`, `create-character-error`).

## Out of scope

- Account-menu avatar change after creation (disclaimer copy only).
- Class, race, attributes, or any gameplay-affecting creation choices beyond cosmetic name/gender/avatar.
- Clan / social systems.

## Architecture

### Frontend

- Catalog: `frontend/src/character/avatars.ts` — codes, titles, gender, image URLs.
- Name pool: `frontend/src/character/nameRandomizer.ts` — injectable RNG for tests.
- Page: rewrite `CreateCharacterPage` as a three-column layout; styles in `frontend/src/pages/createCharacter.css`.
- `createCharacter` API body gains optional `gender` and `avatarCode`.
- `CharacterPortrait` accepts an optional `src` derived from `avatarCode`, falling back to `/character/default-avatar.webp`.

### Backend

- Flyway `V4__character_appearance.sql`:
  - `gender VARCHAR(8) NOT NULL DEFAULT 'MALE'`
  - `avatar_code VARCHAR(64) NOT NULL DEFAULT 'male_unyielding'`
  - check constraints for allowed gender and avatar codes
- Domain allowlist `CharacterAppearance` (pure Java, injectable-free) validates gender/avatar pairing.
- `CreateCharacterRequest` accepts optional `gender` and `avatarCode`. Omitted values default to `MALE` / `male_unyielding` so existing integration and e2e clients that POST `{ "name" }` keep working.
- `CharacterView` / `CharacterResponse` expose `gender` and `avatarCode`.
- `GET /api/v1/characters/name-available?name=` returns whether the name is unused (authenticated). Format errors remain 400 via query validation.

### Gameplay integrity

- Appearance is never used by combat, loot, economy, or progression.
- Server rejects unknown or gender-mismatched avatar codes (400). Client selection is intent only.

## Avatar catalog

| Code | Gender | Title |
| --- | --- | --- |
| `male_unyielding` | MALE | The Unyielding |
| `male_iron_vow` | MALE | The Iron Vow |
| `male_ashen_wolf` | MALE | The Ashen Wolf |
| `male_pale_heir` | MALE | The Pale Heir |
| `male_oathbound` | MALE | The Oathbound |
| `female_veiled` | FEMALE | The Veiled |
| `female_nightbloom` | FEMALE | The Nightbloom |
| `female_silver_thorn` | FEMALE | The Silver Thorn |
| `female_ember_queen` | FEMALE | The Ember Queen |
| `female_hollow_saint` | FEMALE | The Hollow Saint |

## UX details

- Brand: existing crest + Greyhaven wordmark + tagline “A persistent dark fantasy world.”
- Name field: controlled input, die/cube randomize button, hint “3–24 characters”, valid-format checkmark.
- Gender: two large gold-bordered buttons; selected state glows.
- Carousel: previous/next arrows; selected thumbnail gold border; pagination dots (one per avatar).
- Footer note: avatar is cosmetic; can be changed later from Account (not implemented in this task).
- Back: log out and return to login (authenticated users cannot stay on `/login`).
- Compact/office mode and narrow viewports stack columns.

## Tests

- Unit: appearance allowlist; name randomizer pattern/length; page interactions (empty submit disabled, randomize fills name, gender swaps catalog, thumbnail selection).
- Integration: create with explicit appearance; create with name-only defaults; reject mismatched avatar; name-available true/false.
- Existing Selenium helpers remain valid: type name, click submit.

## Verification

- Backend compile + unit/integration tests for character/auth.
- Frontend unit tests + production build.
- Do not change already-applied Flyway `V1`–`V3`.
