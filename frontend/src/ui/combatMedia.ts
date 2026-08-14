const MONSTER_ART: Record<string, string> = {
  STREET_THUG: '/combat/street_thug.webp',
  GIANT_RAT: '/combat/giant_rat.webp',
  FOREST_WOLF: '/combat/forest_wolf.webp',
  BANDIT: '/combat/bandit.webp',
  BANDIT_VETERAN: '/combat/bandit_veteran.webp',
}

export const PLAYER_COMBAT_AVATAR_URL = '/combat/player.webp'

export function monsterCombatArtUrl(code: string): string {
  return MONSTER_ART[code] ?? MONSTER_ART.STREET_THUG
}
