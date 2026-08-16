import { describe, expect, it } from 'vitest'
import { monsterCombatArtUrl, PLAYER_COMBAT_AVATAR_URL } from './combatMedia'

describe('combatMedia', () => {
  it('maps known monsters and falls back for unknown codes', () => {
    expect(PLAYER_COMBAT_AVATAR_URL).toBe('/combat/player.webp')
    expect(monsterCombatArtUrl('FOREST_WOLF')).toBe('/combat/forest_wolf.webp')
    expect(monsterCombatArtUrl('SPARRING_BOT_L07')).toBe('/sparring/full/sparring_bot_l07.webp')
    expect(monsterCombatArtUrl('UNKNOWN')).toBe('/combat/street_thug.webp')
  })
})
